package com.clevstack.clevbill.service;

import com.clevstack.clevbill.dto.PermissionResponse;
import com.clevstack.clevbill.dto.RoleRequest;
import com.clevstack.clevbill.dto.RoleResponse;
import com.clevstack.clevbill.exception.ResourceNotFoundException;
import com.clevstack.clevbill.model.Permission;
import com.clevstack.clevbill.model.Role;
import com.clevstack.clevbill.repository.PermissionRepository;
import com.clevstack.clevbill.repository.RoleRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> list() {
        return roleRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse get(Long id) {
        return toResponse(findEntity(id));
    }

    public RoleResponse create(RoleRequest request) {
        Role role = new Role();
        role.setRoleName(request.roleName());
        role.setDescription(request.description());
        role.setActive(request.active());
        role.setPermissions(resolvePermissions(request.permissionIds()));
        return toResponse(roleRepository.save(role));
    }

    public RoleResponse update(Long id, RoleRequest request) {
        Role role = findEntity(id);

        if (role.isSystem() && !role.getRoleName().equals(request.roleName())) {
            throw new IllegalArgumentException("System role '" + role.getRoleName() + "' cannot be renamed");
        }

        role.setDescription(request.description());

        // Super Admin is the emergency-access role — never let it be
        // deactivated or have permissions stripped from it via the API.
        if (role.getRoleName().equals(Role.SUPER_ADMIN)) {
            role.setActive(true);
            role.setPermissions(new HashSet<>(permissionRepository.findAll()));
        } else {
            role.setActive(request.active());
            role.setPermissions(resolvePermissions(request.permissionIds()));
        }

        return toResponse(roleRepository.save(role));
    }

    public void delete(Long id) {
        Role role = findEntity(id);
        if (role.isSystem()) {
            throw new IllegalArgumentException("System role '" + role.getRoleName() + "' cannot be deleted");
        }
        roleRepository.delete(role);
    }

    Role findEntity(Long id) {
        return roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role not found: " + id));
    }

    private Set<Permission> resolvePermissions(List<Long> permissionIds) {
        Set<Permission> permissions = new HashSet<>();
        for (Long permissionId : permissionIds) {
            permissions.add(permissionRepository
                    .findById(permissionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permissionId)));
        }
        return permissions;
    }

    private RoleResponse toResponse(Role role) {
        List<PermissionResponse> permissions = role.getPermissions().stream()
                .map(p -> new PermissionResponse(p.getId(), p.getModuleCode(), p.getAction(), p.getDescription()))
                .sorted((a, b) -> {
                    int byModule = a.moduleCode().compareTo(b.moduleCode());
                    return byModule != 0 ? byModule : a.action().compareTo(b.action());
                })
                .toList();

        return new RoleResponse(
                role.getId(),
                role.getRoleName(),
                role.getDescription(),
                role.isSystem(),
                role.isActive(),
                permissions,
                role.getCreatedAt(),
                role.getUpdatedAt());
    }
}
