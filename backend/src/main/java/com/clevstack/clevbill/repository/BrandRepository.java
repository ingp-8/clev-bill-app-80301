package com.clevstack.clevbill.repository;

import com.clevstack.clevbill.model.Brand;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    List<Brand> findByPropertyId(Long propertyId);
}
