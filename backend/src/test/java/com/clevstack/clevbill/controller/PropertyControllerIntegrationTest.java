package com.clevstack.clevbill.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clevstack.clevbill.dto.PropertyRequest;
import com.clevstack.clevbill.model.Client;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Property's nested route uses {@code clientId} and its bare-id routes use
 * {@code id} — neither matches the {@code propertyId} variable name
 * PropertyAccessInterceptor keys off, so it never actually fires for any
 * Property endpoint. Only the PROPERTY_MGMT permission dimension is
 * exercised here, deliberately no property-access-interceptor test.
 */
class PropertyControllerIntegrationTest extends AbstractIntegrationTest {

    private PropertyRequest validRequest(String seriesPrefix) {
        return new PropertyRequest(
                "Downtown Store",
                "1 Test Street",
                "27AAAAA0000A1Z5",
                seriesPrefix,
                new BigDecimal("9.00"),
                new BigDecimal("9.00"),
                new BigDecimal("18.00"),
                false,
                true);
    }

    @Test
    void createThenListReturnsThePropertyUnderItsClient() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();

        mockMvc.perform(authJson(post("/api/v1/clients/" + client.getId() + "/properties"), token)
                        .content(objectMapper.writeValueAsString(validRequest("DTS"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.clientId").value(client.getId()))
                .andExpect(jsonPath("$.propertyName").value("Downtown Store"))
                .andExpect(jsonPath("$.invoiceSeriesPrefix").value("DTS"))
                .andExpect(jsonPath("$.defaultCgstRate").value(9.00))
                .andExpect(jsonPath("$.eInvoiceEnabled").value(false))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(auth(get("/api/v1/clients/" + client.getId() + "/properties"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].propertyName").value("Downtown Store"));
    }

    @Test
    void createRejectsBlankPropertyName() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();

        PropertyRequest bad = new PropertyRequest(
                "",
                "addr",
                "27AAAAA0000A1Z5",
                "DTS",
                new BigDecimal("9.00"),
                new BigDecimal("9.00"),
                new BigDecimal("18.00"),
                false,
                true);

        mockMvc.perform(authJson(post("/api/v1/clients/" + client.getId() + "/properties"), token)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRejectsMissingInvoiceSeriesPrefix() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();

        PropertyRequest bad = new PropertyRequest(
                "Store",
                "addr",
                "27AAAAA0000A1Z5",
                "",
                new BigDecimal("9.00"),
                new BigDecimal("9.00"),
                new BigDecimal("18.00"),
                false,
                true);

        mockMvc.perform(authJson(post("/api/v1/clients/" + client.getId() + "/properties"), token)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRejectsMissingDefaultCgstRate() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();

        PropertyRequest bad = new PropertyRequest(
                "Store",
                "addr",
                "27AAAAA0000A1Z5",
                "DTS",
                null,
                new BigDecimal("9.00"),
                new BigDecimal("18.00"),
                false,
                true);

        mockMvc.perform(authJson(post("/api/v1/clients/" + client.getId() + "/properties"), token)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAgainstUnknownClientReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(post("/api/v1/clients/999999/properties"), token)
                        .content(objectMapper.writeValueAsString(validRequest("DTS"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getReturnsTheProperty() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId()), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(property.getId()));
    }

    @Test
    void getUnknownPropertyReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(auth(get("/api/v1/properties/999999"), token))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateChangesPropertyFields() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());

        mockMvc.perform(authJson(put("/api/v1/properties/" + property.getId()), token)
                        .content(objectMapper.writeValueAsString(validRequest("NEW"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propertyName").value("Downtown Store"))
                .andExpect(jsonPath("$.invoiceSeriesPrefix").value("NEW"));
    }

    @Test
    void updateUnknownPropertyReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(put("/api/v1/properties/999999"), token)
                        .content(objectMapper.writeValueAsString(validRequest("DTS"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRemovesTheProperty() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());

        mockMvc.perform(auth(delete("/api/v1/properties/" + property.getId()), token))
                .andExpect(status().isOk());

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId()), token))
                .andExpect(status().isNotFound());
    }

    @Test
    void requestsWithoutABearerTokenAreRejected() throws Exception {
        Client client = factory.createClient();

        mockMvc.perform(get("/api/v1/clients/" + client.getId() + "/properties"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void operatorRoleLacksPropertyMgmtPermissionAndIsForbidden() throws Exception {
        Client client = factory.createClient();
        factory.createUser("operator-prop1", "pass1234", "Operator");
        String token = login("operator-prop1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/clients/" + client.getId() + "/properties"), token))
                .andExpect(status().isForbidden());
    }

    @Test
    void storeManagerLacksPropertyMgmtPermissionAndIsForbidden() throws Exception {
        Client client = factory.createClient();
        factory.createUser("manager-prop1", "pass1234", "Store Manager");
        String token = login("manager-prop1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/clients/" + client.getId() + "/properties"), token))
                .andExpect(status().isForbidden());
    }
}
