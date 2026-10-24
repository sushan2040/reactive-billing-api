# Reactive Billing API

A non-blocking invoice management REST API built with **Spring WebFlux**, **Project Reactor** and
**Spring Data R2DBC** on **PostgreSQL**. Built while working through the
*Spring WebFlux Masterclass: High-Performance Reactive APIs* course.

**Stack:** Java 17, Spring Boot 3.3, Spring WebFlux, Spring Data R2DBC, PostgreSQL, Jakarta Validation,
Gradle, Docker Compose, JUnit 5, Mockito, Reactor Test (`StepVerifier`), `WebTestClient`.

## What it does

Create, read, update, delete and list invoices. An invoice starts as `DRAFT` and can be paid or
cancelled once. After that it is final and cannot be edited or deleted.

| Method | Path | Notes |
|---|---|---|
| POST | `/api/invoices` | Validates the body, returns `201` + `Location` |
| GET | `/api/invoices?status=DRAFT&customer=acme` | Streams a `Flux` of invoices |
| GET | `/api/invoices/{id}` | `404` if missing |
| PUT | `/api/invoices/{id}` | Only while `DRAFT`, otherwise `409` |
| POST | `/api/invoices/{id}/pay` | `DRAFT` -> `PAID` |
| POST | `/api/invoices/{id}/cancel` | `DRAFT` -> `CANCELLED` |
| DELETE | `/api/invoices/{id}` | Only while `DRAFT`, returns `204` |
| GET | `/api/invoices/summary` | Total amount per status |

## Run it

```bash
docker compose up -d postgres
gradle wrapper          # one time, or open the folder in IntelliJ
./gradlew bootRun

curl -s -X POST localhost:8080/api/invoices -H "Content-Type: application/json" \
  -d '{"customerName":"Acme Corp","amount":1500.00,"currency":"INR"}'
curl -s localhost:8080/api/invoices
curl -s -X POST localhost:8080/api/invoices/1/pay
curl -s -X POST localhost:8080/api/invoices/1/pay      # 409: already paid
curl -s localhost:8080/api/invoices/summary
```

Tests (no Docker needed): `./gradlew test`

## Where each concept lives

| Concept | File |
|---|---|
| Blocking vs non-blocking: the whole stack returns `Mono`/`Flux` and never waits on a thread | every class under `service/` and `web/` |
| R2DBC repository (non-blocking database access) | `repository/InvoiceRepository`, `application.yml`, `schema.sql` |
| `Mono` vs `Flux` | `get()` returns one `Mono`, `list()` returns a `Flux` |
| `flatMap` for chained async steps | `InvoiceService.update`, `transition`, `delete` |
| `switchIfEmpty` to turn "no row" into a 404 | `InvoiceService.get` |
| Collecting a stream into one result | `InvoiceService.totalsByStatus` |
| Input validation | `web/dto/InvoiceRequest` |
| Reactive error handling | `web/GlobalExceptionHandler` |
| `WebFilter` (cross-cutting logic) | `filter/RequestLoggingFilter` |
| Testing reactive code | `InvoiceServiceTest` (`StepVerifier`), `InvoiceControllerTest` (`WebTestClient`) |

## Roadmap

- Router Functions / functional endpoints
- Flyway migrations instead of `schema.sql`
- Integration tests with Testcontainers
- Pagination and optimistic locking
# reactive-billing-api
