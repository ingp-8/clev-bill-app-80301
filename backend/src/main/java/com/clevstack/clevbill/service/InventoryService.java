package com.clevstack.clevbill.service;

import com.clevstack.clevbill.dto.InventoryResponse;
import com.clevstack.clevbill.dto.InventoryTransactionResponse;
import com.clevstack.clevbill.exception.ResourceNotFoundException;
import com.clevstack.clevbill.model.Inventory;
import com.clevstack.clevbill.model.InventoryChangeReason;
import com.clevstack.clevbill.model.InventoryTransaction;
import com.clevstack.clevbill.model.Item;
import com.clevstack.clevbill.model.User;
import com.clevstack.clevbill.repository.InventoryRepository;
import com.clevstack.clevbill.repository.InventoryTransactionRepository;
import com.clevstack.clevbill.repository.ItemRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Owns the append-only inventory ledger (docs/ARCHITECTURE.md section 5).
 * {@code applyChange} is the only way stock levels move — never write
 * {@code inventories.quantity} directly. Deliberately does not refuse a
 * change that would take stock negative (see billing-transactions skill):
 * a sale is never blocked over a stock technicality, discrepancies show up
 * in reporting instead.
 */
@Service
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final ItemRepository itemRepository;

    public InventoryService(
            InventoryRepository inventoryRepository,
            InventoryTransactionRepository inventoryTransactionRepository,
            ItemRepository itemRepository) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void applyChange(
            Long itemId, BigDecimal changeQuantity, InventoryChangeReason reason, Long referenceId, String note, User actor) {
        Item item = itemRepository
                .findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setItem(item);
        transaction.setChangeQuantity(changeQuantity);
        transaction.setReason(reason);
        transaction.setReferenceId(referenceId);
        transaction.setNote(note);
        transaction.setCreatedBy(actor);
        inventoryTransactionRepository.save(transaction);

        inventoryRepository.ensureExists(itemId);
        Inventory inventory = inventoryRepository.lockByItemId(itemId).orElseThrow();
        inventory.setQuantity(inventory.getQuantity().add(changeQuantity));
        inventoryRepository.save(inventory);
    }

    public InventoryResponse adjust(Long itemId, BigDecimal quantityDelta, String note, User actor) {
        applyChange(itemId, quantityDelta, InventoryChangeReason.ADJUSTMENT, null, note, actor);
        return getStock(itemId);
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> listAll() {
        List<Item> items = itemRepository.findAll();
        Map<Long, BigDecimal> quantities = new HashMap<>();
        inventoryRepository
                .findByItemIdIn(items.stream().map(Item::getId).toList())
                .forEach(inventory -> quantities.put(inventory.getItemId(), inventory.getQuantity()));

        return items.stream()
                .map(item -> new InventoryResponse(
                        item.getId(), item.getSku(), item.getName(), quantities.getOrDefault(item.getId(), BigDecimal.ZERO)))
                .toList();
    }

    @Transactional(readOnly = true)
    public InventoryResponse getStock(Long itemId) {
        Item item = itemRepository
                .findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));
        BigDecimal quantity =
                inventoryRepository.findById(itemId).map(Inventory::getQuantity).orElse(BigDecimal.ZERO);
        return new InventoryResponse(item.getId(), item.getSku(), item.getName(), quantity);
    }

    @Transactional(readOnly = true)
    public List<InventoryTransactionResponse> getTransactions(Long itemId) {
        return inventoryTransactionRepository.findByItemIdOrderByCreatedAtDesc(itemId).stream()
                .map(transaction -> new InventoryTransactionResponse(
                        transaction.getId(),
                        transaction.getItem().getId(),
                        transaction.getChangeQuantity(),
                        transaction.getReason(),
                        transaction.getReferenceId(),
                        transaction.getNote(),
                        transaction.getCreatedBy() == null ? null : transaction.getCreatedBy().getFullName(),
                        transaction.getCreatedAt()))
                .toList();
    }
}
