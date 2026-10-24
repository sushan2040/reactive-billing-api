package com.billing.web.dto;

import com.billing.domain.Invoice;
import com.billing.domain.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record InvoiceResponse(
        Long id,
        String invoiceNumber,
        String customerName,
        BigDecimal amount,
        String currency,
        InvoiceStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public static InvoiceResponse from(Invoice i) {
        return new InvoiceResponse(i.getId(), i.getInvoiceNumber(), i.getCustomerName(), i.getAmount(),
                i.getCurrency(), i.getStatus(), i.getCreatedAt(), i.getUpdatedAt());
    }
}
