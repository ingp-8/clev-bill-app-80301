package com.clevstack.clevbill.repository;

import com.clevstack.clevbill.model.ReturnItem;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReturnItemRepository extends JpaRepository<ReturnItem, Long> {

    @Query("select coalesce(sum(ri.quantity), 0) from ReturnItem ri where ri.saleItem.id = :saleItemId")
    BigDecimal sumReturnedQuantity(Long saleItemId);
}
