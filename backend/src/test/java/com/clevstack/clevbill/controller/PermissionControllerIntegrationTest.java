package com.clevstack.clevbill.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clevstack.clevbill.repository.PermissionRepository;
import com.clevstack.clevbill.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Permission is a read-only catalog (fixed by the module list seeded in
 * V14/V20, not user-editable) exposed via {@code GET /api/v1/permissions},
 * guarded by ROLE_MGMT:VIEW rather than its own module.
 */
class PermissionControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PermissionRepository permissionRepository;

    @Test
    void listReturnsEverySeededPermission() throws Exception {
        String token = adminToken();
        int expectedCount = (int) permissionRepository.count();

        mockMvc.perform(auth(get("/api/v1/permissions"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(expectedCount)))
                .andExpect(jsonPath("$[?(@.moduleCode == 'MASTERS_ITEM')]").exists())
                .andExpect(jsonPath("$[?(@.moduleCode == 'BILLING')]").exists())
                .andExpect(jsonPath("$[?(@.moduleCode == 'MASTERS_HSN')]").exists())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].action").exists());
    }

    @Test
    void requestsWithoutABearerTokenAreRejected() throws Exception {
        mockMvc.perform(get("/api/v1/permissions")).andExpect(status().isUnauthorized());
    }

    @Test
    void operatorRoleLacksRoleMgmtViewPermissionAndIsForbidden() throws Exception {
        factory.createUser("operator-perm1", "pass1234", "Operator");
        String token = login("operator-perm1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/permissions"), token)).andExpect(status().isForbidden());
    }

    @Test
    void storeManagerLacksRoleMgmtViewPermissionAndIsForbidden() throws Exception {
        factory.createUser("manager-perm1", "pass1234", "Store Manager");
        String token = login("manager-perm1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/permissions"), token)).andExpect(status().isForbidden());
    }
}
