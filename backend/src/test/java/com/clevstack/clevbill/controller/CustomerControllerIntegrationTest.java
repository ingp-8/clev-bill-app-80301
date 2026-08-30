package com.clevstack.clevbill.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clevstack.clevbill.dto.CustomerRequest;
import com.clevstack.clevbill.model.Client;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.support.AbstractIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Full CRUD, validation, not-found, and permission-authorization coverage
 * for the client-scoped Customer master. Unlike Category/TaxRate/HsnCode,
 * Customer hangs off Client (not Property) — endpoints are
 * /api/v1/clients/{clientId}/customers for list/create and
 * /api/v1/customers/{id} for get/update/delete — so PropertyAccessInterceptor
 * (which only inspects a path variable literally named propertyId) never
 * applies here; there is deliberately no property-access test pair.
 */
class CustomerControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createThenListReturnsTheCustomerUnderItsClient() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();

        mockMvc.perform(authJson(post("/api/v1/clients/" + client.getId() + "/customers"), token)
                        .content(objectMapper.writeValueAsString(new CustomerRequest(
                                "Jane Doe", "9000000002", "jane@example.com", null, "1 Customer St", true,
                                List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.clientId").value(client.getId()))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.phone").value("9000000002"))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(auth(get("/api/v1/clients/" + client.getId() + "/customers"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Jane Doe"));
    }

    @Test
    void createRejectsBlankName() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();

        mockMvc.perform(authJson(post("/api/v1/clients/" + client.getId() + "/customers"), token)
                        .content(objectMapper.writeValueAsString(
                                new CustomerRequest("", null, null, null, null, true, List.of()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAgainstUnknownClientReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(post("/api/v1/clients/999999/customers"), token)
                        .content(objectMapper.writeValueAsString(
                                new CustomerRequest("Jane Doe", null, null, null, null, true, List.of()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateChangesNameAndActiveFlag() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();
        var customer = factory.createCustomer(client);

        mockMvc.perform(authJson(put("/api/v1/customers/" + customer.getId()), token)
                        .content(objectMapper.writeValueAsString(
                                new CustomerRequest("Renamed Customer", null, null, null, null, false, List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed Customer"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updateUnknownCustomerReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(put("/api/v1/customers/999999"), token)
                        .content(objectMapper.writeValueAsString(
                                new CustomerRequest("X", null, null, null, null, true, List.of()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRemovesTheCustomer() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();
        var customer = factory.createCustomer(client);

        mockMvc.perform(auth(delete("/api/v1/customers/" + customer.getId()), token))
                .andExpect(status().isOk());

        mockMvc.perform(auth(get("/api/v1/customers/" + customer.getId()), token))
                .andExpect(status().isNotFound());
    }

    @Test
    void requestsWithoutABearerTokenAreRejected() throws Exception {
        Client client = factory.createClient();

        mockMvc.perform(get("/api/v1/clients/" + client.getId() + "/customers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void operatorRoleLacksMastersPermissionAndIsForbidden() throws Exception {
        Client client = factory.createClient();
        factory.createUser("cust-operator1", "pass1234", "Operator");
        String token = login("cust-operator1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/clients/" + client.getId() + "/customers"), token))
                .andExpect(status().isForbidden());
    }

    @Test
    void createWithGstinIsStored() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();

        mockMvc.perform(authJson(post("/api/v1/clients/" + client.getId() + "/customers"), token)
                        .content(objectMapper.writeValueAsString(new CustomerRequest(
                                "GST Customer", null, null, "27AAAAA0000A1Z5", null, true, List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gstin").value("27AAAAA0000A1Z5"));
    }

    @Test
    void createWithoutGstinLeavesItNull() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();

        mockMvc.perform(authJson(post("/api/v1/clients/" + client.getId() + "/customers"), token)
                        .content(objectMapper.writeValueAsString(
                                new CustomerRequest("No GST Customer", null, null, null, null, true, List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gstin").doesNotExist());
    }

    @Test
    void createWithPropertyIdGrantsAccessReflectedInResponse() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();
        Property property = factory.createProperty(client);

        mockMvc.perform(authJson(post("/api/v1/clients/" + client.getId() + "/customers"), token)
                        .content(objectMapper.writeValueAsString(new CustomerRequest(
                                "Scoped Customer", null, null, null, null, true, List.of(property.getId())))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propertyIds", hasSize(1)))
                .andExpect(jsonPath("$.propertyIds[0]").value(property.getId()));
    }

    @Test
    void createWithPropertyIdFromDifferentClientReturnsBadRequest() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();
        Client otherClient = factory.createClient();
        Property otherProperty = factory.createProperty(otherClient);

        mockMvc.perform(authJson(post("/api/v1/clients/" + client.getId() + "/customers"), token)
                        .content(objectMapper.writeValueAsString(new CustomerRequest(
                                "Cross Client Customer", null, null, null, null, true,
                                List.of(otherProperty.getId())))))
                .andExpect(status().isBadRequest());
    }
}
