package com.clevstack.clevbill.controller;

import com.clevstack.clevbill.dto.PermissionResponse;
import com.clevstack.clevbill.repository.PermissionRepository;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only — the permission catalog is fixed by the module list seeded in V14, not user-editable. */
@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {

    private final PermissionRepository permissionRepository;

    public PermissionController(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @PreAuthorize("hasAuthority('ROLE_MGMT:VIEW')")
    @GetMapping
    public List<PermissionResponse> list() {
        return permissionRepository.findAll().stream()
                .map(p -> new PermissionResponse(p.getId(), p.getModuleCode(), p.getAction(), p.getDescription()))
                .toList();
    }
}
