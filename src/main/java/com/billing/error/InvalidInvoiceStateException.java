package com.billing.error;

import com.billing.domain.InvoiceStatus;

public class InvalidInvoiceStateException extends RuntimeException {
    public InvalidInvoiceStateException(String invoiceNumber, InvoiceStatus current) {
        super("Invoice " + invoiceNumber + " is " + current + "; only DRAFT invoices can be changed");
    }
}
