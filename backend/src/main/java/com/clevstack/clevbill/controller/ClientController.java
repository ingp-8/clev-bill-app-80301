package com.clevstack.clevbill.controller;

import com.clevstack.clevbill.dto.ClientRequest;
import com.clevstack.clevbill.dto.ClientResponse;
import com.clevstack.clevbill.service.ClientService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PreAuthorize("hasAuthority('CLIENT_MGMT:VIEW')")
    @GetMapping
    public List<ClientResponse> list() {
        return clientService.list();
    }

    @PreAuthorize("hasAuthority('CLIENT_MGMT:VIEW')")
    @GetMapping("/{id}")
    public ClientResponse get(@PathVariable Long id) {
        return clientService.get(id);
    }

    @PreAuthorize("hasAuthority('CLIENT_MGMT:CREATE')")
    @PostMapping
    public ClientResponse create(@Valid @RequestBody ClientRequest request) {
        return clientService.create(request);
    }

    @PreAuthorize("hasAuthority('CLIENT_MGMT:EDIT')")
    @PutMapping("/{id}")
    public ClientResponse update(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return clientService.update(id, request);
    }

    @PreAuthorize("hasAuthority('CLIENT_MGMT:DELETE')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        clientService.delete(id);
    }
}
