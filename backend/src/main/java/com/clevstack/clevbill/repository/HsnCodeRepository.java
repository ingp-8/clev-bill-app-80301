package com.clevstack.clevbill.repository;

import com.clevstack.clevbill.model.HsnCode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HsnCodeRepository extends JpaRepository<HsnCode, Long> {

    List<HsnCode> findByPropertyId(Long propertyId);
}
