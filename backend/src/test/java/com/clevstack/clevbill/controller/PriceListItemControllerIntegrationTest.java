package com.clevstack.clevbill.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clevstack.clevbill.dto.PriceListItemRequest;
import com.clevstack.clevbill.model.Client;
import com.clevstack.clevbill.model.HsnCode;
import com.clevstack.clevbill.model.Item;
import com.clevstack.clevbill.model.PriceList;
import com.clevstack.clevbill.model.PriceListItem;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.model.TaxRate;
import com.clevstack.clevbill.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Full CRUD coverage for PriceListItem, the child of PriceList, nested at
 * /api/v1/masters/price-lists/{priceListId}/items (not property-scoped in
 * the URL — the path variable is priceListId, not propertyId, so
 * PropertyAccessInterceptor does not apply here; per the task brief only a
 * permission (403) test is written, no property-access test).
 *
 * <p>Per PriceListItemService: create()/update() look up the item purely by
 * id and never verify it belongs to the same property as the price list
 * (unlike ItemService's category/brand/taxRate/hsnCode checks) — {@link
 * #createAllowsAnItemFromADifferentPropertyThanThePriceList()} documents
 * this actual (permissive) behavior; see the test-run summary for whether
 * this is flagged as a gap.
 */
class PriceListItemControllerIntegrationTest extends AbstractIntegrationTest {

    private record Fixture(Property property, PriceList priceList, Item item) {
    }

    private Fixture buildFixture() {
        Property property = factory.createProperty(factory.createClient());
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode hsnCode = factory.createHsnCode(property);
        Item item = factory.createItem(property, taxRate, hsnCode);
        PriceList priceList = factory.createPriceList(property);
        return new Fixture(property, priceList, item);
    }

    @Test
    void createThenListReturnsThePriceListItemUnderThePriceList() throws Exception {
        String token = adminToken();
        Fixture fixture = buildFixture();

        mockMvc.perform(authJson(
                        post("/api/v1/masters/price-lists/" + fixture.priceList().getId() + "/items"), token)
                        .content(objectMapper.writeValueAsString(
                                new PriceListItemRequest(fixture.item().getId(), new BigDecimal("199.99")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.priceListId").value(fixture.priceList().getId()))
                .andExpect(jsonPath("$.item.id").value(fixture.item().getId()))
                .andExpect(jsonPath("$.item.name").value(fixture.item().getName()))
                .andExpect(jsonPath("$.price").value(199.99));

        mockMvc.perform(auth(get("/api/v1/masters/price-lists/" + fixture.priceList().getId() + "/items"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].price").value(199.99));
    }

    @Test
    void createRejectsMissingItemId() throws Exception {
        String token = adminToken();
        Fixture fixture = buildFixture();
        String body = "{\"price\":10.00}";

        mockMvc.perform(authJson(
                        post("/api/v1/masters/price-lists/" + fixture.priceList().getId() + "/items"), token)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRejectsNegativePrice() throws Exception {
        String token = adminToken();
        Fixture fixture = buildFixture();

        mockMvc.perform(authJson(
                        post("/api/v1/masters/price-lists/" + fixture.priceList().getId() + "/items"), token)
                        .content(objectMapper.writeValueAsString(
                                new PriceListItemRequest(fixture.item().getId(), new BigDecimal("-1.00")))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAgainstUnknownPriceListReturnsNotFound() throws Exception {
        String token = adminToken();
        Fixture fixture = buildFixture();

        mockMvc.perform(authJson(post("/api/v1/masters/price-lists/999999/items"), token)
                        .content(objectMapper.writeValueAsString(
                                new PriceListItemRequest(fixture.item().getId(), new BigDecimal("10.00")))))
                .andExpect(status().isNotFound());
    }

    @Test
    void createWithUnknownItemIdReturnsNotFound() throws Exception {
        String token = adminToken();
        Fixture fixture = buildFixture();

        mockMvc.perform(authJson(
                        post("/api/v1/masters/price-lists/" + fixture.priceList().getId() + "/items"), token)
                        .content(objectMapper.writeValueAsString(new PriceListItemRequest(999999L, new BigDecimal("10.00")))))
                .andExpect(status().isNotFound());
    }

    @Test
    void createRejectsDuplicateItemWithinSamePriceList() throws Exception {
        String token = adminToken();
        Fixture fixture = buildFixture();

        mockMvc.perform(authJson(
                        post("/api/v1/masters/price-lists/" + fixture.priceList().getId() + "/items"), token)
                        .content(objectMapper.writeValueAsString(
                                new PriceListItemRequest(fixture.item().getId(), new BigDecimal("10.00")))))
                .andExpect(status().isOk());

        mockMvc.perform(authJson(
                        post("/api/v1/masters/price-lists/" + fixture.priceList().getId() + "/items"), token)
                        .content(objectMapper.writeValueAsString(
                                new PriceListItemRequest(fixture.item().getId(), new BigDecimal("20.00")))))
                .andExpect(status().isConflict());
    }

    /**
     * PriceListItemService.create() never checks that the item's property
     * matches the price list's property (unlike ItemService's category /
     * brand / taxRate / hsnCode cross-property checks). This test pins down
     * that current, permissive behavior rather than assuming a validation
     * rule that isn't actually implemented.
     */
    @Test
    void createAllowsAnItemFromADifferentPropertyThanThePriceList() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();
        Property propertyA = factory.createProperty(client);
        Property propertyB = factory.createProperty(client);
        TaxRate taxRateB = factory.createTaxRate(propertyB);
        HsnCode hsnCodeB = factory.createHsnCode(propertyB);
        Item itemFromPropertyB = factory.createItem(propertyB, taxRateB, hsnCodeB);
        PriceList priceListOnPropertyA = factory.createPriceList(propertyA);

        mockMvc.perform(authJson(
                        post("/api/v1/masters/price-lists/" + priceListOnPropertyA.getId() + "/items"), token)
                        .content(objectMapper.writeValueAsString(
                                new PriceListItemRequest(itemFromPropertyB.getId(), new BigDecimal("10.00")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id").value(itemFromPropertyB.getId()));
    }

    @Test
    void updateChangesItemAndPrice() throws Exception {
        String token = adminToken();
        Fixture fixture = buildFixture();
        PriceListItem priceListItem =
                factory.createPriceListItem(fixture.priceList(), fixture.item(), new BigDecimal("50.00"));
        TaxRate taxRate = factory.createTaxRate(fixture.property());
        HsnCode hsnCode = factory.createHsnCode(fixture.property());
        Item otherItem = factory.createItem(fixture.property(), taxRate, hsnCode);

        mockMvc.perform(authJson(
                        put("/api/v1/masters/price-lists/" + fixture.priceList().getId() + "/items/"
                                + priceListItem.getId()),
                        token)
                        .content(objectMapper.writeValueAsString(
                                new PriceListItemRequest(otherItem.getId(), new BigDecimal("75.50")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id").value(otherItem.getId()))
                .andExpect(jsonPath("$.price").value(75.50));
    }

    @Test
    void updateWithMismatchedPriceListIdInPathReturnsNotFound() throws Exception {
        String token = adminToken();
        Fixture fixture = buildFixture();
        PriceListItem priceListItem =
                factory.createPriceListItem(fixture.priceList(), fixture.item(), new BigDecimal("50.00"));
        PriceList otherPriceList = factory.createPriceList(fixture.property());

        mockMvc.perform(authJson(
                        put("/api/v1/masters/price-lists/" + otherPriceList.getId() + "/items/" + priceListItem.getId()),
                        token)
                        .content(objectMapper.writeValueAsString(
                                new PriceListItemRequest(fixture.item().getId(), new BigDecimal("60.00")))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUnknownPriceListItemReturnsNotFound() throws Exception {
        String token = adminToken();
        Fixture fixture = buildFixture();

        mockMvc.perform(authJson(
                        put("/api/v1/masters/price-lists/" + fixture.priceList().getId() + "/items/999999"), token)
                        .content(objectMapper.writeValueAsString(
                                new PriceListItemRequest(fixture.item().getId(), new BigDecimal("10.00")))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateWithUnknownItemIdReturnsNotFound() throws Exception {
        String token = adminToken();
        Fixture fixture = buildFixture();
        PriceListItem priceListItem =
                factory.createPriceListItem(fixture.priceList(), fixture.item(), new BigDecimal("50.00"));

        mockMvc.perform(authJson(
                        put("/api/v1/masters/price-lists/" + fixture.priceList().getId() + "/items/"
                                + priceListItem.getId()),
                        token)
                        .content(objectMapper.writeValueAsString(new PriceListItemRequest(999999L, new BigDecimal("10.00")))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRemovesThePriceListItem() throws Exception {
        String token = adminToken();
        Fixture fixture = buildFixture();
        PriceListItem priceListItem =
                factory.createPriceListItem(fixture.priceList(), fixture.item(), new BigDecimal("50.00"));

        mockMvc.perform(auth(
                        delete("/api/v1/masters/price-lists/" + fixture.priceList().getId() + "/items/"
                                + priceListItem.getId()),
                        token))
                .andExpect(status().isOk());

        mockMvc.perform(auth(get("/api/v1/masters/price-lists/" + fixture.priceList().getId() + "/items"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deleteWithMismatchedPriceListIdInPathReturnsNotFound() throws Exception {
        String token = adminToken();
        Fixture fixture = buildFixture();
        PriceListItem priceListItem =
                factory.createPriceListItem(fixture.priceList(), fixture.item(), new BigDecimal("50.00"));
        PriceList otherPriceList = factory.createPriceList(fixture.property());

        mockMvc.perform(auth(
                        delete("/api/v1/masters/price-lists/" + otherPriceList.getId() + "/items/"
                                + priceListItem.getId()),
                        token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUnknownPriceListItemReturnsNotFound() throws Exception {
        String token = adminToken();
        Fixture fixture = buildFixture();

        mockMvc.perform(auth(
                        delete("/api/v1/masters/price-lists/" + fixture.priceList().getId() + "/items/999999"), token))
                .andExpect(status().isNotFound());
    }

    @Test
    void listAgainstUnknownPriceListReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(auth(get("/api/v1/masters/price-lists/999999/items"), adminToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void requestsWithoutABearerTokenAreRejected() throws Exception {
        Fixture fixture = buildFixture();

        mockMvc.perform(get("/api/v1/masters/price-lists/" + fixture.priceList().getId() + "/items"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void operatorRoleLacksMastersPermissionAndIsForbidden() throws Exception {
        Fixture fixture = buildFixture();
        factory.createUser("pliop1", "pass1234", "Operator");
        String token = login("pliop1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/masters/price-lists/" + fixture.priceList().getId() + "/items"), token))
                .andExpect(status().isForbidden());
    }
}
