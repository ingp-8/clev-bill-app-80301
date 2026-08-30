package com.clevstack.clevbill.repository;

import com.clevstack.clevbill.model.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByPropertyId(Long propertyId);
}
