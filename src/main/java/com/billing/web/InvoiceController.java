package com.billing.web;

import com.billing.domain.InvoiceStatus;
import com.billing.service.InvoiceService;
import com.billing.web.dto.InvoiceRequest;
import com.billing.web.dto.InvoiceResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService service;

    @PostMapping
    public Mono<ResponseEntity<InvoiceResponse>> create(@Valid @RequestBody InvoiceRequest request) {
        return service.create(request)
                .map(InvoiceResponse::from)
                .map(body -> ResponseEntity.created(URI.create("/api/invoices/" + body.id())).body(body));
    }

    @GetMapping
    public Flux<InvoiceResponse> list(@RequestParam(required = false) InvoiceStatus status,
                                      @RequestParam(required = false) String customer) {
        return service.list(status, customer).map(InvoiceResponse::from);
    }

    @GetMapping("/summary")
    public Mono<Map<InvoiceStatus, BigDecimal>> summary() {
        return service.totalsByStatus();
    }

    @GetMapping("/{id}")
    public Mono<InvoiceResponse> get(@PathVariable Long id) {
        return service.get(id).map(InvoiceResponse::from);
    }

    @PutMapping("/{id}")
    public Mono<InvoiceResponse> update(@PathVariable Long id, @Valid @RequestBody InvoiceRequest request) {
        return service.update(id, request).map(InvoiceResponse::from);
    }

    @PostMapping("/{id}/pay")
    public Mono<InvoiceResponse> pay(@PathVariable Long id) {
        return service.markPaid(id).map(InvoiceResponse::from);
    }

    @PostMapping("/{id}/cancel")
    public Mono<InvoiceResponse> cancel(@PathVariable Long id) {
        return service.cancel(id).map(InvoiceResponse::from);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) {
        return service.delete(id);
    }
}
