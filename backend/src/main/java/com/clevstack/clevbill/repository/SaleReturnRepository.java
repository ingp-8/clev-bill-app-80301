package com.clevstack.clevbill.repository;

import com.clevstack.clevbill.model.SaleReturn;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleReturnRepository extends JpaRepository<SaleReturn, Long> {

    List<SaleReturn> findBySaleId(Long saleId);
}
