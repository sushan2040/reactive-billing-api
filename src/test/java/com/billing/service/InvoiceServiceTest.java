package com.billing.service;

import com.billing.domain.Invoice;
import com.billing.domain.InvoiceStatus;
import com.billing.error.InvalidInvoiceStateException;
import com.billing.error.InvoiceNotFoundException;
import com.billing.repository.InvoiceRepository;
import com.billing.web.dto.InvoiceRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock InvoiceRepository repository;
    @InjectMocks InvoiceService service;

    private static Invoice invoice(Long id, InvoiceStatus status, String amount) {
        return Invoice.builder().id(id).invoiceNumber("INV-" + id).customerName("Acme")
                .amount(new BigDecimal(amount)).currency("INR").status(status)
                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }

    /** Makes repository.save(x) return Mono.just(x), like a real save would. */
    private void saveReturnsArgument() {
        when(repository.save(any(Invoice.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
    }

    @Test
    void create_startsAsDraftWithGeneratedNumber() {
        saveReturnsArgument();

        StepVerifier.create(service.create(new InvoiceRequest("  Acme Corp ", new BigDecimal("100.00"), "INR")))
                .assertNext(created -> {
                    assertThat(created.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
                    assertThat(created.getInvoiceNumber()).startsWith("INV-");
                    assertThat(created.getCustomerName()).isEqualTo("Acme Corp");   // trimmed
                })
                .verifyComplete();
    }

    @Test
    void get_missingInvoiceEmitsNotFoundError() {
        when(repository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(service.get(99L))
                .expectError(InvoiceNotFoundException.class)
                .verify();
    }

    @Test
    void markPaid_draftBecomesPaid() {
        when(repository.findById(1L)).thenReturn(Mono.just(invoice(1L, InvoiceStatus.DRAFT, "50.00")));
        saveReturnsArgument();

        StepVerifier.create(service.markPaid(1L))
                .assertNext(paid -> assertThat(paid.getStatus()).isEqualTo(InvoiceStatus.PAID))
                .verifyComplete();
    }

    @Test
    void markPaid_alreadyPaidIsRejectedAndNothingIsSaved() {
        when(repository.findById(2L)).thenReturn(Mono.just(invoice(2L, InvoiceStatus.PAID, "50.00")));

        StepVerifier.create(service.markPaid(2L))
                .expectError(InvalidInvoiceStateException.class)
                .verify();

        verify(repository, never()).save(any());
    }

    @Test
    void delete_paidInvoiceIsRejected() {
        when(repository.findById(3L)).thenReturn(Mono.just(invoice(3L, InvoiceStatus.PAID, "10.00")));

        StepVerifier.create(service.delete(3L))
                .expectError(InvalidInvoiceStateException.class)
                .verify();

        verify(repository, never()).delete(any());
    }

    @Test
    void totalsByStatus_sumsAmountsPerStatus() {
        when(repository.findAll()).thenReturn(Flux.just(
                invoice(1L, InvoiceStatus.DRAFT, "10.00"),
                invoice(2L, InvoiceStatus.DRAFT, "5.50"),
                invoice(3L, InvoiceStatus.PAID, "20.00")));

        StepVerifier.create(service.totalsByStatus())
                .assertNext(totals -> {
                    assertThat(totals.get(InvoiceStatus.DRAFT)).isEqualByComparingTo("15.50");
                    assertThat(totals.get(InvoiceStatus.PAID)).isEqualByComparingTo("20.00");
                })
                .verifyComplete();
    }
}
