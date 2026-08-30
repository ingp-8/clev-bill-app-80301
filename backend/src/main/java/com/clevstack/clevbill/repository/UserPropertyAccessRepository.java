package com.clevstack.clevbill.repository;

import com.clevstack.clevbill.model.UserPropertyAccess;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPropertyAccessRepository extends JpaRepository<UserPropertyAccess, Long> {

    List<UserPropertyAccess> findByUserId(Long userId);

    Optional<UserPropertyAccess> findByUserIdAndPropertyId(Long userId, Long propertyId);

    boolean existsByUserIdAndPropertyId(Long userId, Long propertyId);
}
