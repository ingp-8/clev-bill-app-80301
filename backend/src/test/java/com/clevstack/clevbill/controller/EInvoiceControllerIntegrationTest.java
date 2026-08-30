package com.clevstack.clevbill.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clevstack.clevbill.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Happy-path queueing behavior (queued PENDING when eInvoiceEnabled, no row
 * at all when disabled) is covered end-to-end in
 * SaleControllerIntegrationTest, since it's a direct side effect of
 * checkout. This file covers the endpoint's own concerns: unknown sale and
 * the permission gate.
 */
class EInvoiceControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void unknownSaleReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(auth(get("/api/v1/sales/999999/e-invoice"), token)).andExpect(status().isNotFound());
    }

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/sales/1/e-invoice")).andExpect(status().isUnauthorized());
    }

    @Test
    void userWithoutBillingViewPermissionIsForbidden() throws Exception {
        factory.createUser("noview", "pass1234"); // no roles, no permissions
        String token = login("noview", "pass1234");

        mockMvc.perform(auth(get("/api/v1/sales/1/e-invoice"), token)).andExpect(status().isForbidden());
    }
}
