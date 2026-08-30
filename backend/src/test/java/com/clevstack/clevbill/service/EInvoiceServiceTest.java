package com.clevstack.clevbill.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clevstack.clevbill.model.EInvoice;
import com.clevstack.clevbill.model.EInvoiceStatus;
import com.clevstack.clevbill.model.Sale;
import com.clevstack.clevbill.repository.EInvoiceRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@code attempt(Long)} touches no {@code self}-proxy indirection (unlike
 * processForSale/retryOutstanding, which exist purely to route through the
 * Spring AOP proxy for @Transactional/@Async — see the class javadoc), so
 * it's testable as a plain Mockito unit against a stubbed IrpClient,
 * without booting Spring or hitting the (randomly-failing) MockIrpClient.
 */
class EInvoiceServiceTest {

    private EInvoiceRepository eInvoiceRepository;
    private IrpClient irpClient;
    private EInvoiceService service;

    @BeforeEach
    void setUp() {
        eInvoiceRepository = mock(EInvoiceRepository.class);
        irpClient = mock(IrpClient.class);
        service = new EInvoiceService(eInvoiceRepository, irpClient, null);
    }

    private EInvoice pendingEInvoice() {
        Sale sale = new Sale();
        sale.setBillNumber("PROP-2026-27-00001");
        sale.setTotalAmount(new BigDecimal("236.00"));

        EInvoice eInvoice = new EInvoice();
        eInvoice.setId(1L);
        eInvoice.setSale(sale);
        eInvoice.setStatus(EInvoiceStatus.PENDING);
        eInvoice.setAttempts(0);
        return eInvoice;
    }

    @Test
    void successfulSubmissionMarksSuccessAndStoresIrnAndQr() {
        EInvoice eInvoice = pendingEInvoice();
        when(eInvoiceRepository.findById(1L)).thenReturn(Optional.of(eInvoice));
        when(irpClient.submit(any())).thenReturn(new IrpResponse(true, "irn-123", "qr-payload", null));

        service.attempt(1L);

        assertThat(eInvoice.getStatus()).isEqualTo(EInvoiceStatus.SUCCESS);
        assertThat(eInvoice.getIrn()).isEqualTo("irn-123");
        assertThat(eInvoice.getSignedQrCode()).isEqualTo("qr-payload");
        assertThat(eInvoice.getLastError()).isNull();
        assertThat(eInvoice.getAttempts()).isEqualTo(1);
        verify(eInvoiceRepository).save(eInvoice);
    }

    @Test
    void failedSubmissionMarksFailedAndRecordsError() {
        EInvoice eInvoice = pendingEInvoice();
        when(eInvoiceRepository.findById(1L)).thenReturn(Optional.of(eInvoice));
        when(irpClient.submit(any())).thenReturn(new IrpResponse(false, null, null, "IRP timeout"));

        service.attempt(1L);

        assertThat(eInvoice.getStatus()).isEqualTo(EInvoiceStatus.FAILED);
        assertThat(eInvoice.getLastError()).isEqualTo("IRP timeout");
        assertThat(eInvoice.getIrn()).isNull();
    }

    @Test
    void alreadySuccessfulEInvoiceIsNotResubmitted() {
        EInvoice eInvoice = pendingEInvoice();
        eInvoice.setStatus(EInvoiceStatus.SUCCESS);
        when(eInvoiceRepository.findById(1L)).thenReturn(Optional.of(eInvoice));

        service.attempt(1L);

        verify(irpClient, never()).submit(any());
        verify(eInvoiceRepository, never()).save(any());
    }

    @Test
    void missingEInvoiceIsANoOp() {
        when(eInvoiceRepository.findById(99L)).thenReturn(Optional.empty());

        service.attempt(99L);

        verify(irpClient, never()).submit(any());
    }

    @Test
    void requestSentToIrpCarriesBillNumberAndTotal() {
        EInvoice eInvoice = pendingEInvoice();
        when(eInvoiceRepository.findById(1L)).thenReturn(Optional.of(eInvoice));
        when(irpClient.submit(any())).thenReturn(new IrpResponse(true, "irn-123", "qr", null));

        service.attempt(1L);

        verify(irpClient).submit(eq(new IrpRequest("PROP-2026-27-00001", new BigDecimal("236.00"))));
    }
}
