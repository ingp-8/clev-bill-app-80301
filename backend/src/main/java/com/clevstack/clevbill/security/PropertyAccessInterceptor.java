package com.clevstack.clevbill.security;

import com.clevstack.clevbill.service.PropertyAccessService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Closes the gap {@code @PreAuthorize} leaves open: a permission check
 * proves a user can perform an action ("can this role EDIT items"), not
 * that they're allowed to touch this particular property's data. Every
 * request whose path carries a {@code propertyId} variable — every
 * "list under this property" / "create under this property" endpoint —
 * is checked here against PropertyAccessService before the controller
 * runs.
 *
 * <p>Known gap: endpoints addressed by a bare resource id
 * ({@code /api/v1/categories/{id}}, not {@code /api/v1/properties/{propertyId}/categories/{id}})
 * don't carry a propertyId in the path, so this interceptor can't check
 * them — get/update/delete-by-id doesn't yet re-verify the resource's
 * owning property against the caller's access. Fast-follow, not
 * silently accepted.
 */
@Component
public class PropertyAccessInterceptor implements HandlerInterceptor {

    private final PropertyAccessService propertyAccessService;

    public PropertyAccessInterceptor(PropertyAccessService propertyAccessService) {
        this.propertyAccessService = propertyAccessService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Object attribute = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (!(attribute instanceof Map<?, ?> rawVariables)) {
            return true;
        }
        Map<String, String> pathVariables = (Map<String, String>) rawVariables;
        String propertyIdValue = pathVariables.get("propertyId");
        if (propertyIdValue == null) {
            return true;
        }

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return true; // let Spring Security's own auth check handle this
        }

        Long propertyId = Long.valueOf(propertyIdValue);
        if (!propertyAccessService.hasAccessByUsername(authentication.getName(), propertyId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"No access to property " + propertyId + "\"}");
            return false;
        }

        return true;
    }
}
