package com.billing.repository;

import com.billing.domain.Invoice;
import com.billing.domain.InvoiceStatus;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

/** Spring Data R2DBC: same idea as JpaRepository, but every method returns Mono/Flux. */
public interface InvoiceRepository extends ReactiveCrudRepository<Invoice, Long> {

    Flux<Invoice> findByStatus(InvoiceStatus status);

    Flux<Invoice> findByCustomerNameContainingIgnoreCase(String customerName);
}
