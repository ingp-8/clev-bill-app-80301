package com.clevstack.clevbill.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clevstack.clevbill.dto.ItemRequest;
import com.clevstack.clevbill.model.Brand;
import com.clevstack.clevbill.model.Category;
import com.clevstack.clevbill.model.Client;
import com.clevstack.clevbill.model.HsnCode;
import com.clevstack.clevbill.model.Item;
import com.clevstack.clevbill.model.ItemUnit;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.model.TaxRate;
import com.clevstack.clevbill.support.AbstractIntegrationTest;
import com.clevstack.clevbill.support.TestEntityFactory;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Full CRUD coverage for the Item master: property-scoped list/create,
 * bare-id get/update/delete, cross-property reference validation on
 * categoryId/brandId/taxRateId/hsnCodeId, unique constraints (sku/barcode
 * per property), the full ItemUnit enum, and both authorization dimensions
 * (permission + PropertyAccessInterceptor), following the pattern in
 * BrandControllerIntegrationTest.
 */
class ItemControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createThenListReturnsTheItemUnderItsPropertyWithFullDetail() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();
        Property property = factory.createProperty(client);
        Category category = factory.createCategory(property);
        Brand brand = factory.createBrand(property);
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode hsnCode = factory.createHsnCode(property);

        ItemRequest request = new ItemRequest(
                "SKU-001",
                "BAR-001",
                "Widget",
                category.getId(),
                brand.getId(),
                taxRate.getId(),
                hsnCode.getId(),
                ItemUnit.PCS,
                new BigDecimal("150.00"),
                new BigDecimal("90.00"),
                true);

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/items"), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.propertyId").value(property.getId()))
                .andExpect(jsonPath("$.clientId").value(client.getId()))
                .andExpect(jsonPath("$.sku").value("SKU-001"))
                .andExpect(jsonPath("$.barcode").value("BAR-001"))
                .andExpect(jsonPath("$.name").value("Widget"))
                .andExpect(jsonPath("$.category.id").value(category.getId()))
                .andExpect(jsonPath("$.category.name").value(category.getName()))
                .andExpect(jsonPath("$.brand.id").value(brand.getId()))
                .andExpect(jsonPath("$.brand.name").value(brand.getName()))
                .andExpect(jsonPath("$.taxRate.id").value(taxRate.getId()))
                .andExpect(jsonPath("$.hsnCode.id").value(hsnCode.getId()))
                .andExpect(jsonPath("$.unit").value("PCS"))
                .andExpect(jsonPath("$.sellingPrice").value(150.00))
                .andExpect(jsonPath("$.costPrice").value(90.00))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/items"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sku").value("SKU-001"));
    }

    @Test
    void createWithNullCategoryAndBrandSucceeds() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode hsnCode = factory.createHsnCode(property);

        ItemRequest request = new ItemRequest(
                "SKU-NOREF",
                null,
                "No Ref Item",
                null,
                null,
                taxRate.getId(),
                hsnCode.getId(),
                ItemUnit.KG,
                new BigDecimal("10.00"),
                null,
                true);

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/items"), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").doesNotExist())
                .andExpect(jsonPath("$.brand").doesNotExist())
                .andExpect(jsonPath("$.barcode").doesNotExist())
                .andExpect(jsonPath("$.costPrice").doesNotExist());
    }

    @Test
    void createRejectsBlankSku() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode hsnCode = factory.createHsnCode(property);

        ItemRequest request = new ItemRequest(
                "",
                null,
                "Widget",
                null,
                null,
                taxRate.getId(),
                hsnCode.getId(),
                ItemUnit.PCS,
                new BigDecimal("10.00"),
                null,
                true);

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/items"), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRejectsBlankName() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode hsnCode = factory.createHsnCode(property);

        ItemRequest request = new ItemRequest(
                "SKU-002",
                null,
                "",
                null,
                null,
                taxRate.getId(),
                hsnCode.getId(),
                ItemUnit.PCS,
                new BigDecimal("10.00"),
                null,
                true);

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/items"), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRejectsMissingTaxRateId() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        HsnCode hsnCode = factory.createHsnCode(property);

        ItemRequest request = new ItemRequest(
                "SKU-003",
                null,
                "Widget",
                null,
                null,
                null,
                hsnCode.getId(),
                ItemUnit.PCS,
                new BigDecimal("10.00"),
                null,
                true);

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/items"), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRejectsMissingHsnCodeId() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        TaxRate taxRate = factory.createTaxRate(property);

        ItemRequest request = new ItemRequest(
                "SKU-004",
                null,
                "Widget",
                null,
                null,
                taxRate.getId(),
                null,
                ItemUnit.PCS,
                new BigDecimal("10.00"),
                null,
                true);

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/items"), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRejectsNegativeSellingPrice() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode hsnCode = factory.createHsnCode(property);

        ItemRequest request = new ItemRequest(
                "SKU-005",
                null,
                "Widget",
                null,
                null,
                taxRate.getId(),
                hsnCode.getId(),
                ItemUnit.PCS,
                new BigDecimal("-5.00"),
                null,
                true);

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/items"), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAgainstUnknownPropertyReturnsNotFound() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode hsnCode = factory.createHsnCode(property);

        ItemRequest request = new ItemRequest(
                "SKU-006",
                null,
                "Widget",
                null,
                null,
                taxRate.getId(),
                hsnCode.getId(),
                ItemUnit.PCS,
                new BigDecimal("10.00"),
                null,
                true);

        mockMvc.perform(authJson(post("/api/v1/properties/999999/items"), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createWithUnknownTaxRateIdReturnsNotFound() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        HsnCode hsnCode = factory.createHsnCode(property);

        ItemRequest request = new ItemRequest(
                "SKU-007",
                null,
                "Widget",
                null,
                null,
                999999L,
                hsnCode.getId(),
                ItemUnit.PCS,
                new BigDecimal("10.00"),
                null,
                true);

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/items"), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createWithUnknownHsnCodeIdReturnsNotFound() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        TaxRate taxRate = factory.createTaxRate(property);

        ItemRequest request = new ItemRequest(
                "SKU-008",
                null,
                "Widget",
                null,
                null,
                taxRate.getId(),
                999999L,
                ItemUnit.PCS,
                new BigDecimal("10.00"),
                null,
                true);

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/items"), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createWithCategoryFromDifferentPropertyReturnsBadRequest() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();
        Property property = factory.createProperty(client);
        Property otherProperty = factory.createProperty(client);
        Category foreignCategory = factory.createCategory(otherProperty);
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode hsnCode = factory.createHsnCode(property);

        ItemRequest request = new ItemRequest(
                "SKU-009",
                null,
                "Widget",
                foreignCategory.getId(),
                null,
                taxRate.getId(),
                hsnCode.getId(),
                ItemUnit.PCS,
                new BigDecimal("10.00"),
                null,
                true);

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/items"), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("does not belong to property")));
    }

    @Test
    void createWithBrandFromDifferentPropertyReturnsBadRequest() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();
        Property property = factory.createProperty(client);
        Property otherProperty = factory.createProperty(client);
        Brand foreignBrand = factory.createBrand(otherProperty);
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode hsnCode = factory.createHsnCode(property);

        ItemRequest request = new ItemRequest(
                "SKU-010",
                null,
                "Widget",
                null,
                foreignBrand.getId(),
                taxRate.getId(),
                hsnCode.getId(),
                ItemUnit.PCS,
                new BigDecimal("10.00"),
                null,
                true);

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/items"), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("does not belong to property")));
    }

    @Test
    void createWithTaxRateFromDifferentPropertyReturnsBadRequest() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();
        Property property = factory.createProperty(client);
        Property otherProperty = factory.createProperty(client);
        TaxRate foreignTaxRate = factory.createTaxRate(otherProperty);
        HsnCode hsnCode = factory.createHsnCode(property);

        ItemRequest request = new ItemRequest(
                "SKU-011",
                null,
                "Widget",
                null,
                null,
                foreignTaxRate.getId(),
                hsnCode.getId(),
                ItemUnit.PCS,
                new BigDecimal("10.00"),
                null,
                true);

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/items"), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("does not belong to property")));
    }

    @Test
    void createWithHsnCodeFromDifferentPropertyReturnsBadRequest() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();
        Property property = factory.createProperty(client);
        Property otherProperty = factory.createProperty(client);
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode foreignHsnCode = factory.createHsnCode(otherProperty);

        ItemRequest request = new ItemRequest(
                "SKU-012",
                null,
                "Widget",
                null,
                null,
                taxRate.getId(),
                foreignHsnCode.getId(),
                ItemUnit.PCS,
                new BigDecimal("10.00"),
                null,
                true);

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/items"), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("does not belong to property")));
    }

    @Test
    void createRejectsDuplicateSkuWithinSameProperty() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode hsnCode = factory.createHsnCode(property);
        factory.createItem(property, taxRate, hsnCode); // has sku from unique("SKU")

        ItemRequest request = new ItemRequest(
                "DUP-SKU",
                null,
                "Widget A",
                null,
                null,
                taxRate.getId(),
                hsnCode.getId(),
                ItemUnit.PCS,
                new BigDecimal("10.00"),
                null,
                true);
        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/items"), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        ItemRequest duplicateRequest = new ItemRequest(
                "DUP-SKU",
                null,
                "Widget B",
                null,
                null,
                taxRate.getId(),
                hsnCode.getId(),
                ItemUnit.PCS,
                new BigDecimal("20.00"),
                null,
                true);
        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/items"), token)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void createRejectsDuplicateBarcodeWithinSameProperty() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode hsnCode = factory.createHsnCode(property);

        ItemRequest first = new ItemRequest(
                "BC-SKU-1",
                "DUP-BAR",
                "Widget A",
                null,
                null,
                taxRate.getId(),
                hsnCode.getId(),
                ItemUnit.PCS,
                new BigDecimal("10.00"),
                null,
                true);
        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/items"), token)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isOk());

        ItemRequest second = new ItemRequest(
                "BC-SKU-2",
                "DUP-BAR",
                "Widget B",
                null,
                null,
                taxRate.getId(),
                hsnCode.getId(),
                ItemUnit.PCS,
                new BigDecimal("20.00"),
                null,
                true);
        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/items"), token)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isConflict());
    }

    @ParameterizedTest
    @EnumSource(ItemUnit.class)
    void createAcceptsEveryItemUnit(ItemUnit unit) throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode hsnCode = factory.createHsnCode(property);

        ItemRequest request = new ItemRequest(
                TestEntityFactory.unique("SKU-UNIT"),
                null,
                "Widget " + unit,
                null,
                null,
                taxRate.getId(),
                hsnCode.getId(),
                unit,
                new BigDecimal("10.00"),
                null,
                true);

        mockMvc.perform(authJson(post("/api/v1/properties/" + property.getId() + "/items"), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unit").value(unit.name()));
    }

    @Test
    void updateChangesItemFields() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode hsnCode = factory.createHsnCode(property);
        Item item = factory.createItem(property, taxRate, hsnCode);

        ItemRequest request = new ItemRequest(
                "UPDATED-SKU",
                "UPDATED-BAR",
                "Updated Name",
                null,
                null,
                taxRate.getId(),
                hsnCode.getId(),
                ItemUnit.BOX,
                new BigDecimal("250.00"),
                new BigDecimal("200.00"),
                false);

        mockMvc.perform(authJson(put("/api/v1/items/" + item.getId()), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("UPDATED-SKU"))
                .andExpect(jsonPath("$.barcode").value("UPDATED-BAR"))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.unit").value("BOX"))
                .andExpect(jsonPath("$.sellingPrice").value(250.00))
                .andExpect(jsonPath("$.costPrice").value(200.00))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updateWithTaxRateFromDifferentPropertyReturnsBadRequest() throws Exception {
        String token = adminToken();
        Client client = factory.createClient();
        Property property = factory.createProperty(client);
        Property otherProperty = factory.createProperty(client);
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode hsnCode = factory.createHsnCode(property);
        Item item = factory.createItem(property, taxRate, hsnCode);
        TaxRate foreignTaxRate = factory.createTaxRate(otherProperty);

        ItemRequest request = new ItemRequest(
                item.getSku(),
                item.getBarcode(),
                item.getName(),
                null,
                null,
                foreignTaxRate.getId(),
                hsnCode.getId(),
                ItemUnit.PCS,
                new BigDecimal("10.00"),
                null,
                true);

        mockMvc.perform(authJson(put("/api/v1/items/" + item.getId()), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUnknownItemReturnsNotFound() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode hsnCode = factory.createHsnCode(property);

        ItemRequest request = new ItemRequest(
                "X-SKU",
                null,
                "X",
                null,
                null,
                taxRate.getId(),
                hsnCode.getId(),
                ItemUnit.PCS,
                new BigDecimal("10.00"),
                null,
                true);

        mockMvc.perform(authJson(put("/api/v1/items/999999"), token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRemovesTheItem() throws Exception {
        String token = adminToken();
        Property property = factory.createProperty(factory.createClient());
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode hsnCode = factory.createHsnCode(property);
        Item item = factory.createItem(property, taxRate, hsnCode);

        mockMvc.perform(auth(delete("/api/v1/items/" + item.getId()), token))
                .andExpect(status().isOk());

        mockMvc.perform(auth(get("/api/v1/items/" + item.getId()), token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUnknownItemReturnsNotFound() throws Exception {
        String token = adminToken();

        mockMvc.perform(auth(delete("/api/v1/items/999999"), token))
                .andExpect(status().isNotFound());
    }

    @Test
    void requestsWithoutABearerTokenAreRejected() throws Exception {
        Property property = factory.createProperty(factory.createClient());

        mockMvc.perform(get("/api/v1/properties/" + property.getId() + "/items"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void operatorRoleLacksMastersPermissionAndIsForbidden() throws Exception {
        Property property = factory.createProperty(factory.createClient());
        factory.createUser("itemop1", "pass1234", "Operator");
        String token = login("itemop1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/items"), token))
                .andExpect(status().isForbidden());
    }

    @Test
    void storeManagerWithoutPropertyAccessIsForbiddenByPropertyAccessInterceptor() throws Exception {
        Property property = factory.createProperty(factory.createClient());
        factory.createUser("itemmgr1", "pass1234", "Store Manager");
        String token = login("itemmgr1", "pass1234");

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/items"), token))
                .andExpect(status().isForbidden());
    }

    @Test
    void storeManagerWithPropertyAccessCanListItems() throws Exception {
        Property property = factory.createProperty(factory.createClient());
        TaxRate taxRate = factory.createTaxRate(property);
        HsnCode hsnCode = factory.createHsnCode(property);
        factory.createItem(property, taxRate, hsnCode);
        var manager = factory.createUser("itemmgr2", "pass1234", "Store Manager");
        factory.grantAccess(manager, property);
        String token = login("itemmgr2", "pass1234");

        mockMvc.perform(auth(get("/api/v1/properties/" + property.getId() + "/items"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
