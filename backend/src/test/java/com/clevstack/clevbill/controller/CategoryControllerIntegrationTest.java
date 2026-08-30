package com.clevstack.clevbill.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clevstack.clevbill.dto.CategoryRequest;
import com.clevstack.clevbill.model.Client;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Full CRUD, validation, not-found, and both authorization dimensions
 * (permission via @PreAuthorize authority, property access via
 * PropertyAccessInterceptor) for the property-scoped Category master —
 * same shape as Brand.
 */
class CategoryControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createThenListReturnsTheCategoryUnderItsProperty() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();
        Property property = factory.createProperty(client);

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/categories"), token)
                        .content(objectMapper.writeValueAsString(new CategoryRequest("Beverages", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.propertyId").value(property.getId()))
                .andExpect(jsonPath("$.clientId").value(client.getId()))
                .andExpect(jsonPath("$.name").value("Beverages"))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/categories"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Beverages"));
    }

    @Test
    void createRejectsBlankName() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/categories"), token)
                        .content(objectMapper.writeValueAsString(new CategoryRequest("", true))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAgainstUnknownPropertyReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(post("/api/v1/properties/999999/categories"), token)
                        .content(objectMapper.writeValueAsString(new CategoryRequest("Beverages", true))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateChangesNameAndActiveFlag() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        var category = factory.createCategory(property);

        mockMvc.perform(authJson(put("/api/v1/categories/" + category.getId()), token)
                        .content(objectMapper.writeValueAsString(new CategoryRequest("Renamed", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updateUnknownCategoryReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(put("/api/v1/categories/999999"), token)
                        .content(objectMapper.writeValueAsString(new CategoryRequest("X", true))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRemovesTheCategory() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        var category = factory.createCategory(property);

        mockMvc.perform(auth(delete("/api/v1/categories/" + category.getId()), token))
                .andExpect(status().isOk());

        mockMvc.perform(auth(get("/api/v1/categories/" + category.getId()), token))
                .andExpect(status().isNotFound());
    }

    @Test
    void requestsWithoutABearerTokenAreRejected() throws Exception {
        Property property = factory.createProperty(factory.createClient());

        mockMvc.perform(get("/api/v1/properties/" + property.getId() + "/categories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void operatorRoleLacksMastersPermissionAndIsForbidden() throws Exception {
        Property property = factory.createProperty(factory.createClient());
        factory.createUser("cat-operator1", "pass1234", "Operator");
        String token = login("cat-operator1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/categories"), token))
                .andExpect(status().isForbidden());
    }

    @Test
    void storeManagerWithoutPropertyAccessIsForbiddenByPropertyAccessInterceptor() throws Exception {
        Property property = factory.createProperty(factory.createClient());
        // Store Manager has MASTERS_CATEGORY:VIEW, but is never granted access to this property.
        factory.createUser("cat-manager1", "pass1234", "Store Manager");
        String token = login("cat-manager1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/categories"), token))
                .andExpect(status().isForbidden());
    }

    @Test
    void storeManagerWithPropertyAccessCanListCategories() throws Exception {
        Property property = factory.createProperty(factory.createClient());
        factory.createCategory(property);
        var manager = factory.createUser("cat-manager2", "pass1234", "Store Manager");
        factory.grantAccess(manager, property);
        String token = login("cat-manager2", "pass1234");

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/categories"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void createWithActiveFalseIsStoredAsInactive() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/categories"), token)
                        .content(objectMapper.writeValueAsString(new CategoryRequest("Discontinued", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }
}
