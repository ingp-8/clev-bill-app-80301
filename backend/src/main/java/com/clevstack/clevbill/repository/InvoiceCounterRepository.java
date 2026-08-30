package com.clevstack.clevbill.repository;

import com.clevstack.clevbill.model.InvoiceCounter;
import com.clevstack.clevbill.model.InvoiceCounterId;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceCounterRepository extends JpaRepository<InvoiceCounter, InvoiceCounterId> {

    @Modifying
    @Query(
            value =
                    "INSERT INTO invoice_counters (property_id, financial_year, next_number) VALUES (:propertyId, :fy, 1) "
                            + "ON CONFLICT (property_id, financial_year) DO NOTHING",
            nativeQuery = true)
    void ensureExists(@Param("propertyId") Long propertyId, @Param("fy") String fy);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from InvoiceCounter c where c.property.id = :propertyId and c.financialYear = :fy")
    Optional<InvoiceCounter> lockByPropertyAndFinancialYear(@Param("propertyId") Long propertyId, @Param("fy") String fy);
}
