package com.clevstack.clevbill.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clevstack.clevbill.dto.CheckoutItemRequest;
import com.clevstack.clevbill.dto.CheckoutRequest;
import com.clevstack.clevbill.dto.PaymentRequest;
import com.clevstack.clevbill.dto.ReturnItemRequest;
import com.clevstack.clevbill.dto.ReturnRequest;
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

class ReturnControllerIntegrationTest extends AbstractIntegrationTest {

    private record Sale(Long saleId, Long saleItemId) {
    }

    private Sale checkoutOneItem(String token, Property property, PosTerminal pos, Item item, BigDecimal quantity, BigDecimal total)
            throws Exception {
        CheckoutRequest request = new CheckoutRequest(
                property.getId(),
                pos.getId(),
                null,
                List.of(new CheckoutItemRequest(item.getId(), quantity)),
                List.of(new PaymentRequest(PaymentMethod.CASH, total)));

        String response = mockMvc.perform(authJson(post("/api/v1/sales"), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var json = objectMapper.readTree(response);
        return new Sale(json.get("id").asLong(), json.get("items").get(0).get("id").asLong());
    }

    @Test
    void partialReturnWithinPurchasedQuantitySucceedsAndRestocksInventory() throws Exception {
        String token = adminToken();
        var client = factory.createClient();
        Property property = factory.createProperty(client);
        PosTerminal pos = factory.createPosTerminal(property);
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode hsn = factory.createHsnCode(property);
        Item item = factory.createItem(property, taxRate, hsn, new BigDecimal("100.00"));

        Sale sale = checkoutOneItem(token, property, pos, item, new BigDecimal("5"), new BigDecimal("590.00"));

        ReturnRequest returnRequest =
                new ReturnRequest("damaged", List.of(new ReturnItemRequest(sale.saleItemId(), new BigDecimal("2"))));

        mockMvc.perform(authJson(post("/api/v1/sales/" + sale.saleId() + "/returns"), token)
                        .content(objectMapper.writeValueAsString(returnRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saleId").value(sale.saleId()))
                .andExpect(jsonPath("$.totalAmount").value(236.00))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        // inventory started at -5 from the sale (never blocked on stock), +2 from the return => -3
        mockMvc.perform(auth(get("/api/v1/inventory/" + item.getId()), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(-3));
    }

    @Test
    void returningMoreThanRemainingQuantityIsRejected() throws Exception {
        String token = adminToken();
        var client = factory.createClient();
        Property property = factory.createProperty(client);
        PosTerminal pos = factory.createPosTerminal(property);
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode hsn = factory.createHsnCode(property);
        Item item = factory.createItem(property, taxRate, hsn, new BigDecimal("100.00"));

        Sale sale = checkoutOneItem(token, property, pos, item, new BigDecimal("2"), new BigDecimal("236.00"));

        ReturnRequest returnRequest =
                new ReturnRequest("too many", List.of(new ReturnItemRequest(sale.saleItemId(), new BigDecimal("3"))));

        mockMvc.perform(authJson(post("/api/v1/sales/" + sale.saleId() + "/returns"), token)
                        .content(objectMapper.writeValueAsString(returnRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void secondReturnIsCappedByWhatsAlreadyBeenReturned() throws Exception {
        String token = adminToken();
        var client = factory.createClient();
        Property property = factory.createProperty(client);
        PosTerminal pos = factory.createPosTerminal(property);
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode hsn = factory.createHsnCode(property);
        Item item = factory.createItem(property, taxRate, hsn, new BigDecimal("100.00"));

        Sale sale = checkoutOneItem(token, property, pos, item, new BigDecimal("5"), new BigDecimal("590.00"));

        mockMvc.perform(authJson(post("/api/v1/sales/" + sale.saleId() + "/returns"), token)
                        .content(objectMapper.writeValueAsString(
                                new ReturnRequest("first", List.of(new ReturnItemRequest(sale.saleItemId(), new BigDecimal("4")))))))
                .andExpect(status().isOk());

        // only 1 unit remains returnable now
        mockMvc.perform(authJson(post("/api/v1/sales/" + sale.saleId() + "/returns"), token)
                        .content(objectMapper.writeValueAsString(
                                new ReturnRequest("second", List.of(new ReturnItemRequest(sale.saleItemId(), new BigDecimal("2")))))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(authJson(post("/api/v1/sales/" + sale.saleId() + "/returns"), token)
                        .content(objectMapper.writeValueAsString(
                                new ReturnRequest("second-ok", List.of(new ReturnItemRequest(sale.saleItemId(), new BigDecimal("1")))))))
                .andExpect(status().isOk());
    }

    @Test
    void returnAgainstASaleItemFromADifferentSaleIsRejected() throws Exception {
        String token = adminToken();
        var client = factory.createClient();
        Property property = factory.createProperty(client);
        PosTerminal pos = factory.createPosTerminal(property);
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode hsn = factory.createHsnCode(property);
        Item item = factory.createItem(property, taxRate, hsn, new BigDecimal("100.00"));

        Sale saleOne = checkoutOneItem(token, property, pos, item, new BigDecimal("1"), new BigDecimal("118.00"));
        Sale saleTwo = checkoutOneItem(token, property, pos, item, new BigDecimal("1"), new BigDecimal("118.00"));

        ReturnRequest returnRequest =
                new ReturnRequest("wrong sale", List.of(new ReturnItemRequest(saleOne.saleItemId(), new BigDecimal("1"))));

        mockMvc.perform(authJson(post("/api/v1/sales/" + saleTwo.saleId() + "/returns"), token)
                        .content(objectMapper.writeValueAsString(returnRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnAgainstUnknownSaleReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(authJson(post("/api/v1/sales/999999/returns"), token)
                        .content(objectMapper.writeValueAsString(
                                new ReturnRequest("x", List.of(new ReturnItemRequest(1L, new BigDecimal("1")))))))
                .andExpect(status().isNotFound());
    }

    @Test
    void listReturnsForASale() throws Exception {
        String token = adminToken();
        var client = factory.createClient();
        Property property = factory.createProperty(client);
        PosTerminal pos = factory.createPosTerminal(property);
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode hsn = factory.createHsnCode(property);
        Item item = factory.createItem(property, taxRate, hsn, new BigDecimal("100.00"));
        Sale sale = checkoutOneItem(token, property, pos, item, new BigDecimal("5"), new BigDecimal("590.00"));

        mockMvc.perform(authJson(post("/api/v1/sales/" + sale.saleId() + "/returns"), token)
                        .content(objectMapper.writeValueAsString(
                                new ReturnRequest("r1", List.of(new ReturnItemRequest(sale.saleItemId(), new BigDecimal("1")))))))
                .andExpect(status().isOk());

        mockMvc.perform(auth(get("/api/v1/sales/" + sale.saleId() + "/returns"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void userWithoutReturnsPermissionCannotCreateAReturn() throws Exception {
        String token = adminToken();
        var client = factory.createClient();
        Property property = factory.createProperty(client);
        PosTerminal pos = factory.createPosTerminal(property);
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode hsn = factory.createHsnCode(property);
        Item item = factory.createItem(property, taxRate, hsn, new BigDecimal("100.00"));
        Sale sale = checkoutOneItem(token, property, pos, item, new BigDecimal("1"), new BigDecimal("118.00"));

        factory.createUser("noreturns", "pass1234");
        String noAccessToken = login("noreturns", "pass1234");

        mockMvc.perform(authJson(post("/api/v1/sales/" + sale.saleId() + "/returns"), noAccessToken)
                        .content(objectMapper.writeValueAsString(
                                new ReturnRequest("x", List.of(new ReturnItemRequest(sale.saleItemId(), new BigDecimal("1")))))))
                .andExpect(status().isForbidden());
    }
}
