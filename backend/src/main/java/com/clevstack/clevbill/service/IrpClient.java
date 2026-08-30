package com.clevstack.clevbill.service;

/**
 * Client for the government Invoice Registration Portal (e-invoice IRN
 * issuance). See the gst-compliance skill for the queueing contract this is
 * called under — never invoke synchronously from the checkout path.
 */
public interface IrpClient {

    IrpResponse submit(IrpRequest request);
}
