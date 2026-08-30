package com.clevstack.clevbill.service;

import com.clevstack.clevbill.dto.CustomerRequest;
import com.clevstack.clevbill.dto.CustomerResponse;
import com.clevstack.clevbill.exception.ResourceNotFoundException;
import com.clevstack.clevbill.model.Client;
import com.clevstack.clevbill.model.Customer;
import com.clevstack.clevbill.model.PartyType;
import com.clevstack.clevbill.repository.CustomerRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final ClientService clientService;
    private final PartyPropertyAccessService partyPropertyAccessService;

    public CustomerService(
            CustomerRepository customerRepository,
            ClientService clientService,
            PartyPropertyAccessService partyPropertyAccessService) {
        this.customerRepository = customerRepository;
        this.clientService = clientService;
        this.partyPropertyAccessService = partyPropertyAccessService;
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> listByClient(Long clientId) {
        clientService.findEntity(clientId);
        return customerRepository.findByClientId(clientId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(Long id) {
        return toResponse(findEntity(id));
    }

    public CustomerResponse create(Long clientId, CustomerRequest request) {
        Client client = clientService.findEntity(clientId);
        Customer customer = new Customer();
        customer.setClient(client);
        applyRequest(customer, request);
        Customer saved = customerRepository.save(customer);
        partyPropertyAccessService.sync(PartyType.CUSTOMER, saved.getId(), clientId, request.propertyIds());
        return toResponse(saved);
    }

    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findEntity(id);
        applyRequest(customer, request);
        Customer saved = customerRepository.save(customer);
        partyPropertyAccessService.sync(PartyType.CUSTOMER, saved.getId(), saved.getClient().getId(), request.propertyIds());
        return toResponse(saved);
    }

    public void delete(Long id) {
        Customer customer = findEntity(id);
        partyPropertyAccessService.sync(PartyType.CUSTOMER, id, customer.getClient().getId(), List.of());
        customerRepository.delete(customer);
    }

    private Customer findEntity(Long id) {
        return customerRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    private void applyRequest(Customer customer, CustomerRequest request) {
        customer.setName(request.name());
        customer.setPhone(request.phone());
        customer.setEmail(request.email());
        customer.setGstin(request.gstin());
        customer.setAddress(request.address());
        customer.setActive(request.active());
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getClient().getId(),
                customer.getName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getGstin(),
                customer.getAddress(),
                customer.isActive(),
                partyPropertyAccessService.getPropertyIds(PartyType.CUSTOMER, customer.getId()),
                customer.getCreatedAt(),
                customer.getUpdatedAt());
    }
}
