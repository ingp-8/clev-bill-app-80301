package com.clevstack.clevbill.repository;

import com.clevstack.clevbill.model.Sale;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByPropertyId(Long propertyId);
}
