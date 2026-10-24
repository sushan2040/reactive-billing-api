package com.billing.web;

import com.billing.domain.Invoice;
import com.billing.domain.InvoiceStatus;
import com.billing.error.InvalidInvoiceStateException;
import com.billing.error.InvoiceNotFoundException;
import com.billing.filter.RequestLoggingFilter;
import com.billing.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(InvoiceController.class)
@Import({GlobalExceptionHandler.class, RequestLoggingFilter.class})
class InvoiceControllerTest {

    @Autowired WebTestClient client;
    @MockBean InvoiceService service;

    private static Invoice sample() {
        return Invoice.builder().id(1L).invoiceNumber("INV-TEST0001").customerName("Acme Corp")
                .amount(new BigDecimal("1500.00")).currency("INR").status(InvoiceStatus.DRAFT)
                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }

    @Test
    void createInvoice_returns201WithLocation() {
        when(service.create(any())).thenReturn(Mono.just(sample()));

        client.post().uri("/api/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("customerName", "Acme Corp", "amount", new BigDecimal("1500.00"), "currency", "INR"))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals("Location", "/api/invoices/1")
                .expectBody()
                .jsonPath("$.invoiceNumber").isEqualTo("INV-TEST0001")
                .jsonPath("$.status").isEqualTo("DRAFT");
    }

    @Test
    void createInvoice_withInvalidBodyReturns400WithFieldErrors() {
        client.post().uri("/api/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("customerName", " ", "amount", -5, "currency", "XYZ"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors.customerName").exists()
                .jsonPath("$.errors.amount").exists()
                .jsonPath("$.errors.currency").exists();
    }

    @Test
    void getInvoice_unknownIdReturns404() {
        when(service.get(99L)).thenReturn(Mono.error(new InvoiceNotFoundException(99L)));

        client.get().uri("/api/invoices/99")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Invoice not found");
    }

    @Test
    void payInvoice_alreadyPaidReturns409() {
        when(service.markPaid(1L)).thenReturn(Mono.error(new InvalidInvoiceStateException("INV-1", InvoiceStatus.PAID)));

        client.post().uri("/api/invoices/1/pay")
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void listInvoices_returnsJsonArray() {
        when(service.list(null, null)).thenReturn(Flux.just(sample()));

        client.get().uri("/api/invoices")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].customerName").isEqualTo("Acme Corp");
    }

    @Test
    void webFilter_addsRequestIdHeaderToEveryResponse() {
        when(service.get(1L)).thenReturn(Mono.just(sample()));

        client.get().uri("/api/invoices/1")
                .header("X-Request-Id", "abc-123")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Request-Id", "abc-123");
    }
}
