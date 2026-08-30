package com.clevstack.clevbill.repository;

import com.clevstack.clevbill.model.PosTerminal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PosTerminalRepository extends JpaRepository<PosTerminal, Long> {

    List<PosTerminal> findByPropertyId(Long propertyId);
}
