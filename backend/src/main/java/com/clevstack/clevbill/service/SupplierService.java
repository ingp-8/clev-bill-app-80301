package com.clevstack.clevbill.service;

import com.clevstack.clevbill.dto.SupplierRequest;
import com.clevstack.clevbill.dto.SupplierResponse;
import com.clevstack.clevbill.exception.ResourceNotFoundException;
import com.clevstack.clevbill.model.Client;
import com.clevstack.clevbill.model.PartyType;
import com.clevstack.clevbill.model.Supplier;
import com.clevstack.clevbill.repository.SupplierRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final ClientService clientService;
    private final PartyPropertyAccessService partyPropertyAccessService;

    public SupplierService(
            SupplierRepository supplierRepository,
            ClientService clientService,
            PartyPropertyAccessService partyPropertyAccessService) {
        this.supplierRepository = supplierRepository;
        this.clientService = clientService;
        this.partyPropertyAccessService = partyPropertyAccessService;
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> listByClient(Long clientId) {
        clientService.findEntity(clientId);
        return supplierRepository.findByClientId(clientId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SupplierResponse get(Long id) {
        return toResponse(findEntity(id));
    }

    public SupplierResponse create(Long clientId, SupplierRequest request) {
        Client client = clientService.findEntity(clientId);
        Supplier supplier = new Supplier();
        supplier.setClient(client);
        applyRequest(supplier, request);
        Supplier saved = supplierRepository.save(supplier);
        partyPropertyAccessService.sync(PartyType.SUPPLIER, saved.getId(), clientId, request.propertyIds());
        return toResponse(saved);
    }

    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = findEntity(id);
        applyRequest(supplier, request);
        Supplier saved = supplierRepository.save(supplier);
        partyPropertyAccessService.sync(PartyType.SUPPLIER, saved.getId(), saved.getClient().getId(), request.propertyIds());
        return toResponse(saved);
    }

    public void delete(Long id) {
        Supplier supplier = findEntity(id);
        partyPropertyAccessService.sync(PartyType.SUPPLIER, id, supplier.getClient().getId(), List.of());
        supplierRepository.delete(supplier);
    }

    private Supplier findEntity(Long id) {
        return supplierRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + id));
    }

    private void applyRequest(Supplier supplier, SupplierRequest request) {
        supplier.setName(request.name());
        supplier.setPhone(request.phone());
        supplier.setEmail(request.email());
        supplier.setGstin(request.gstin());
        supplier.setAddress(request.address());
        supplier.setActive(request.active());
    }

    private SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getClient().getId(),
                supplier.getName(),
                supplier.getPhone(),
                supplier.getEmail(),
                supplier.getGstin(),
                supplier.getAddress(),
                supplier.isActive(),
                partyPropertyAccessService.getPropertyIds(PartyType.SUPPLIER, supplier.getId()),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt());
    }
}
