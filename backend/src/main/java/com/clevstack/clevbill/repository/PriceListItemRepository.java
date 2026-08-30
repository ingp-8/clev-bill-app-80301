package com.clevstack.clevbill.repository;

import com.clevstack.clevbill.model.PriceListItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceListItemRepository extends JpaRepository<PriceListItem, Long> {

    List<PriceListItem> findByPriceListId(Long priceListId);

    Optional<PriceListItem> findByPriceListIdAndItemId(Long priceListId, Long itemId);
}
