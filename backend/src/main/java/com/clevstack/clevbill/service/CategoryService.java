package com.clevstack.clevbill.service;

import com.clevstack.clevbill.dto.CategoryRequest;
import com.clevstack.clevbill.dto.CategoryResponse;
import com.clevstack.clevbill.exception.ResourceNotFoundException;
import com.clevstack.clevbill.model.Category;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final PropertyService propertyService;

    public CategoryService(CategoryRepository categoryRepository, PropertyService propertyService) {
        this.categoryRepository = categoryRepository;
        this.propertyService = propertyService;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listByProperty(Long propertyId) {
        propertyService.findEntity(propertyId);
        return categoryRepository.findByPropertyId(propertyId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse get(Long id) {
        return toResponse(findEntity(id));
    }

    public CategoryResponse create(Long propertyId, CategoryRequest request) {
        Property property = propertyService.findEntity(propertyId);
        Category category = new Category();
        category.setProperty(property);
        category.setClient(property.getClient());
        applyRequest(category, request);
        return toResponse(categoryRepository.save(category));
    }

    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findEntity(id);
        applyRequest(category, request);
        return toResponse(categoryRepository.save(category));
    }

    public void delete(Long id) {
        categoryRepository.delete(findEntity(id));
    }

    Category findEntity(Long id) {
        return categoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    private void applyRequest(Category category, CategoryRequest request) {
        category.setName(request.name());
        category.setActive(request.active());
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getProperty().getId(),
                category.getClient().getId(),
                category.getName(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }
}
