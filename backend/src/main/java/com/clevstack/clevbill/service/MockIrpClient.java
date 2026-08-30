package com.clevstack.clevbill.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Placeholder IRP integration — there is no real government IRP sandbox
 * account wired up in this environment. Generates a well-formed-looking IRN
 * (64-char hex, matching the real NIC hash format) and a stub QR payload, and
 * occasionally simulates a failure so the retry path in EInvoiceService is
 * exercised. Replace with a real IRP client (auth, e-invoice API calls,
 * signed QR from the response) before going live.
 */
@Service
public class MockIrpClient implements IrpClient {

    private static final double SIMULATED_FAILURE_RATE = 0.1;

    private final SecureRandom random = new SecureRandom();

    @Override
    public IrpResponse submit(IrpRequest request) {
        if (random.nextDouble() < SIMULATED_FAILURE_RATE) {
            return new IrpResponse(false, null, null, "Simulated IRP timeout — mock client, no real IRP configured");
        }

        String irn = sha256Hex(request.billNumber() + "|" + UUID.randomUUID() + "|" + Instant.now());
        String qrCode = "clevbill-mock-qr:%s:%s".formatted(request.billNumber(), irn.substring(0, 16));
        return new IrpResponse(true, irn, qrCode, null);
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
