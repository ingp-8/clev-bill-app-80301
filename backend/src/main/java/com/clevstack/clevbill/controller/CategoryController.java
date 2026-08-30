package com.clevstack.clevbill.controller;

import com.clevstack.clevbill.dto.CategoryRequest;
import com.clevstack.clevbill.dto.CategoryResponse;
import com.clevstack.clevbill.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PreAuthorize("hasAuthority('MASTERS_CATEGORY:VIEW')")
    @GetMapping("/api/v1/properties/{propertyId}/categories")
    public List<CategoryResponse> listByProperty(@PathVariable Long propertyId) {
        return categoryService.listByProperty(propertyId);
    }

    @PreAuthorize("hasAuthority('MASTERS_CATEGORY:VIEW')")
    @GetMapping("/api/v1/categories/{id}")
    public CategoryResponse get(@PathVariable Long id) {
        return categoryService.get(id);
    }

    @PreAuthorize("hasAuthority('MASTERS_CATEGORY:CREATE')")
    @PostMapping("/api/v1/properties/{propertyId}/categories")
    public CategoryResponse create(@PathVariable Long propertyId, @Valid @RequestBody CategoryRequest request) {
        return categoryService.create(propertyId, request);
    }

    @PreAuthorize("hasAuthority('MASTERS_CATEGORY:EDIT')")
    @PutMapping("/api/v1/categories/{id}")
    public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(id, request);
    }

    @PreAuthorize("hasAuthority('MASTERS_CATEGORY:DELETE')")
    @DeleteMapping("/api/v1/categories/{id}")
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}
