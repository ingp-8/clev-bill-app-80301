package com.clevstack.clevbill.service;

import com.clevstack.clevbill.dto.EInvoiceResponse;
import com.clevstack.clevbill.exception.ResourceNotFoundException;
import com.clevstack.clevbill.model.EInvoice;
import com.clevstack.clevbill.model.EInvoiceStatus;
import com.clevstack.clevbill.model.Sale;
import com.clevstack.clevbill.repository.EInvoiceRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Queues and processes e-invoice IRN requests. See the gst-compliance skill:
 * the bill is printed immediately from local data, this pipeline runs
 * asynchronously after the sale transaction commits, and retries with
 * backoff on failure rather than treating it as best-effort.
 */
@Service
public class EInvoiceService {

    private static final Logger log = LoggerFactory.getLogger(EInvoiceService.class);
    private static final int MAX_ATTEMPTS = 5;
    private static final long BACKOFF_SECONDS_PER_ATTEMPT = 30;

    private final EInvoiceRepository eInvoiceRepository;
    private final IrpClient irpClient;

    /**
     * Self-reference obtained through the Spring proxy, not {@code this}.
     * {@code attempt} is {@code @Transactional}; calling it directly from
     * another method in this class would bypass the AOP proxy and silently
     * run without a transaction (classic Spring self-invocation pitfall).
     */
    private final EInvoiceService self;

    public EInvoiceService(EInvoiceRepository eInvoiceRepository, IrpClient irpClient, @Lazy EInvoiceService self) {
        this.eInvoiceRepository = eInvoiceRepository;
        this.irpClient = irpClient;
        this.self = self;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void queueIfEnabled(Sale sale) {
        if (!sale.getProperty().isEInvoiceEnabled()) {
            return;
        }
        EInvoice eInvoice = new EInvoice();
        eInvoice.setSale(sale);
        eInvoice.setStatus(EInvoiceStatus.PENDING);
        eInvoiceRepository.save(eInvoice);
    }

    @Transactional(readOnly = true)
    public EInvoiceResponse getForSale(Long saleId) {
        EInvoice eInvoice = eInvoiceRepository
                .findBySaleId(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("No e-invoice queued for sale: " + saleId));
        return new EInvoiceResponse(
                eInvoice.getId(),
                saleId,
                eInvoice.getStatus(),
                eInvoice.getIrn(),
                eInvoice.getSignedQrCode(),
                eInvoice.getAttempts(),
                eInvoice.getLastError(),
                eInvoice.getLastAttemptAt());
    }

    @Async
    public void processForSale(Long saleId) {
        eInvoiceRepository.findBySaleId(saleId).ifPresent(eInvoice -> self.attempt(eInvoice.getId()));
    }

    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    public void retryOutstanding() {
        List<EInvoice> candidates = eInvoiceRepository.findByStatusInAndAttemptsLessThan(
                List.of(EInvoiceStatus.PENDING, EInvoiceStatus.FAILED), MAX_ATTEMPTS);

        Instant now = Instant.now();
        for (EInvoice eInvoice : candidates) {
            Instant lastAttempt = eInvoice.getLastAttemptAt();
            long backoffSeconds = BACKOFF_SECONDS_PER_ATTEMPT * Math.max(1, eInvoice.getAttempts());
            if (lastAttempt != null && lastAttempt.plusSeconds(backoffSeconds).isAfter(now)) {
                continue;
            }
            self.attempt(eInvoice.getId());
        }
    }

    @Transactional
    public void attempt(Long eInvoiceId) {
        EInvoice eInvoice = eInvoiceRepository.findById(eInvoiceId).orElse(null);
        if (eInvoice == null || eInvoice.getStatus() == EInvoiceStatus.SUCCESS) {
            return;
        }

        eInvoice.setAttempts(eInvoice.getAttempts() + 1);
        eInvoice.setLastAttemptAt(Instant.now());

        Sale sale = eInvoice.getSale();
        IrpResponse response = irpClient.submit(new IrpRequest(sale.getBillNumber(), sale.getTotalAmount()));

        if (response.success()) {
            eInvoice.setStatus(EInvoiceStatus.SUCCESS);
            eInvoice.setIrn(response.irn());
            eInvoice.setSignedQrCode(response.signedQrCode());
            eInvoice.setLastError(null);
        } else {
            eInvoice.setStatus(EInvoiceStatus.FAILED);
            eInvoice.setLastError(response.errorMessage());
            log.warn(
                    "IRN submission failed for sale {} (attempt {}): {}",
                    sale.getId(),
                    eInvoice.getAttempts(),
                    response.errorMessage());
        }

        eInvoiceRepository.save(eInvoice);
    }
}
