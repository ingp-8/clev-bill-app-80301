package com.clevstack.clevbill.repository;

import com.clevstack.clevbill.model.InventoryTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    List<InventoryTransaction> findByItemIdOrderByCreatedAtDesc(Long itemId);
}
