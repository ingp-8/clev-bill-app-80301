package com.clevstack.clevbill.repository;

import com.clevstack.clevbill.model.Property;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findByClientId(Long clientId);
}
