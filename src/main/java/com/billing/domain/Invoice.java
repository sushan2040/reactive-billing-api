package com.billing.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Table("invoices")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    @Id
    private Long id;
    private String invoiceNumber;
    private String customerName;
    private BigDecimal amount;
    private String currency;
    private InvoiceStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
