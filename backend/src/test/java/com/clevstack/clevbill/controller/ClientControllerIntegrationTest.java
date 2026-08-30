package com.clevstack.clevbill.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clevstack.clevbill.dto.ClientRequest;
import com.clevstack.clevbill.model.Client;
import com.clevstack.clevbill.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Client is a fully global master (no property scoping) guarded only by
 * CLIENT_MGMT:&lt;ACTION&gt; — no PropertyAccessInterceptor involvement
 * since no endpoint carries a {@code propertyId} path variable.
 */
class ClientControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createThenListReturnsTheClient() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(post("/api/v1/clients"), token)
                        .content(objectMapper.writeValueAsString(
                                new ClientRequest("Acme Retail", "1 Main St", "acme@example.com", "9000000000", null, true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.clientName").value("Acme Retail"))
                .andExpect(jsonPath("$.address").value("1 Main St"))
                .andExpect(jsonPath("$.email").value("acme@example.com"))
                .andExpect(jsonPath("$.mobileNo").value("9000000000"))
                .andExpect(jsonPath("$.active").value(true));

        // Client list is global (unscoped), and a default "Clevbill Store" client
        // already exists from the V2 business-settings migration — so the list is
        // never empty; assert the new client is present rather than an exact size.
        mockMvc.perform(auth(get("/api/v1/clients"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[?(@.clientName == 'Acme Retail')]").exists());
    }

    @Test
    void getReturnsTheCreatedClient() throws Exception {
        String token = adminToken();
        Client client = factory.createClient("Direct Client");

        mockMvc.perform(auth(get("/api/v1/clients/" + client.getId()), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(client.getId()))
                .andExpect(jsonPath("$.clientName").value("Direct Client"));
    }

    @Test
    void createRejectsBlankClientName() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(post("/api/v1/clients"), token)
                        .content(objectMapper.writeValueAsString(
                                new ClientRequest("", null, null, null, null, true))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUnknownClientReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(auth(get("/api/v1/clients/999999"), token))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateChangesClientFields() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();

        mockMvc.perform(authJson(put("/api/v1/clients/" + client.getId()), token)
                        .content(objectMapper.writeValueAsString(
                                new ClientRequest("Renamed Co", "New Addr", "new@example.com", "9111111111", null, false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientName").value("Renamed Co"))
                .andExpect(jsonPath("$.address").value("New Addr"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updateUnknownClientReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(put("/api/v1/clients/999999"), token)
                        .content(objectMapper.writeValueAsString(
                                new ClientRequest("X", null, null, null, null, true))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRemovesTheClient() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();

        mockMvc.perform(auth(delete("/api/v1/clients/" + client.getId()), token))
                .andExpect(status().isOk());

        mockMvc.perform(auth(get("/api/v1/clients/" + client.getId()), token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUnknownClientReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(auth(delete("/api/v1/clients/999999"), token))
                .andExpect(status().isNotFound());
    }

    @Test
    void requestsWithoutABearerTokenAreRejected() throws Exception {
        mockMvc.perform(get("/api/v1/clients")).andExpect(status().isUnauthorized());
    }

    @Test
    void operatorRoleLacksClientMgmtPermissionAndIsForbidden() throws Exception {
        factory.createUser("operator-client1", "pass1234", "Operator");
        String token = login("operator-client1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/clients"), token)).andExpect(status().isForbidden());
    }

    @Test
    void storeManagerLacksClientMgmtPermissionAndIsForbidden() throws Exception {
        // Store Manager (per V14 migration) is explicitly excluded from
        // CLIENT_MGMT, PROPERTY_MGMT, POS_MGMT, USER_MGMT, ROLE_MGMT.
        factory.createUser("manager-client1", "pass1234", "Store Manager");
        String token = login("manager-client1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/clients"), token)).andExpect(status().isForbidden());
    }
}
