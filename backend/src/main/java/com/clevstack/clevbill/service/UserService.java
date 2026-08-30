package com.clevstack.clevbill.service;

import com.clevstack.clevbill.dto.MasterRefResponse;
import com.clevstack.clevbill.dto.UserRequest;
import com.clevstack.clevbill.dto.UserResponse;
import com.clevstack.clevbill.exception.ResourceNotFoundException;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.model.Role;
import com.clevstack.clevbill.model.User;
import com.clevstack.clevbill.model.UserPropertyAccess;
import com.clevstack.clevbill.repository.PropertyRepository;
import com.clevstack.clevbill.repository.RoleRepository;
import com.clevstack.clevbill.repository.UserPropertyAccessRepository;
import com.clevstack.clevbill.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PropertyRepository propertyRepository;
    private final UserPropertyAccessRepository userPropertyAccessRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PropertyRepository propertyRepository,
            UserPropertyAccessRepository userPropertyAccessRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.propertyRepository = propertyRepository;
        this.userPropertyAccessRepository = userPropertyAccessRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return toResponse(findEntity(id));
    }

    public UserResponse create(UserRequest request) {
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("password is required when creating a user");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setFullName(request.fullName());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEnabled(request.enabled());
        user.setRoles(resolveRoles(request.roleIds()));
        User saved = userRepository.save(user);
        syncPropertyAccess(saved, request.propertyIds());
        return toResponse(saved);
    }

    public UserResponse update(Long id, UserRequest request) {
        User user = findEntity(id);
        user.setUsername(request.username());
        user.setFullName(request.fullName());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        user.setEnabled(request.enabled());
        user.setRoles(resolveRoles(request.roleIds()));
        User saved = userRepository.save(user);
        syncPropertyAccess(saved, request.propertyIds());
        return toResponse(saved);
    }

    public void delete(Long id) {
        User user = findEntity(id);
        for (UserPropertyAccess access : userPropertyAccessRepository.findByUserId(id)) {
            userPropertyAccessRepository.delete(access);
        }
        userRepository.delete(user);
    }

    private User findEntity(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private Set<Role> resolveRoles(List<Long> roleIds) {
        Set<Role> roles = new HashSet<>();
        for (Long roleId : roleIds) {
            roles.add(roleRepository
                    .findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId)));
        }
        return roles;
    }

    private void syncPropertyAccess(User user, List<Long> propertyIds) {
        List<UserPropertyAccess> existing = userPropertyAccessRepository.findByUserId(user.getId());
        Set<Long> existingPropertyIds = new HashSet<>();
        for (UserPropertyAccess access : existing) {
            existingPropertyIds.add(access.getProperty().getId());
        }

        Set<Long> desired = new HashSet<>(propertyIds);
        for (UserPropertyAccess access : existing) {
            if (!desired.contains(access.getProperty().getId())) {
                userPropertyAccessRepository.delete(access);
            }
        }

        for (Long propertyId : desired) {
            if (existingPropertyIds.contains(propertyId)) {
                continue;
            }
            Property property = propertyRepository
                    .findById(propertyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));
            UserPropertyAccess access = new UserPropertyAccess();
            access.setUser(user);
            access.setProperty(property);
            access.setClient(property.getClient());
            userPropertyAccessRepository.save(access);
        }
    }

    private UserResponse toResponse(User user) {
        List<MasterRefResponse> roles =
                user.getRoles().stream().map(r -> new MasterRefResponse(r.getId(), r.getRoleName())).toList();
        List<Long> propertyIds = userPropertyAccessRepository.findByUserId(user.getId()).stream()
                .map(access -> access.getProperty().getId())
                .toList();

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.isEnabled(),
                roles,
                propertyIds,
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
