# DineFlow

![CI](https://github.com/olimjonof/dineflow/actions/workflows/ci.yml/badge.svg)

Multi-tenant restaurant reservation API built with Java 17, Spring Boot 3 and PostgreSQL, using Hexagonal Architecture and the Transactional Outbox pattern.

## Tech Stack

* **Java:** 17
* **Framework:** Spring Boot 3
* **Database:** PostgreSQL
* **ORM:** Spring Data JPA / Hibernate
* **Testing:** JUnit 5, Mockito
* **Build:** Maven (multi-module)

## Architecture

```
dineflow-domain          entities, enums (no framework logic)
dineflow-application     use cases (services) and ports (interfaces)
dineflow-infrastructure  adapters: JPA repositories, tenant interceptor, outbox scheduler
dineflow-api             REST controllers, exception handling, application entry point
```

Dependencies point inwards: `api` and `infrastructure` depend on `application`, which depends on `domain`.

### Key design decisions

* **Hexagonal Architecture:** the application layer only knows ports (`ReservationRepositoryPort`, `OutboxRepositoryPort`, `MessagePublisher`), so persistence and messaging can be swapped without touching business logic.
* **Transactional Outbox:** a reservation and its `RESERVATION_CREATED` event are saved in the same transaction. `OutboxScheduler` publishes pending events every 5 seconds, so an event is never lost if the broker is down.
* **Multi-tenancy:** every request carries an `X-Tenant-Id` header. `TenantInterceptor` stores it in a `ThreadLocal` (`TenantContext`) and clears it after the request.
* **Double-booking protection:** an overlap query rejects reservations that intersect an existing one for the same table; a unique index and optimistic locking (`@Version`) guard against races.

## Running locally

```bash
export DB_URL=jdbc:postgresql://localhost:5432/dineflow
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
mvn -B verify
mvn -pl dineflow-api spring-boot:run
```

## Known limitations / roadmap

* `X-Tenant-Id` is trusted as-is. There is no authentication yet, so a caller can pass any tenant id. Next step: Spring Security with JWT carrying the tenant claim.
* The outbox scheduler is not safe for multiple instances yet (needs `SELECT ... FOR UPDATE SKIP LOCKED`).
* The message publisher is a mock; a real broker (Kafka/RabbitMQ) is not wired up.
* Schema is managed by Hibernate `ddl-auto`; Flyway migrations are planned.
* Integration tests with Testcontainers are planned.
