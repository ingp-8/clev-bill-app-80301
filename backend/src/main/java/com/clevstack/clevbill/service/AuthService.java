package com.clevstack.clevbill.service;

import com.clevstack.clevbill.dto.LoginRequest;
import com.clevstack.clevbill.dto.LoginResponse;
import com.clevstack.clevbill.dto.RefreshRequest;
import com.clevstack.clevbill.model.Role;
import com.clevstack.clevbill.model.User;
import com.clevstack.clevbill.repository.UserRepository;
import com.clevstack.clevbill.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtService jwtService,
            RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        User user = userRepository
                .findByUsername(request.username())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + request.username()));

        return issueTokens(user);
    }

    @Transactional
    public LoginResponse refresh(RefreshRequest request) {
        User user = refreshTokenService.consume(request.refreshToken());
        return issueTokens(user);
    }

    @Transactional
    public void logout(RefreshRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    private LoginResponse issueTokens(User user) {
        String accessToken = jwtService.generateToken(user.getUsername());
        String refreshToken = refreshTokenService.issue(user);
        var roleNames = user.getRoles().stream().map(Role::getRoleName).toList();

        return new LoginResponse(
                accessToken,
                refreshToken,
                user.getUsername(),
                user.getFullName(),
                roleNames,
                jwtService.getExpiration(accessToken));
    }
}
