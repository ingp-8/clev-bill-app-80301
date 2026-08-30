package com.clevstack.clevbill.service;

public record IrpResponse(boolean success, String irn, String signedQrCode, String errorMessage) {
}
