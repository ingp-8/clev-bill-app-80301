package com.clevstack.clevbill.service;

import com.clevstack.clevbill.dto.ClientRequest;
import com.clevstack.clevbill.dto.ClientResponse;
import com.clevstack.clevbill.exception.ResourceNotFoundException;
import com.clevstack.clevbill.model.Client;
import com.clevstack.clevbill.repository.ClientRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> list() {
        return clientRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ClientResponse get(Long id) {
        return toResponse(findEntity(id));
    }

    public ClientResponse create(ClientRequest request) {
        Client client = new Client();
        applyRequest(client, request);
        return toResponse(clientRepository.save(client));
    }

    public ClientResponse update(Long id, ClientRequest request) {
        Client client = findEntity(id);
        applyRequest(client, request);
        return toResponse(clientRepository.save(client));
    }

    public void delete(Long id) {
        clientRepository.delete(findEntity(id));
    }

    Client findEntity(Long id) {
        return clientRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Client not found: " + id));
    }

    private void applyRequest(Client client, ClientRequest request) {
        client.setClientName(request.clientName());
        client.setAddress(request.address());
        client.setEmail(request.email());
        client.setMobileNo(request.mobileNo());
        client.setLogoPath(request.logoPath());
        client.setActive(request.active());
    }

    private ClientResponse toResponse(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getClientName(),
                client.getAddress(),
                client.getEmail(),
                client.getMobileNo(),
                client.getLogoPath(),
                client.isActive(),
                client.getCreatedAt(),
                client.getUpdatedAt());
    }
}
