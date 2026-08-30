package com.clevstack.clevbill.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clevstack.clevbill.dto.RoleRequest;
import com.clevstack.clevbill.model.Permission;
import com.clevstack.clevbill.model.PermissionAction;
import com.clevstack.clevbill.model.Role;
import com.clevstack.clevbill.repository.PermissionRepository;
import com.clevstack.clevbill.support.AbstractIntegrationTest;
import com.clevstack.clevbill.support.TestEntityFactory;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Role is a fully global master guarded by ROLE_MGMT:&lt;ACTION&gt;. Also
 * covers RoleService's system-role guards, read straight from the code
 * (not assumed): renaming any system role (is_system = true) is rejected,
 * deleting any system role is rejected, but only "Super Admin" specifically
 * is protected against deactivation/permission-stripping via update —
 * "Store Manager" and "Operator" are system roles too, yet their
 * description/active/permissions CAN be changed through a normal update.
 * That asymmetry is exercised explicitly below and flagged in the test
 * names/comments rather than "fixed", since it reads as a deliberate
 * design choice (Super Admin is called out by name in RoleService as the
 * emergency-access role) rather than an obvious bug.
 */
class RoleControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PermissionRepository permissionRepository;

    private Long permissionId(String moduleCode, PermissionAction action) {
        return permissionRepository.findAll().stream()
                .filter(p -> p.getModuleCode().equals(moduleCode) && p.getAction() == action)
                .map(Permission::getId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Permission not seeded: " + moduleCode + ":" + action));
    }

    @Test
    void createCustomRoleWithSubsetOfPermissionsRoundTripsThePermissions() throws Exception {
        String token = adminToken();
        Long viewId = permissionId("MASTERS_ITEM", PermissionAction.VIEW);
        Long createId = permissionId("MASTERS_ITEM", PermissionAction.CREATE);

        String roleName = TestEntityFactory.unique("CustomRole");
        RoleRequest request = new RoleRequest(roleName, "custom test role", true, List.of(viewId, createId));

        mockMvc.perform(authJson(post("/api/v1/roles"), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.roleName").value(roleName))
                .andExpect(jsonPath("$.isSystem").value(false))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.permissions", hasSize(2)))
                .andExpect(jsonPath("$.permissions[*].moduleCode", org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.is("MASTERS_ITEM"))));
    }

    @Test
    void createRejectsBlankRoleName() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(post("/api/v1/roles"), token)
                        .content(objectMapper.writeValueAsString(new RoleRequest("", null, true, List.of()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRejectsDuplicateRoleName() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(post("/api/v1/roles"), token)
                        .content(objectMapper.writeValueAsString(new RoleRequest("Operator", null, true, List.of()))))
                .andExpect(status().isConflict());
    }

    @Test
    void createWithUnknownPermissionIdReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(post("/api/v1/roles"), token)
                        .content(objectMapper.writeValueAsString(
                                new RoleRequest(TestEntityFactory.unique("Role"), null, true, List.of(999999L)))))
                .andExpect(status().isNotFound());
    }

    @Test
    void listIncludesTheThreeSeededSystemRoles() throws Exception {
        String token = adminToken();

        mockMvc.perform(auth(get("/api/v1/roles"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[?(@.roleName == 'Super Admin')]").exists())
                .andExpect(jsonPath("$[?(@.roleName == 'Store Manager')]").exists())
                .andExpect(jsonPath("$[?(@.roleName == 'Operator')]").exists());
    }

    @Test
    void getReturnsRoleWithItsPermissions() throws Exception {
        String token = adminToken();
        Role operator = factory.role("Operator");

        mockMvc.perform(auth(get("/api/v1/roles/" + operator.getId()), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").value("Operator"))
                .andExpect(jsonPath("$.isSystem").value(true))
                .andExpect(jsonPath("$.permissions").isNotEmpty());
    }

    @Test
    void getUnknownRoleReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(auth(get("/api/v1/roles/999999"), token))
                .andExpect(status().isNotFound());
    }

    @Test
    void renamingASystemRoleIsRejected() throws Exception {
        String token = adminToken();
        Role operator = factory.role("Operator");

        RoleRequest request = new RoleRequest("Not Operator Anymore", "desc", true, List.of());

        mockMvc.perform(authJson(put("/api/v1/roles/" + operator.getId()), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatingNonNameFieldsOfANonSuperAdminSystemRoleIsAllowed() throws Exception {
        // RoleService only guards Super Admin's permissions/active flag; Operator
        // (also is_system = true) can still have its description, active flag,
        // and permission set changed via a normal update as long as the name
        // is left untouched. Documenting actual behavior here.
        String token = adminToken();
        Role operator = factory.role("Operator");
        Long viewId = permissionId("REPORTS", PermissionAction.VIEW);

        RoleRequest request = new RoleRequest("Operator", "updated description", false, List.of(viewId));

        mockMvc.perform(authJson(put("/api/v1/roles/" + operator.getId()), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").value("Operator"))
                .andExpect(jsonPath("$.description").value("updated description"))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.permissions", hasSize(1)))
                .andExpect(jsonPath("$.permissions[0].moduleCode").value("REPORTS"));
    }

    @Test
    void updatingSuperAdminAlwaysKeepsItActiveWithEveryPermission() throws Exception {
        String token = adminToken();
        Role superAdmin = factory.role(Role.SUPER_ADMIN);
        int totalPermissionCount = (int) permissionRepository.count();

        // Attempt to deactivate Super Admin and strip all its permissions.
        RoleRequest request = new RoleRequest(Role.SUPER_ADMIN, "attempted downgrade", false, List.of());

        mockMvc.perform(authJson(put("/api/v1/roles/" + superAdmin.getId()), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").value("Super Admin"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.permissions", hasSize(totalPermissionCount)));
    }

    @Test
    void updateUnknownRoleReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(put("/api/v1/roles/999999"), token)
                        .content(objectMapper.writeValueAsString(new RoleRequest("X", null, true, List.of()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletingASystemRoleIsRejected() throws Exception {
        String token = adminToken();
        Role operator = factory.role("Operator");

        mockMvc.perform(auth(delete("/api/v1/roles/" + operator.getId()), token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletingACustomRoleSucceeds() throws Exception {
        String token = adminToken();
        String roleName = TestEntityFactory.unique("DisposableRole");

        String created = mockMvc.perform(authJson(post("/api/v1/roles"), token)
                        .content(objectMapper.writeValueAsString(
                                new RoleRequest(roleName, null, true, List.of()))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(auth(delete("/api/v1/roles/" + id), token))
                .andExpect(status().isOk());

        mockMvc.perform(auth(get("/api/v1/roles/" + id), token))
                .andExpect(status().isNotFound());
    }

    @Test
    void requestsWithoutABearerTokenAreRejected() throws Exception {
        mockMvc.perform(get("/api/v1/roles")).andExpect(status().isUnauthorized());
    }

    @Test
    void operatorRoleLacksRoleMgmtPermissionAndIsForbidden() throws Exception {
        factory.createUser("operator-role1", "pass1234", "Operator");
        String token = login("operator-role1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/roles"), token)).andExpect(status().isForbidden());
    }

    @Test
    void storeManagerLacksRoleMgmtPermissionAndIsForbidden() throws Exception {
        factory.createUser("manager-role1", "pass1234", "Store Manager");
        String token = login("manager-role1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/roles"), token)).andExpect(status().isForbidden());
    }
}
