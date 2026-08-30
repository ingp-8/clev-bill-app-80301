package com.clevstack.clevbill.repository;

import com.clevstack.clevbill.model.TaxRate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxRateRepository extends JpaRepository<TaxRate, Long> {

    List<TaxRate> findByPropertyId(Long propertyId);
}
