package com.clevstack.clevbill.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clevstack.clevbill.dto.TaxRateRequest;
import com.clevstack.clevbill.model.Client;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Full CRUD, validation, not-found, and both authorization dimensions
 * (permission via @PreAuthorize authority, property access via
 * PropertyAccessInterceptor) for the property-scoped TaxRate master —
 * same shape as Brand, plus BigDecimal rate fields.
 */
class TaxRateControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createThenListReturnsTheTaxRateUnderItsProperty() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();
        Property property = factory.createProperty(client);

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/tax-rates"), token)
                        .content(objectMapper.writeValueAsString(new TaxRateRequest(
                                "GST 18%",
                                new BigDecimal("9.00"),
                                new BigDecimal("9.00"),
                                new BigDecimal("18.00"),
                                true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.propertyId").value(property.getId()))
                .andExpect(jsonPath("$.clientId").value(client.getId()))
                .andExpect(jsonPath("$.name").value("GST 18%"))
                .andExpect(jsonPath("$.cgstRate").value(9.0))
                .andExpect(jsonPath("$.sgstRate").value(9.0))
                .andExpect(jsonPath("$.igstRate").value(18.0))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/tax-rates"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("GST 18%"));
    }

    @Test
    void createRejectsBlankName() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/tax-rates"), token)
                        .content(objectMapper.writeValueAsString(new TaxRateRequest(
                                "",
                                new BigDecimal("9.00"),
                                new BigDecimal("9.00"),
                                new BigDecimal("18.00"),
                                true))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRejectsNegativeRate() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/tax-rates"), token)
                        .content(objectMapper.writeValueAsString(new TaxRateRequest(
                                "Bad Rate",
                                new BigDecimal("-1.00"),
                                new BigDecimal("9.00"),
                                new BigDecimal("18.00"),
                                true))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAgainstUnknownPropertyReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(post("/api/v1/properties/999999/tax-rates"), token)
                        .content(objectMapper.writeValueAsString(new TaxRateRequest(
                                "GST 18%",
                                new BigDecimal("9.00"),
                                new BigDecimal("9.00"),
                                new BigDecimal("18.00"),
                                true))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateChangesRatesAndActiveFlag() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        var taxRate = factory.createTaxRate(property);

        mockMvc.perform(authJson(put("/api/v1/tax-rates/" + taxRate.getId()), token)
                        .content(objectMapper.writeValueAsString(new TaxRateRequest(
                                "GST 5%",
                                new BigDecimal("2.50"),
                                new BigDecimal("2.50"),
                                new BigDecimal("5.00"),
                                false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("GST 5%"))
                .andExpect(jsonPath("$.cgstRate").value(2.5))
                .andExpect(jsonPath("$.sgstRate").value(2.5))
                .andExpect(jsonPath("$.igstRate").value(5.0))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updateUnknownTaxRateReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(put("/api/v1/tax-rates/999999"), token)
                        .content(objectMapper.writeValueAsString(new TaxRateRequest(
                                "X",
                                new BigDecimal("9.00"),
                                new BigDecimal("9.00"),
                                new BigDecimal("18.00"),
                                true))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRemovesTheTaxRate() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        var taxRate = factory.createTaxRate(property);

        mockMvc.perform(auth(delete("/api/v1/tax-rates/" + taxRate.getId()), token))
                .andExpect(status().isOk());

        mockMvc.perform(auth(get("/api/v1/tax-rates/" + taxRate.getId()), token))
                .andExpect(status().isNotFound());
    }

    @Test
    void requestsWithoutABearerTokenAreRejected() throws Exception {
        Property property = factory.createProperty(factory.createClient());

        mockMvc.perform(get("/api/v1/properties/" + property.getId() + "/tax-rates"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void operatorRoleLacksMastersPermissionAndIsForbidden() throws Exception {
        Property property = factory.createProperty(factory.createClient());
        factory.createUser("tax-operator1", "pass1234", "Operator");
        String token = login("tax-operator1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/tax-rates"), token))
                .andExpect(status().isForbidden());
    }

    @Test
    void storeManagerWithoutPropertyAccessIsForbiddenByPropertyAccessInterceptor() throws Exception {
        Property property = factory.createProperty(factory.createClient());
        // Store Manager has MASTERS_TAX_RATE:VIEW, but is never granted access to this property.
        factory.createUser("tax-manager1", "pass1234", "Store Manager");
        String token = login("tax-manager1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/tax-rates"), token))
                .andExpect(status().isForbidden());
    }

    @Test
    void storeManagerWithPropertyAccessCanListTaxRates() throws Exception {
        Property property = factory.createProperty(factory.createClient());
        factory.createTaxRate(property);
        var manager = factory.createUser("tax-manager2", "pass1234", "Store Manager");
        factory.grantAccess(manager, property);
        String token = login("tax-manager2", "pass1234");

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/tax-rates"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void createWithZeroRatesSucceeds() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/tax-rates"), token)
                        .content(objectMapper.writeValueAsString(new TaxRateRequest(
                                "GST Exempt",
                                new BigDecimal("0.00"),
                                new BigDecimal("0.00"),
                                new BigDecimal("0.00"),
                                true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cgstRate").value(0.0))
                .andExpect(jsonPath("$.sgstRate").value(0.0))
                .andExpect(jsonPath("$.igstRate").value(0.0));
    }
}
