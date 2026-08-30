package com.clevstack.clevbill.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clevstack.clevbill.dto.BrandRequest;
import com.clevstack.clevbill.model.Client;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Exemplar for the "simple property-scoped master" family (Category, Brand,
 * TaxRate, HsnCode all follow this exact controller/service shape) —
 * full CRUD, validation, not-found, and both authorization dimensions this
 * system has: permission (@PreAuthorize authority) and property access
 * (PropertyAccessInterceptor).
 */
class BrandControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createThenListReturnsTheBrandUnderItsProperty() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();
        Property property = factory.createProperty(client);

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/brands"), token)
                        .content(objectMapper.writeValueAsString(new BrandRequest("Acme", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.propertyId").value(property.getId()))
                .andExpect(jsonPath("$.clientId").value(client.getId()))
                .andExpect(jsonPath("$.name").value("Acme"))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/brands"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Acme"));
    }

    @Test
    void createRejectsBlankName() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/brands"), token)
                        .content(objectMapper.writeValueAsString(new BrandRequest("", true))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAgainstUnknownPropertyReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(post("/api/v1/properties/999999/brands"), token)
                        .content(objectMapper.writeValueAsString(new BrandRequest("Acme", true))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateChangesNameAndActiveFlag() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        var brand = factory.createBrand(property);

        mockMvc.perform(authJson(put("/api/v1/brands/" + brand.getId()), token)
                        .content(objectMapper.writeValueAsString(new BrandRequest("Renamed", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updateUnknownBrandReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(put("/api/v1/brands/999999"), token)
                        .content(objectMapper.writeValueAsString(new BrandRequest("X", true))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRemovesTheBrand() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        var brand = factory.createBrand(property);

        mockMvc.perform(auth(delete("/api/v1/brands/" + brand.getId()), token))
                .andExpect(status().isOk());

        mockMvc.perform(auth(get("/api/v1/brands/" + brand.getId()), token))
                .andExpect(status().isNotFound());
    }

    @Test
    void requestsWithoutABearerTokenAreRejected() throws Exception {
        Property property = factory.createProperty(factory.createClient());

        mockMvc.perform(get("/api/v1/properties/" + property.getId() + "/brands"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void operatorRoleLacksMastersPermissionAndIsForbidden() throws Exception {
        Property property = factory.createProperty(factory.createClient());
        factory.createUser("operator1", "pass1234", "Operator");
        String token = login("operator1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/brands"), token))
                .andExpect(status().isForbidden());
    }

    @Test
    void storeManagerWithoutPropertyAccessIsForbiddenByPropertyAccessInterceptor() throws Exception {
        Property property = factory.createProperty(factory.createClient());
        // Store Manager has MASTERS_BRAND:VIEW, but is never granted access to this property.
        factory.createUser("manager1", "pass1234", "Store Manager");
        String token = login("manager1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/brands"), token))
                .andExpect(status().isForbidden());
    }

    @Test
    void storeManagerWithPropertyAccessCanListBrands() throws Exception {
        Property property = factory.createProperty(factory.createClient());
        factory.createBrand(property);
        var manager = factory.createUser("manager2", "pass1234", "Store Manager");
        factory.grantAccess(manager, property);
        String token = login("manager2", "pass1234");

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/brands"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
