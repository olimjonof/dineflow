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
* **Multi-tenancy:** the tenant id is a claim (`tenant_id`) inside the signed JWT. `TenantInterceptor` copies it into a `ThreadLocal` (`TenantContext`) and clears it after the request. The tenant is never taken from a header or request body, so a caller cannot act as another tenant.
* **Authentication:** stateless JWT (HS256) with Spring Security's resource server. Passwords are hashed with BCrypt. Token issuing and password hashing sit behind ports (`TokenIssuer`, `PasswordHasher`), so the application layer stays framework-free.
* **Double-booking protection:** an overlap query rejects reservations that intersect an existing one for the same table; a unique index and optimistic locking (`@Version`) guard against races.

## Running locally

```bash
export DB_URL=jdbc:postgresql://localhost:5432/dineflow
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
export JWT_SECRET=$(openssl rand -base64 48)   # required, at least 32 characters
mvn -B verify
mvn -pl dineflow-api spring-boot:run
```

## API

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/auth/register` | public | Creates a tenant and its first user, returns a token |
| POST | `/api/v1/auth/login` | public | Returns a token |
| POST | `/api/v1/reservations` | Bearer token | Creates a reservation for the token's tenant |

```bash
# register a restaurant owner
curl -s -X POST localhost:8080/api/v1/auth/register -H 'Content-Type: application/json' \
  -d '{"tenantName":"My Restaurant","name":"Owner","email":"owner@example.com","password":"change-me-123"}'

# use the returned accessToken
curl -s -X POST localhost:8080/api/v1/reservations \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"restaurantId":1,"tableId":1,"customerId":1,"startTime":"2026-10-10T19:00:00","endTime":"2026-10-10T21:00:00","status":"CONFIRMED"}'
```

Configuration: `JWT_SECRET` (required), `JWT_EXPIRATION_MINUTES` (default 60).

## Known limitations / roadmap

* `restaurantId` and `tableId` in a reservation are not yet checked to belong to the caller's tenant.
* No roles/permissions, refresh tokens or token revocation yet; there is also no endpoint to add more users to an existing tenant.
* The `users` table gains `tenant_id` and `password_hash` columns (nullable, so `ddl-auto=update` works on existing data). Flyway migrations are planned.
* The outbox scheduler is not safe for multiple instances yet (needs `SELECT ... FOR UPDATE SKIP LOCKED`).
* The message publisher is a mock; a real broker (Kafka/RabbitMQ) is not wired up.
* Schema is managed by Hibernate `ddl-auto`; Flyway migrations are planned.
* Integration tests with Testcontainers are planned.
