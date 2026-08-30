package com.clevstack.clevbill.controller;

import com.clevstack.clevbill.dto.RoleRequest;
import com.clevstack.clevbill.dto.RoleResponse;
import com.clevstack.clevbill.service.RoleService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PreAuthorize("hasAuthority('ROLE_MGMT:VIEW')")
    @GetMapping
    public List<RoleResponse> list() {
        return roleService.list();
    }

    @PreAuthorize("hasAuthority('ROLE_MGMT:VIEW')")
    @GetMapping("/{id}")
    public RoleResponse get(@PathVariable Long id) {
        return roleService.get(id);
    }

    @PreAuthorize("hasAuthority('ROLE_MGMT:CREATE')")
    @PostMapping
    public RoleResponse create(@Valid @RequestBody RoleRequest request) {
        return roleService.create(request);
    }

    @PreAuthorize("hasAuthority('ROLE_MGMT:EDIT')")
    @PutMapping("/{id}")
    public RoleResponse update(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        return roleService.update(id, request);
    }

    @PreAuthorize("hasAuthority('ROLE_MGMT:DELETE')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        roleService.delete(id);
    }
}
