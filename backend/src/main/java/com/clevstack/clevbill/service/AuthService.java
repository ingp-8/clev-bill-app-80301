package com.clevstack.clevbill.service;

import com.clevstack.clevbill.dto.LoginRequest;
import com.clevstack.clevbill.dto.LoginResponse;
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

    public AuthService(
            AuthenticationManager authenticationManager, UserRepository userRepository, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        User user = userRepository
                .findByUsername(request.username())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + request.username()));

        String token = jwtService.generateToken(user.getUsername());
        var roleNames = user.getRoles().stream().map(Role::getRoleName).toList();

        return new LoginResponse(token, user.getUsername(), user.getFullName(), roleNames, jwtService.getExpiration(token));
    }
}
