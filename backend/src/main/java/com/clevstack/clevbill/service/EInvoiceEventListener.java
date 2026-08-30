package com.clevstack.clevbill.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class EInvoiceEventListener {

    private final EInvoiceService eInvoiceService;

    public EInvoiceEventListener(EInvoiceService eInvoiceService) {
        this.eInvoiceService = eInvoiceService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSaleCreated(SaleCreatedEvent event) {
        eInvoiceService.processForSale(event.saleId());
    }
}
