package com.clevstack.clevbill.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clevstack.clevbill.dto.HsnCodeRequest;
import com.clevstack.clevbill.model.Client;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Full CRUD, validation, not-found, and both authorization dimensions
 * (permission via @PreAuthorize authority, property access via
 * PropertyAccessInterceptor) for the property-scoped HsnCode master —
 * same shape as Brand, plus the 4/6/8-digit code format rule.
 */
class HsnCodeControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createThenListReturnsTheHsnCodeUnderItsProperty() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();
        Property property = factory.createProperty(client);

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/hsn-codes"), token)
                        .content(objectMapper.writeValueAsString(new HsnCodeRequest("1006", "Rice", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.propertyId").value(property.getId()))
                .andExpect(jsonPath("$.clientId").value(client.getId()))
                .andExpect(jsonPath("$.code").value("1006"))
                .andExpect(jsonPath("$.description").value("Rice"))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/hsn-codes"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code").value("1006"));
    }

    @Test
    void createRejectsInvalidCodeFormat() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/hsn-codes"), token)
                        .content(objectMapper.writeValueAsString(new HsnCodeRequest("123", "Bad code", true))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAgainstUnknownPropertyReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(post("/api/v1/properties/999999/hsn-codes"), token)
                        .content(objectMapper.writeValueAsString(new HsnCodeRequest("1006", "Rice", true))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateChangesCodeAndActiveFlag() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        var hsnCode = factory.createHsnCode(property);

        mockMvc.perform(authJson(put("/api/v1/hsn-codes/" + hsnCode.getId()), token)
                        .content(objectMapper.writeValueAsString(new HsnCodeRequest("2106", "Food prep", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("2106"))
                .andExpect(jsonPath("$.description").value("Food prep"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updateUnknownHsnCodeReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(put("/api/v1/hsn-codes/999999"), token)
                        .content(objectMapper.writeValueAsString(new HsnCodeRequest("1006", "Rice", true))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRemovesTheHsnCode() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        var hsnCode = factory.createHsnCode(property);

        mockMvc.perform(auth(delete("/api/v1/hsn-codes/" + hsnCode.getId()), token))
                .andExpect(status().isOk());

        mockMvc.perform(auth(get("/api/v1/hsn-codes/" + hsnCode.getId()), token))
                .andExpect(status().isNotFound());
    }

    @Test
    void requestsWithoutABearerTokenAreRejected() throws Exception {
        Property property = factory.createProperty(factory.createClient());

        mockMvc.perform(get("/api/v1/properties/" + property.getId() + "/hsn-codes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void operatorRoleLacksMastersPermissionAndIsForbidden() throws Exception {
        Property property = factory.createProperty(factory.createClient());
        factory.createUser("hsn-operator1", "pass1234", "Operator");
        String token = login("hsn-operator1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/hsn-codes"), token))
                .andExpect(status().isForbidden());
    }

    @Test
    void storeManagerWithoutPropertyAccessIsForbiddenByPropertyAccessInterceptor() throws Exception {
        Property property = factory.createProperty(factory.createClient());
        // Store Manager has MASTERS_HSN:VIEW, but is never granted access to this property.
        factory.createUser("hsn-manager1", "pass1234", "Store Manager");
        String token = login("hsn-manager1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/hsn-codes"), token))
                .andExpect(status().isForbidden());
    }

    @Test
    void storeManagerWithPropertyAccessCanListHsnCodes() throws Exception {
        Property property = factory.createProperty(factory.createClient());
        factory.createHsnCode(property);
        var manager = factory.createUser("hsn-manager2", "pass1234", "Store Manager");
        factory.grantAccess(manager, property);
        String token = login("hsn-manager2", "pass1234");

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/hsn-codes"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void createAcceptsFourDigitCode() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/hsn-codes"), token)
                        .content(objectMapper.writeValueAsString(new HsnCodeRequest("1006", "Rice", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("1006"));
    }

    @Test
    void createAcceptsEightDigitCode() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/hsn-codes"), token)
                        .content(objectMapper.writeValueAsString(new HsnCodeRequest("10063000", "Rice, husked", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("10063000"));
    }
}
