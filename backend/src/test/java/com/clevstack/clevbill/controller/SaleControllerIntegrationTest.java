package com.clevstack.clevbill.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clevstack.clevbill.dto.CheckoutItemRequest;
import com.clevstack.clevbill.dto.CheckoutRequest;
import com.clevstack.clevbill.dto.PaymentRequest;
import com.clevstack.clevbill.model.Client;
import com.clevstack.clevbill.model.Customer;
import com.clevstack.clevbill.model.HsnCode;
import com.clevstack.clevbill.model.Item;
import com.clevstack.clevbill.model.PaymentMethod;
import com.clevstack.clevbill.model.PosTerminal;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.model.TaxRate;
import com.clevstack.clevbill.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Checkout is the primary write path (billing-transactions skill): bill
 * number allocation, GST split, split payments, and the stock decrement
 * all have to happen atomically. These tests exercise that whole path
 * through the real HTTP boundary rather than mocking SaleService's
 * collaborators, since the thing actually worth verifying is that they
 * cooperate correctly.
 */
class SaleControllerIntegrationTest extends AbstractIntegrationTest {

    private record Fixture(Client client, Property property, PosTerminal pos, TaxRate taxRate, HsnCode hsn) {
    }

    private Fixture fixture(String seriesPrefix, boolean eInvoiceEnabled) {
        Client client = factory.createClient();
        Property property = factory.createProperty(client, seriesPrefix, eInvoiceEnabled);
        PosTerminal pos = factory.createPosTerminal(property);
        TaxRate taxRate = factory.createTaxRate(property, new BigDecimal("9.00"), new BigDecimal("9.00"), new BigDecimal("18.00"));
        HsnCode hsn = factory.createHsnCode(property);
        return new Fixture(client, property, pos, taxRate, hsn);
    }

    @Test
    void checkoutWithSinglePaymentComputesTotalsAndAllocatesBillNumber() throws Exception {
        String token = adminToken();
        Fixture fx = fixture("ACME", false);
        Item item = factory.createItem(fx.property(), fx.taxRate(), fx.hsn(), new BigDecimal("100.00"));

        CheckoutRequest request = new CheckoutRequest(
                fx.property().getId(),
                fx.pos().getId(),
                null,
                List.of(new CheckoutItemRequest(item.getId(), new BigDecimal("2"))),
                List.of(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("236.00"))));

        mockMvc.perform(authJson(post("/api/v1/sales"), token).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.billNumber", matchesPattern("ACME-\\d{4}-\\d{2}-\\d{5}")))
                .andExpect(jsonPath("$.subtotalAmount").value(200.00))
                .andExpect(jsonPath("$.taxAmount").value(36.00))
                .andExpect(jsonPath("$.totalAmount").value(236.00))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].cgstAmount").value(18.00))
                .andExpect(jsonPath("$.items[0].sgstAmount").value(18.00))
                .andExpect(jsonPath("$.items[0].igstAmount").value(0.00));
    }

    @Test
    void checkoutDecrementsInventoryAndRecordsALedgerEntry() throws Exception {
        String token = adminToken();
        Fixture fx = fixture("ACME", false);
        Item item = factory.createItem(fx.property(), fx.taxRate(), fx.hsn(), new BigDecimal("50.00"));
        // seed starting stock via a manual adjustment so we can assert a real decrement, not just going negative
        mockMvc.perform(authJson(post("/api/v1/inventory/adjustments"), token)
                        .content(objectMapper.writeValueAsString(
                                new com.clevstack.clevbill.dto.InventoryAdjustmentRequest(item.getId(), new BigDecimal("10"), "initial stock"))))
                .andExpect(status().isOk());

        CheckoutRequest request = new CheckoutRequest(
                fx.property().getId(),
                fx.pos().getId(),
                null,
                List.of(new CheckoutItemRequest(item.getId(), new BigDecimal("3"))),
                List.of(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("177.00"))));

        mockMvc.perform(authJson(post("/api/v1/sales"), token).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(auth(get("/api/v1/inventory/" + item.getId()), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(7));

        mockMvc.perform(auth(get("/api/v1/inventory/" + item.getId() + "/transactions"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reason").value("SALE"))
                .andExpect(jsonPath("$[0].changeQuantity").value(-3));
    }

    @Test
    void checkoutIsNeverBlockedByInsufficientStock() throws Exception {
        // billing-transactions skill: never refuse a sale over a stock technicality.
        String token = adminToken();
        Fixture fx = fixture("ACME", false);
        Item item = factory.createItem(fx.property(), fx.taxRate(), fx.hsn(), new BigDecimal("10.00"));

        CheckoutRequest request = new CheckoutRequest(
                fx.property().getId(),
                fx.pos().getId(),
                null,
                List.of(new CheckoutItemRequest(item.getId(), new BigDecimal("5"))),
                List.of(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("59.00"))));

        mockMvc.perform(authJson(post("/api/v1/sales"), token).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(auth(get("/api/v1/inventory/" + item.getId()), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(-5));
    }

    @Test
    void checkoutRejectsMismatchedPaymentTotal() throws Exception {
        String token = adminToken();
        Fixture fx = fixture("ACME", false);
        Item item = factory.createItem(fx.property(), fx.taxRate(), fx.hsn(), new BigDecimal("100.00"));

        CheckoutRequest request = new CheckoutRequest(
                fx.property().getId(),
                fx.pos().getId(),
                null,
                List.of(new CheckoutItemRequest(item.getId(), new BigDecimal("1"))),
                List.of(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("50.00"))));

        mockMvc.perform(authJson(post("/api/v1/sales"), token).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkoutAcceptsSplitPaymentsSummingToTheTotal() throws Exception {
        String token = adminToken();
        Fixture fx = fixture("ACME", false);
        Item item = factory.createItem(fx.property(), fx.taxRate(), fx.hsn(), new BigDecimal("100.00"));

        CheckoutRequest request = new CheckoutRequest(
                fx.property().getId(),
                fx.pos().getId(),
                null,
                List.of(new CheckoutItemRequest(item.getId(), new BigDecimal("1"))),
                List.of(
                        new PaymentRequest(PaymentMethod.CASH, new BigDecimal("100.00")),
                        new PaymentRequest(PaymentMethod.CARD, new BigDecimal("18.00"))));

        mockMvc.perform(authJson(post("/api/v1/sales"), token).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payments", hasSize(2)))
                .andExpect(jsonPath("$.totalAmount").value(118.00));
    }

    @Test
    void checkoutRejectsPosTerminalFromAnotherProperty() throws Exception {
        String token = adminToken();
        Fixture fx = fixture("ACME", false);
        Property otherProperty = factory.createProperty(fx.client());
        PosTerminal foreignPos = factory.createPosTerminal(otherProperty);
        Item item = factory.createItem(fx.property(), fx.taxRate(), fx.hsn(), new BigDecimal("100.00"));

        CheckoutRequest request = new CheckoutRequest(
                fx.property().getId(),
                foreignPos.getId(),
                null,
                List.of(new CheckoutItemRequest(item.getId(), new BigDecimal("1"))),
                List.of(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("118.00"))));

        mockMvc.perform(authJson(post("/api/v1/sales"), token).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkoutRejectsItemFromAnotherProperty() throws Exception {
        String token = adminToken();
        Fixture fx = fixture("ACME", false);
        Property otherProperty = factory.createProperty(fx.client());
        TaxRate otherTaxRate = factory.createTaxRate(otherProperty);
        HsnCode otherHsn = factory.createHsnCode(otherProperty);
        Item foreignItem = factory.createItem(otherProperty, otherTaxRate, otherHsn, new BigDecimal("100.00"));

        CheckoutRequest request = new CheckoutRequest(
                fx.property().getId(),
                fx.pos().getId(),
                null,
                List.of(new CheckoutItemRequest(foreignItem.getId(), new BigDecimal("1"))),
                List.of(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("118.00"))));

        mockMvc.perform(authJson(post("/api/v1/sales"), token).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkoutAgainstUnknownCustomerReturnsNotFound() throws Exception {
        String token = adminToken();
        Fixture fx = fixture("ACME", false);
        Item item = factory.createItem(fx.property(), fx.taxRate(), fx.hsn(), new BigDecimal("100.00"));

        CheckoutRequest request = new CheckoutRequest(
                fx.property().getId(),
                fx.pos().getId(),
                999999L,
                List.of(new CheckoutItemRequest(item.getId(), new BigDecimal("1"))),
                List.of(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("118.00"))));

        mockMvc.perform(authJson(post("/api/v1/sales"), token).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void interstateSaleAppliesIgstInsteadOfCgstSgst() throws Exception {
        String token = adminToken();
        Fixture fx = fixture("ACME", false); // property GSTIN state code "27"
        Item item = factory.createItem(fx.property(), fx.taxRate(), fx.hsn(), new BigDecimal("100.00"));
        Customer interstateCustomer = factory.createCustomer(fx.client(), "09BBBBB1111B2Z6"); // state code "09"

        CheckoutRequest request = new CheckoutRequest(
                fx.property().getId(),
                fx.pos().getId(),
                interstateCustomer.getId(),
                List.of(new CheckoutItemRequest(item.getId(), new BigDecimal("1"))),
                List.of(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("118.00"))));

        mockMvc.perform(authJson(post("/api/v1/sales"), token).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].cgstAmount").value(0.00))
                .andExpect(jsonPath("$.items[0].sgstAmount").value(0.00))
                .andExpect(jsonPath("$.items[0].igstAmount").value(18.00));
    }

    @Test
    void intrastateSaleWithMatchingCustomerGstinAppliesCgstSgst() throws Exception {
        String token = adminToken();
        Fixture fx = fixture("ACME", false); // property GSTIN state code "27"
        Item item = factory.createItem(fx.property(), fx.taxRate(), fx.hsn(), new BigDecimal("100.00"));
        Customer localCustomer = factory.createCustomer(fx.client(), "27BBBBB1111B2Z6");

        CheckoutRequest request = new CheckoutRequest(
                fx.property().getId(),
                fx.pos().getId(),
                localCustomer.getId(),
                List.of(new CheckoutItemRequest(item.getId(), new BigDecimal("1"))),
                List.of(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("118.00"))));

        mockMvc.perform(authJson(post("/api/v1/sales"), token).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].cgstAmount").value(9.00))
                .andExpect(jsonPath("$.items[0].igstAmount").value(0.00));
    }

    @Test
    void fractionalQuantityIsSupportedForWeightSoldItems() throws Exception {
        String token = adminToken();
        Fixture fx = fixture("ACME", false);
        Item item = factory.createItem(fx.property(), fx.taxRate(), fx.hsn(), new BigDecimal("100.00"));

        CheckoutRequest request = new CheckoutRequest(
                fx.property().getId(),
                fx.pos().getId(),
                null,
                List.of(new CheckoutItemRequest(item.getId(), new BigDecimal("1.5"))),
                List.of(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("177.00"))));

        mockMvc.perform(authJson(post("/api/v1/sales"), token).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(1.5))
                .andExpect(jsonPath("$.subtotalAmount").value(150.00))
                .andExpect(jsonPath("$.totalAmount").value(177.00));
    }

    @Test
    void billNumbersIncrementSequentiallyWithinAProperty() throws Exception {
        String token = adminToken();
        Fixture fx = fixture("SEQ", false);
        Item item = factory.createItem(fx.property(), fx.taxRate(), fx.hsn(), new BigDecimal("100.00"));

        CheckoutRequest request = new CheckoutRequest(
                fx.property().getId(),
                fx.pos().getId(),
                null,
                List.of(new CheckoutItemRequest(item.getId(), new BigDecimal("1"))),
                List.of(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("118.00"))));
        String body = objectMapper.writeValueAsString(request);

        String firstBill = mockMvc.perform(authJson(post("/api/v1/sales"), token).content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String secondBill = mockMvc.perform(authJson(post("/api/v1/sales"), token).content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String firstNumber = objectMapper.readTree(firstBill).get("billNumber").asText();
        String secondNumber = objectMapper.readTree(secondBill).get("billNumber").asText();
        int firstSeq = Integer.parseInt(firstNumber.substring(firstNumber.length() - 5));
        int secondSeq = Integer.parseInt(secondNumber.substring(secondNumber.length() - 5));

        org.assertj.core.api.Assertions.assertThat(secondSeq).isEqualTo(firstSeq + 1);
    }

    @Test
    void twoDifferentPropertiesGetIndependentBillNumberSequences() throws Exception {
        String token = adminToken();
        Fixture fxA = fixture("PA", false);
        Fixture fxB = fixture("PB", false);
        Item itemA = factory.createItem(fxA.property(), fxA.taxRate(), fxA.hsn(), new BigDecimal("100.00"));
        Item itemB = factory.createItem(fxB.property(), fxB.taxRate(), fxB.hsn(), new BigDecimal("100.00"));

        CheckoutRequest requestA = new CheckoutRequest(
                fxA.property().getId(),
                fxA.pos().getId(),
                null,
                List.of(new CheckoutItemRequest(itemA.getId(), new BigDecimal("1"))),
                List.of(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("118.00"))));
        CheckoutRequest requestB = new CheckoutRequest(
                fxB.property().getId(),
                fxB.pos().getId(),
                null,
                List.of(new CheckoutItemRequest(itemB.getId(), new BigDecimal("1"))),
                List.of(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("118.00"))));

        mockMvc.perform(authJson(post("/api/v1/sales"), token).content(objectMapper.writeValueAsString(requestA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.billNumber", matchesPattern("PA-\\d{4}-\\d{2}-00001")));
        mockMvc.perform(authJson(post("/api/v1/sales"), token).content(objectMapper.writeValueAsString(requestB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.billNumber", matchesPattern("PB-\\d{4}-\\d{2}-00001")));
    }

    @Test
    void eInvoiceIsQueuedAsPendingWhenPropertyHasEInvoicingEnabled() throws Exception {
        String token = adminToken();
        Fixture fx = fixture("EINV", true);
        Item item = factory.createItem(fx.property(), fx.taxRate(), fx.hsn(), new BigDecimal("100.00"));

        CheckoutRequest request = new CheckoutRequest(
                fx.property().getId(),
                fx.pos().getId(),
                null,
                List.of(new CheckoutItemRequest(item.getId(), new BigDecimal("1"))),
                List.of(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("118.00"))));

        String response = mockMvc.perform(authJson(post("/api/v1/sales"), token).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long saleId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(auth(get("/api/v1/sales/" + saleId + "/e-invoice"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.attempts").value(0));
    }

    @Test
    void noEInvoiceIsQueuedWhenPropertyHasEInvoicingDisabled() throws Exception {
        String token = adminToken();
        Fixture fx = fixture("NOEINV", false);
        Item item = factory.createItem(fx.property(), fx.taxRate(), fx.hsn(), new BigDecimal("100.00"));

        CheckoutRequest request = new CheckoutRequest(
                fx.property().getId(),
                fx.pos().getId(),
                null,
                List.of(new CheckoutItemRequest(item.getId(), new BigDecimal("1"))),
                List.of(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("118.00"))));

        String response = mockMvc.perform(authJson(post("/api/v1/sales"), token).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long saleId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(auth(get("/api/v1/sales/" + saleId + "/e-invoice"), token))
                .andExpect(status().isNotFound());
    }

    @Test
    void listByPropertyAndGetById() throws Exception {
        String token = adminToken();
        Fixture fx = fixture("ACME", false);
        Item item = factory.createItem(fx.property(), fx.taxRate(), fx.hsn(), new BigDecimal("100.00"));
        CheckoutRequest request = new CheckoutRequest(
                fx.property().getId(),
                fx.pos().getId(),
                null,
                List.of(new CheckoutItemRequest(item.getId(), new BigDecimal("1"))),
                List.of(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("118.00"))));

        mockMvc.perform(authJson(post("/api/v1/sales"), token).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(auth(get("/api/v1/properties/" + fx.property().getId() + "/sales"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getUnknownSaleReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(auth(get("/api/v1/sales/999999"), token)).andExpect(status().isNotFound());
    }

    @Test
    void operatorWithoutBillingCreatePermissionCannotCheckOutWhenRoleLacksIt() throws Exception {
        // Operator DOES have BILLING:CREATE by seed data — verify a role that
        // genuinely lacks it (Store Manager has BILLING:CREATE too, so use a
        // freshly-created role with no permissions at all).
        Fixture fx = fixture("ACME", false);
        Item item = factory.createItem(fx.property(), fx.taxRate(), fx.hsn(), new BigDecimal("100.00"));
        factory.createUser("nobody", "pass1234"); // no roles at all
        String token = login("nobody", "pass1234");

        CheckoutRequest request = new CheckoutRequest(
                fx.property().getId(),
                fx.pos().getId(),
                null,
                List.of(new CheckoutItemRequest(item.getId(), new BigDecimal("1"))),
                List.of(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("118.00"))));

        mockMvc.perform(authJson(post("/api/v1/sales"), token).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void checkoutRequiresAuthentication() throws Exception {
        Fixture fx = fixture("ACME", false);
        Item item = factory.createItem(fx.property(), fx.taxRate(), fx.hsn(), new BigDecimal("100.00"));
        CheckoutRequest request = new CheckoutRequest(
                fx.property().getId(),
                fx.pos().getId(),
                null,
                List.of(new CheckoutItemRequest(item.getId(), new BigDecimal("1"))),
                List.of(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("118.00"))));

        mockMvc.perform(post("/api/v1/sales")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
