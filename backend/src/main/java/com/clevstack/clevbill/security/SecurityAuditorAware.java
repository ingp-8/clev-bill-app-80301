package com.clevstack.clevbill.security;

import com.clevstack.clevbill.model.User;
import com.clevstack.clevbill.repository.UserRepository;
import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the current request's authenticated user for {@code @CreatedBy}/
 * {@code @LastModifiedBy} auditing. Returns empty for unauthenticated writes
 * (Flyway seed data, the bootstrap admin insert) — those rows simply carry no
 * auditor rather than failing.
 */
@Component
public class SecurityAuditorAware implements AuditorAware<Long> {

    private final UserRepository userRepository;

    public SecurityAuditorAware(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return userRepository.findByUsername(authentication.getName()).map(User::getId);
    }
}
