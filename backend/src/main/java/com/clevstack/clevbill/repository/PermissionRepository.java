package com.clevstack.clevbill.repository;

import com.clevstack.clevbill.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
