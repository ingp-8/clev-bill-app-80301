package com.clevstack.clevbill.repository;

import com.clevstack.clevbill.model.Item;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByPropertyId(Long propertyId);

    Optional<Item> findByPropertyIdAndBarcode(Long propertyId, String barcode);
}
