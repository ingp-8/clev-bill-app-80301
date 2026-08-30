package com.clevstack.clevbill.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clevstack.clevbill.dto.PosTerminalRequest;
import com.clevstack.clevbill.model.Client;
import com.clevstack.clevbill.model.PosTerminal;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.model.Role;
import com.clevstack.clevbill.repository.PermissionRepository;
import com.clevstack.clevbill.repository.RoleRepository;
import com.clevstack.clevbill.support.AbstractIntegrationTest;
import com.clevstack.clevbill.support.TestEntityFactory;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Unlike the other administrative masters in this group, PosTerminal's
 * list/create routes are nested under {@code /api/v1/properties/{propertyId}/pos}
 * — a literal {@code propertyId} path variable — so PropertyAccessInterceptor
 * DOES apply here. None of the three seeded roles carry POS_MGMT except
 * Super Admin (who bypasses the interceptor entirely), so a custom role is
 * built directly through the repositories to exercise the interceptor with
 * a non-super-admin, POS_MGMT-permitted user.
 */
class PosTerminalControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    private Role createPosManagerRole() {
        Role role = new Role();
        role.setRoleName(TestEntityFactory.unique("PosManagerRole"));
        role.setDescription("test-only role with POS_MGMT permissions");
        role.setActive(true);
        role.setSystem(false);
        Set<com.clevstack.clevbill.model.Permission> posPermissions = permissionRepository.findAll().stream()
                .filter(p -> p.getModuleCode().equals("POS_MGMT"))
                .collect(Collectors.toSet());
        role.setPermissions(posPermissions);
        return roleRepository.save(role);
    }

    @Test
    void createThenListReturnsThePosTerminalUnderItsProperty() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();
        Property property = factory.createProperty(client);

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/pos"), token)
                        .content(objectMapper.writeValueAsString(new PosTerminalRequest("Till 1", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.propertyId").value(property.getId()))
                .andExpect(jsonPath("$.posName").value("Till 1"))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/pos"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].posName").value("Till 1"));
    }

    @Test
    void createRejectsBlankPosName() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/pos"), token)
                        .content(objectMapper.writeValueAsString(new PosTerminalRequest("", true))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAgainstUnknownPropertyReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(post("/api/v1/properties/999999/pos"), token)
                        .content(objectMapper.writeValueAsString(new PosTerminalRequest("Till 1", true))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateChangesPosNameAndActiveFlag() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        PosTerminal pos = factory.createPosTerminal(property);

        mockMvc.perform(authJson(put("/api/v1/pos/" + pos.getId()), token)
                        .content(objectMapper.writeValueAsString(new PosTerminalRequest("Renamed Till", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posName").value("Renamed Till"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updateUnknownPosTerminalReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(put("/api/v1/pos/999999"), token)
                        .content(objectMapper.writeValueAsString(new PosTerminalRequest("X", true))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRemovesThePosTerminal() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        PosTerminal pos = factory.createPosTerminal(property);

        mockMvc.perform(auth(delete("/api/v1/pos/" + pos.getId()), token))
                .andExpect(status().isOk());

        // No bare-id GET exists for PosTerminal; verify via the list endpoint instead.
        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/pos"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void requestsWithoutABearerTokenAreRejected() throws Exception {
        Property property = factory.createProperty(factory.createClient());

        mockMvc.perform(get("/api/v1/properties/" + property.getId() + "/pos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void operatorRoleLacksPosMgmtPermissionAndIsForbidden() throws Exception {
        Property property = factory.createProperty(factory.createClient());
        factory.createUser("operator-pos1", "pass1234", "Operator");
        String token = login("operator-pos1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/pos"), token))
                .andExpect(status().isForbidden());
    }

    @Test
    void storeManagerLacksPosMgmtPermissionAndIsForbiddenEvenWithPropertyAccess() throws Exception {
        // Store Manager (per V14 migration) is explicitly excluded from POS_MGMT,
        // so this must fail on the @PreAuthorize check even though property access is granted.
        Property property = factory.createProperty(factory.createClient());
        var manager = factory.createUser("manager-pos1", "pass1234", "Store Manager");
        factory.grantAccess(manager, property);
        String token = login("manager-pos1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/pos"), token))
                .andExpect(status().isForbidden());
    }

    @Test
    void posManagerWithoutPropertyAccessIsForbiddenByPropertyAccessInterceptor() throws Exception {
        Property property = factory.createProperty(factory.createClient());
        Role posManagerRole = createPosManagerRole();
        factory.createUser("posmgr1", "pass1234", posManagerRole.getRoleName());
        String token = login("posmgr1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/pos"), token))
                .andExpect(status().isForbidden());
    }

    @Test
    void posManagerWithPropertyAccessCanListPosTerminals() throws Exception {
        Property property = factory.createProperty(factory.createClient());
        factory.createPosTerminal(property);
        Role posManagerRole = createPosManagerRole();
        var user = factory.createUser("posmgr2", "pass1234", posManagerRole.getRoleName());
        factory.grantAccess(user, property);
        String token = login("posmgr2", "pass1234");

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/pos"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
