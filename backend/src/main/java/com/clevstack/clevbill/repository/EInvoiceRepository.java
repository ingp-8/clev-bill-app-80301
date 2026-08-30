package com.clevstack.clevbill.repository;

import com.clevstack.clevbill.model.EInvoice;
import com.clevstack.clevbill.model.EInvoiceStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EInvoiceRepository extends JpaRepository<EInvoice, Long> {

    Optional<EInvoice> findBySaleId(Long saleId);

    List<EInvoice> findByStatusInAndAttemptsLessThan(List<EInvoiceStatus> statuses, int maxAttempts);
}
