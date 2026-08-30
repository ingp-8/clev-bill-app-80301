package com.clevstack.clevbill.repository;

import com.clevstack.clevbill.model.PriceList;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceListRepository extends JpaRepository<PriceList, Long> {

    List<PriceList> findByPropertyId(Long propertyId);
}
