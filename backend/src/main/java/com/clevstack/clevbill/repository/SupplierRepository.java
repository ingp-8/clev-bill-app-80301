package com.clevstack.clevbill.repository;

import com.clevstack.clevbill.model.Supplier;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    List<Supplier> findByClientId(Long clientId);
}
