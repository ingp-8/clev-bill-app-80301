package com.clevstack.clevbill.service;

import com.clevstack.clevbill.dto.MeResponse;
import com.clevstack.clevbill.dto.PropertyResponse;
import com.clevstack.clevbill.exception.ResourceNotFoundException;
import com.clevstack.clevbill.model.Permission;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.model.Role;
import com.clevstack.clevbill.model.User;
import com.clevstack.clevbill.repository.UserRepository;
import java.util.List;
import java.util.TreeSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MeService {

    private final UserRepository userRepository;
    private final PropertyAccessService propertyAccessService;

    public MeService(UserRepository userRepository, PropertyAccessService propertyAccessService) {
        this.userRepository = userRepository;
        this.propertyAccessService = propertyAccessService;
    }

    public MeResponse getProfile(String username) {
        User user = findUser(username);

        List<String> roles = user.getRoles().stream().map(Role::getRoleName).sorted().toList();

        TreeSet<String> permissions = new TreeSet<>();
        for (Role role : user.getRoles()) {
            for (Permission permission : role.getPermissions()) {
                permissions.add(permission.getModuleCode() + ":" + permission.getAction());
            }
        }

        return new MeResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                roles,
                List.copyOf(permissions),
                propertyAccessService.isSuperAdmin(user));
    }

    public List<PropertyResponse> getAccessibleProperties(String username) {
        User user = findUser(username);
        return propertyAccessService.getAccessibleProperties(user).stream().map(this::toResponse).toList();
    }

    private User findUser(String username) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private PropertyResponse toResponse(Property property) {
        return new PropertyResponse(
                property.getId(),
                property.getClient().getId(),
                property.getClient().getClientName(),
                property.getPropertyName(),
                property.getAddress(),
                property.getGstin(),
                property.getInvoiceSeriesPrefix(),
                property.getDefaultCgstRate(),
                property.getDefaultSgstRate(),
                property.getDefaultIgstRate(),
                property.isEInvoiceEnabled(),
                property.isActive(),
                property.getCreatedAt(),
                property.getUpdatedAt());
    }
}
