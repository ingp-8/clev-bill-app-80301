package com.clevstack.clevbill.service;

import com.clevstack.clevbill.exception.ResourceNotFoundException;
import com.clevstack.clevbill.model.PartyPropertyAccess;
import com.clevstack.clevbill.model.PartyType;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.repository.PartyPropertyAccessRepository;
import com.clevstack.clevbill.repository.PropertyRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Which properties a Customer or Supplier serves — shared by both since
 * the underlying mapping table is a single polymorphic one (see
 * PartyPropertyAccess). A customer/supplier belongs to one client but can
 * be granted access to any subset of that client's properties.
 */
@Service
@Transactional
public class PartyPropertyAccessService {

    private final PartyPropertyAccessRepository partyPropertyAccessRepository;
    private final PropertyRepository propertyRepository;

    public PartyPropertyAccessService(
            PartyPropertyAccessRepository partyPropertyAccessRepository, PropertyRepository propertyRepository) {
        this.partyPropertyAccessRepository = partyPropertyAccessRepository;
        this.propertyRepository = propertyRepository;
    }

    /** Replaces this party's granted properties with exactly the given set. */
    public void sync(PartyType partyType, Long partyId, Long clientId, List<Long> propertyIds) {
        List<PartyPropertyAccess> existing = partyPropertyAccessRepository.findByPartyTypeAndPartyId(partyType, partyId);
        Set<Long> existingPropertyIds = new HashSet<>();
        for (PartyPropertyAccess access : existing) {
            existingPropertyIds.add(access.getProperty().getId());
        }

        Set<Long> desired = new HashSet<>(propertyIds);
        for (PartyPropertyAccess access : existing) {
            if (!desired.contains(access.getProperty().getId())) {
                partyPropertyAccessRepository.delete(access);
            }
        }

        for (Long propertyId : desired) {
            if (existingPropertyIds.contains(propertyId)) {
                continue;
            }
            Property property = propertyRepository
                    .findById(propertyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));
            if (!property.getClient().getId().equals(clientId)) {
                throw new IllegalArgumentException(
                        "Property %d does not belong to client %d".formatted(propertyId, clientId));
            }
            PartyPropertyAccess access = new PartyPropertyAccess();
            access.setPartyType(partyType);
            access.setPartyId(partyId);
            access.setProperty(property);
            access.setClient(property.getClient());
            partyPropertyAccessRepository.save(access);
        }
    }

    @Transactional(readOnly = true)
    public List<Long> getPropertyIds(PartyType partyType, Long partyId) {
        return partyPropertyAccessRepository.findByPartyTypeAndPartyId(partyType, partyId).stream()
                .map(access -> access.getProperty().getId())
                .toList();
    }
}
