package com.clevstack.clevbill.repository;

import com.clevstack.clevbill.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
}
