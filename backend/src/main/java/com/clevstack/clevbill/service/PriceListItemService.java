package com.clevstack.clevbill.service;

import com.clevstack.clevbill.dto.MasterRefResponse;
import com.clevstack.clevbill.dto.PriceListItemRequest;
import com.clevstack.clevbill.dto.PriceListItemResponse;
import com.clevstack.clevbill.exception.ResourceNotFoundException;
import com.clevstack.clevbill.model.Item;
import com.clevstack.clevbill.model.PriceList;
import com.clevstack.clevbill.model.PriceListItem;
import com.clevstack.clevbill.repository.ItemRepository;
import com.clevstack.clevbill.repository.PriceListItemRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PriceListItemService {

    private final PriceListItemRepository priceListItemRepository;
    private final PriceListService priceListService;
    private final ItemRepository itemRepository;

    public PriceListItemService(
            PriceListItemRepository priceListItemRepository,
            PriceListService priceListService,
            ItemRepository itemRepository) {
        this.priceListItemRepository = priceListItemRepository;
        this.priceListService = priceListService;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public List<PriceListItemResponse> list(Long priceListId) {
        priceListService.findEntity(priceListId);
        return priceListItemRepository.findByPriceListId(priceListId).stream()
                .map(this::toResponse)
                .toList();
    }

    public PriceListItemResponse create(Long priceListId, PriceListItemRequest request) {
        PriceList priceList = priceListService.findEntity(priceListId);
        Item item = itemRepository
                .findById(request.itemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + request.itemId()));

        PriceListItem priceListItem = new PriceListItem();
        priceListItem.setPriceList(priceList);
        priceListItem.setItem(item);
        priceListItem.setPrice(request.price());
        return toResponse(priceListItemRepository.save(priceListItem));
    }

    public PriceListItemResponse update(Long priceListId, Long id, PriceListItemRequest request) {
        PriceListItem priceListItem = findEntity(priceListId, id);
        Item item = itemRepository
                .findById(request.itemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + request.itemId()));
        priceListItem.setItem(item);
        priceListItem.setPrice(request.price());
        return toResponse(priceListItemRepository.save(priceListItem));
    }

    public void delete(Long priceListId, Long id) {
        priceListItemRepository.delete(findEntity(priceListId, id));
    }

    private PriceListItem findEntity(Long priceListId, Long id) {
        PriceListItem priceListItem = priceListItemRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Price list item not found: " + id));
        if (!priceListItem.getPriceList().getId().equals(priceListId)) {
            throw new ResourceNotFoundException("Price list item not found: " + id);
        }
        return priceListItem;
    }

    private PriceListItemResponse toResponse(PriceListItem priceListItem) {
        Item item = priceListItem.getItem();
        return new PriceListItemResponse(
                priceListItem.getId(),
                priceListItem.getPriceList().getId(),
                new MasterRefResponse(item.getId(), item.getName()),
                priceListItem.getPrice(),
                priceListItem.getCreatedAt(),
                priceListItem.getUpdatedAt());
    }
}
