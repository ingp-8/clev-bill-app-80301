package com.clevstack.clevbill.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clevstack.clevbill.dto.PriceListRequest;
import com.clevstack.clevbill.model.Client;
import com.clevstack.clevbill.model.PriceList;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Full CRUD coverage for the PriceList master: property-scoped list/create,
 * bare-id get/update/delete, validation, the (property_id, name) unique
 * constraint, and both authorization dimensions (permission +
 * PropertyAccessInterceptor), following the pattern in
 * BrandControllerIntegrationTest.
 */
class PriceListControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createThenListReturnsThePriceListUnderItsProperty() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();
        Property property = factory.createProperty(client);

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/price-lists"), token)
                        .content(objectMapper.writeValueAsString(new PriceListRequest("Retail", true, true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.propertyId").value(property.getId()))
                .andExpect(jsonPath("$.clientId").value(client.getId()))
                .andExpect(jsonPath("$.name").value("Retail"))
                .andExpect(jsonPath("$.isDefault").value(true))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/price-lists"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Retail"));
    }

    @Test
    void createRejectsBlankName() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/price-lists"), token)
                        .content(objectMapper.writeValueAsString(new PriceListRequest("", false, true))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRejectsMissingIsDefault() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        String body = "{\"name\":\"Retail\",\"active\":true}";

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/price-lists"), token)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAgainstUnknownPropertyReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(post("/api/v1/properties/999999/price-lists"), token)
                        .content(objectMapper.writeValueAsString(new PriceListRequest("Retail", false, true))))
                .andExpect(status().isNotFound());
    }

    @Test
    void createRejectsDuplicateNameWithinSameProperty() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/price-lists"), token)
                        .content(objectMapper.writeValueAsString(new PriceListRequest("Wholesale", false, true))))
                .andExpect(status().isOk());

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/price-lists"), token)
                        .content(objectMapper.writeValueAsString(new PriceListRequest("Wholesale", false, true))))
                .andExpect(status().isConflict());
    }

    @Test
    void sameNameAllowedAcrossDifferentProperties() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();
        Property propertyA = factory.createProperty(client);
        Property propertyB = factory.createProperty(client);

        mockMvc.perform(authJson(post("/api/v1/properties/" + propertyA.getId() + "/price-lists"), token)
                        .content(objectMapper.writeValueAsString(new PriceListRequest("Retail", false, true))))
                .andExpect(status().isOk());

        mockMvc.perform(authJson(post("/api/v1/properties/" + propertyB.getId() + "/price-lists"), token)
                        .content(objectMapper.writeValueAsString(new PriceListRequest("Retail", false, true))))
                .andExpect(status().isOk());
    }

    @Test
    void updateChangesNameDefaultAndActiveFlags() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        PriceList priceList = factory.createPriceList(property);

        mockMvc.perform(authJson(put("/api/v1/price-lists/" + priceList.getId()), token)
                        .content(objectMapper.writeValueAsString(new PriceListRequest("Renamed", true, false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed"))
                .andExpect(jsonPath("$.isDefault").value(true))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updateUnknownPriceListReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(put("/api/v1/price-lists/999999"), token)
                        .content(objectMapper.writeValueAsString(new PriceListRequest("X", false, true))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRemovesThePriceList() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        PriceList priceList = factory.createPriceList(property);

        mockMvc.perform(auth(delete("/api/v1/price-lists/" + priceList.getId()), token))
                .andExpect(status().isOk());

        mockMvc.perform(auth(get("/api/v1/price-lists/" + priceList.getId()), token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUnknownPriceListReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(auth(delete("/api/v1/price-lists/999999"), token))
                .andExpect(status().isNotFound());
    }

    @Test
    void requestsWithoutABearerTokenAreRejected() throws Exception {
        Property property = factory.createProperty(factory.createClient());

        mockMvc.perform(get("/api/v1/properties/" + property.getId() + "/price-lists"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void operatorRoleLacksMastersPermissionAndIsForbidden() throws Exception {
        Property property = factory.createProperty(factory.createClient());
        factory.createUser("plop1", "pass1234", "Operator");
        String token = login("plop1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/price-lists"), token))
                .andExpect(status().isForbidden());
    }

    @Test
    void storeManagerWithoutPropertyAccessIsForbiddenByPropertyAccessInterceptor() throws Exception {
        Property property = factory.createProperty(factory.createClient());
        factory.createUser("plmgr1", "pass1234", "Store Manager");
        String token = login("plmgr1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/price-lists"), token))
                .andExpect(status().isForbidden());
    }

    @Test
    void storeManagerWithPropertyAccessCanListPriceLists() throws Exception {
        Property property = factory.createProperty(factory.createClient());
        factory.createPriceList(property);
        var manager = factory.createUser("plmgr2", "pass1234", "Store Manager");
        factory.grantAccess(manager, property);
        String token = login("plmgr2", "pass1234");

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/price-lists"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
