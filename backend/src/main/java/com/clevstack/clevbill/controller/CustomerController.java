package com.clevstack.clevbill.controller;

import com.clevstack.clevbill.dto.CustomerRequest;
import com.clevstack.clevbill.dto.CustomerResponse;
import com.clevstack.clevbill.service.CustomerService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PreAuthorize("hasAuthority('MASTERS_CUSTOMER:VIEW')")
    @GetMapping("/api/v1/clients/{clientId}/customers")
    public List<CustomerResponse> listByClient(@PathVariable Long clientId) {
        return customerService.listByClient(clientId);
    }

    @PreAuthorize("hasAuthority('MASTERS_CUSTOMER:VIEW')")
    @GetMapping("/api/v1/customers/{id}")
    public CustomerResponse get(@PathVariable Long id) {
        return customerService.get(id);
    }

    @PreAuthorize("hasAuthority('MASTERS_CUSTOMER:CREATE')")
    @PostMapping("/api/v1/clients/{clientId}/customers")
    public CustomerResponse create(@PathVariable Long clientId, @Valid @RequestBody CustomerRequest request) {
        return customerService.create(clientId, request);
    }

    @PreAuthorize("hasAuthority('MASTERS_CUSTOMER:EDIT')")
    @PutMapping("/api/v1/customers/{id}")
    public CustomerResponse update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return customerService.update(id, request);
    }

    @PreAuthorize("hasAuthority('MASTERS_CUSTOMER:DELETE')")
    @DeleteMapping("/api/v1/customers/{id}")
    public void delete(@PathVariable Long id) {
        customerService.delete(id);
    }
}
