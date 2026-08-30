package com.clevstack.clevbill.repository;

import com.clevstack.clevbill.model.Inventory;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Modifying
    @Query(
            value = "INSERT INTO inventories (item_id, quantity) VALUES (:itemId, 0) ON CONFLICT (item_id) DO NOTHING",
            nativeQuery = true)
    void ensureExists(Long itemId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.itemId = :itemId")
    Optional<Inventory> lockByItemId(Long itemId);

    List<Inventory> findByItemIdIn(List<Long> itemIds);
}
