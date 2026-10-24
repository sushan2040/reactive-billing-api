package com.billing.error;

public class InvoiceNotFoundException extends RuntimeException {
    public InvoiceNotFoundException(Long id) {
        super("Invoice " + id + " not found");
    }
}
