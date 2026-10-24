package com.billing.domain;

public enum InvoiceStatus {
    DRAFT,       // can still be edited, paid, cancelled or deleted
    PAID,        // final
    CANCELLED    // final
}
