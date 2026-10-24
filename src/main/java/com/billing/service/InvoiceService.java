package com.billing.service;

import com.billing.domain.Invoice;
import com.billing.domain.InvoiceStatus;
import com.billing.error.InvalidInvoiceStateException;
import com.billing.error.InvoiceNotFoundException;
import com.billing.repository.InvoiceRepository;
import com.billing.web.dto.InvoiceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository repository;

    public Mono<Invoice> create(InvoiceRequest request) {
        Instant now = Instant.now();
        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customerName(request.customerName().trim())
                .amount(request.amount())
                .currency(request.currency())
                .status(InvoiceStatus.DRAFT)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return repository.save(invoice);
    }

    /** Empty Mono from the repository (no row) becomes a 404 via switchIfEmpty. */
    public Mono<Invoice> get(Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new InvoiceNotFoundException(id)));
    }

    public Flux<Invoice> list(InvoiceStatus status, String customer) {
        if (status != null) {
            return repository.findByStatus(status);
        }
        if (customer != null && !customer.isBlank()) {
            return repository.findByCustomerNameContainingIgnoreCase(customer);
        }
        return repository.findAll();
    }

    /** flatMap because each step returns another Mono (load -> check -> save). */
    public Mono<Invoice> update(Long id, InvoiceRequest request) {
        return get(id)
                .flatMap(this::requireDraft)
                .flatMap(invoice -> {
                    invoice.setCustomerName(request.customerName().trim());
                    invoice.setAmount(request.amount());
                    invoice.setCurrency(request.currency());
                    invoice.setUpdatedAt(Instant.now());
                    return repository.save(invoice);
                });
    }

    public Mono<Invoice> markPaid(Long id) {
        return transition(id, InvoiceStatus.PAID);
    }

    public Mono<Invoice> cancel(Long id) {
        return transition(id, InvoiceStatus.CANCELLED);
    }

    public Mono<Void> delete(Long id) {
        return get(id)
                .flatMap(this::requireDraft)
                .flatMap(repository::delete);
    }

    /** Totals per status: collects the whole Flux into one Mono<Map>. */
    public Mono<Map<InvoiceStatus, BigDecimal>> totalsByStatus() {
        return repository.findAll()
                .collect(Collectors.groupingBy(Invoice::getStatus,
                        Collectors.reducing(BigDecimal.ZERO, Invoice::getAmount, BigDecimal::add)));
    }

    private Mono<Invoice> transition(Long id, InvoiceStatus target) {
        return get(id)
                .flatMap(this::requireDraft)
                .flatMap(invoice -> {
                    invoice.setStatus(target);
                    invoice.setUpdatedAt(Instant.now());
                    return repository.save(invoice);
                });
    }

    private Mono<Invoice> requireDraft(Invoice invoice) {
        return invoice.getStatus() == InvoiceStatus.DRAFT
                ? Mono.just(invoice)
                : Mono.<Invoice>error(new InvalidInvoiceStateException(invoice.getInvoiceNumber(), invoice.getStatus()));
    }
}
