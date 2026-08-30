package com.clevstack.clevbill.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clevstack.clevbill.dto.InventoryAdjustmentRequest;
import com.clevstack.clevbill.model.HsnCode;
import com.clevstack.clevbill.model.Item;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.model.TaxRate;
import com.clevstack.clevbill.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * The inventory ledger is append-only (docs/ARCHITECTURE.md §5) — every
 * change, whatever the reason, goes through POST /adjustments or a
 * checkout/return, never a direct write to the cached quantity. These
 * tests cover the manual-adjustment path directly; the SALE/RETURN reasons
 * are covered end-to-end in SaleControllerIntegrationTest and
 * ReturnControllerIntegrationTest.
 */
class InventoryControllerIntegrationTest extends AbstractIntegrationTest {

    private Item newItem() {
        var client = factory.createClient();
        Property property = factory.createProperty(client);
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode hsn = factory.createHsnCode(property);
        return factory.createItem(property, taxRate, hsn, new BigDecimal("50.00"));
    }

    @Test
    void itemWithNoMovementHasZeroStock() throws Exception {
        String token = adminToken();
        Item item = newItem();

        mockMvc.perform(auth(get("/api/v1/inventory/" + item.getId()), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(0));
    }

    @Test
    void positiveAdjustmentIncreasesStock() throws Exception {
        String token = adminToken();
        Item item = newItem();

        mockMvc.perform(authJson(post("/api/v1/inventory/adjustments"), token)
                        .content(objectMapper.writeValueAsString(
                                new InventoryAdjustmentRequest(item.getId(), new BigDecimal("25"), "stock intake"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(25));
    }

    @Test
    void negativeAdjustmentCanTakeStockNegative() throws Exception {
        // InventoryService deliberately never refuses a change that would go negative.
        String token = adminToken();
        Item item = newItem();

        mockMvc.perform(authJson(post("/api/v1/inventory/adjustments"), token)
                        .content(objectMapper.writeValueAsString(
                                new InventoryAdjustmentRequest(item.getId(), new BigDecimal("-10"), "shrinkage"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(-10));
    }

    @Test
    void multipleAdjustmentsAccumulate() throws Exception {
        String token = adminToken();
        Item item = newItem();

        mockMvc.perform(authJson(post("/api/v1/inventory/adjustments"), token)
                        .content(objectMapper.writeValueAsString(new InventoryAdjustmentRequest(item.getId(), new BigDecimal("10"), null))))
                .andExpect(status().isOk());
        mockMvc.perform(authJson(post("/api/v1/inventory/adjustments"), token)
                        .content(objectMapper.writeValueAsString(new InventoryAdjustmentRequest(item.getId(), new BigDecimal("-3"), null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(7));

        mockMvc.perform(auth(get("/api/v1/inventory/" + item.getId() + "/transactions"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].reason").value("ADJUSTMENT"));
    }

    @Test
    void adjustmentAgainstUnknownItemReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(post("/api/v1/inventory/adjustments"), token)
                        .content(objectMapper.writeValueAsString(new InventoryAdjustmentRequest(999999L, new BigDecimal("1"), null))))
                .andExpect(status().isNotFound());
    }

    @Test
    void listAllIncludesEveryItemEvenWithoutMovement() throws Exception {
        String token = adminToken();
        newItem();
        newItem();

        mockMvc.perform(auth(get("/api/v1/inventory"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void userWithoutInventoryEditPermissionCannotAdjustStock() throws Exception {
        Item item = newItem();
        factory.createUser("viewer", "pass1234", "Operator"); // Operator has no INVENTORY permissions at all
        String token = login("viewer", "pass1234");

        mockMvc.perform(authJson(post("/api/v1/inventory/adjustments"), token)
                        .content(objectMapper.writeValueAsString(new InventoryAdjustmentRequest(item.getId(), new BigDecimal("1"), null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequestsAreRejected() throws Exception {
        mockMvc.perform(get("/api/v1/inventory")).andExpect(status().isUnauthorized());
    }
}
