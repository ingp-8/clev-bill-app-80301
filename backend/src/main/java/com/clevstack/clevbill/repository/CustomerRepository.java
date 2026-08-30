package com.clevstack.clevbill.repository;

import com.clevstack.clevbill.model.Customer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByClientId(Long clientId);
}
