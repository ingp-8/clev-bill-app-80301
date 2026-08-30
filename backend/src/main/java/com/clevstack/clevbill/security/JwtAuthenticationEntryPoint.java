package com.clevstack.clevbill.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Handles requests that reach a protected endpoint with no valid authentication
 * (missing, expired, or malformed JWT) — returns a clean 401 with the same
 * shape as {@code ApiError}, instead of Spring Security's default blank
 * response. Builds the JSON by hand rather than pulling in a Jackson
 * ObjectMapper for one fixed, five-field body.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                """
                {"timestamp":"%s","status":401,"error":"Unauthorized","message":"Authentication required","path":"%s"}"""
                        .formatted(Instant.now(), escape(request.getRequestURI())));
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
