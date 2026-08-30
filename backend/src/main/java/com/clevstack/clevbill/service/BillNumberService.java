package com.clevstack.clevbill.service;

import com.clevstack.clevbill.model.InvoiceCounter;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.repository.InvoiceCounterRepository;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillNumberService {

    private final InvoiceCounterRepository invoiceCounterRepository;

    public BillNumberService(InvoiceCounterRepository invoiceCounterRepository) {
        this.invoiceCounterRepository = invoiceCounterRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public String nextBillNumber(Property property) {
        String financialYear = currentFinancialYear();
        invoiceCounterRepository.ensureExists(property.getId(), financialYear);
        InvoiceCounter counter = invoiceCounterRepository
                .lockByPropertyAndFinancialYear(property.getId(), financialYear)
                .orElseThrow();

        long sequence = counter.getNextNumber();
        counter.setNextNumber(sequence + 1);
        invoiceCounterRepository.save(counter);

        return "%s-%s-%05d".formatted(property.getInvoiceSeriesPrefix(), financialYear, sequence);
    }

    private String currentFinancialYear() {
        LocalDate today = LocalDate.now();
        int startYear = today.getMonthValue() >= 4 ? today.getYear() : today.getYear() - 1;
        int endYearShort = (startYear + 1) % 100;
        return "%d-%02d".formatted(startYear, endYearShort);
    }
}
