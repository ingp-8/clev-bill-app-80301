package com.clevstack.clevbill.service;

import com.clevstack.clevbill.exception.InvalidRefreshTokenException;
import com.clevstack.clevbill.model.RefreshToken;
import com.clevstack.clevbill.model.User;
import com.clevstack.clevbill.repository.RefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Issues and validates the long-lived refresh token that lets a client mint
 * new short-lived access tokens without re-entering credentials. Stored only
 * as a SHA-256 hash — the raw value exists solely in the response sent to
 * the client and is never persisted or logged.
 *
 * <p>Rotation on every use: each refresh call revokes the token it was given
 * and issues a brand new one, so a stolen-but-unused refresh token becomes
 * useless the moment the legitimate client uses theirs, and reuse of a
 * revoked token is a detectable signal (not currently escalated further —
 * see {@link #consume}).
 */
@Service
@Transactional
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final long expirationDays;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${app.jwt.refresh-expiration-days}") long expirationDays) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.expirationDays = expirationDays;
    }

    public String issue(User user) {
        String rawToken = generateRawToken();
        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setTokenHash(hash(rawToken));
        entity.setExpiresAt(Instant.now().plus(expirationDays, ChronoUnit.DAYS));
        refreshTokenRepository.save(entity);
        return rawToken;
    }

    /** Validates and revokes {@code rawToken}, returning the user it belonged to. */
    public User consume(String rawToken) {
        RefreshToken entity = refreshTokenRepository
                .findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not recognized"));
        if (!entity.isActive()) {
            throw new InvalidRefreshTokenException("Refresh token expired or already used");
        }
        entity.setRevokedAt(Instant.now());
        refreshTokenRepository.save(entity);
        return entity.getUser();
    }

    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken)).ifPresent(entity -> {
            entity.setRevokedAt(Instant.now());
            refreshTokenRepository.save(entity);
        });
    }

    private static String generateRawToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
