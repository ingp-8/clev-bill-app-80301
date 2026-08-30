package com.clevstack.clevbill.repository;

import com.clevstack.clevbill.model.PartyPropertyAccess;
import com.clevstack.clevbill.model.PartyType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyPropertyAccessRepository extends JpaRepository<PartyPropertyAccess, Long> {

    List<PartyPropertyAccess> findByPartyTypeAndPartyId(PartyType partyType, Long partyId);

    void deleteByPartyTypeAndPartyIdAndPropertyId(PartyType partyType, Long partyId, Long propertyId);
}
