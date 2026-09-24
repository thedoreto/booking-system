# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & run

Spring Boot 3.2 / Java 17 / Maven (no Maven wrapper — use a system `mvn`).

```bash
mvn clean package            # build jar into target/
mvn spring-boot:run          # run locally on :8080
mvn test                     # run tests
mvn test -Dtest=ClassName#method   # single test
docker build -t hotel-booking . && docker run -e PORT=8080 -p 8080:8080 hotel-booking
```

There is currently no `src/test` directory, so `mvn test` runs nothing. The Dockerfile builds with `-DskipTests` and passes `$PORT` as `server.port` (it's meant for Render/Railway).

The app will not start without access to the MongoDB URI and the Aiven Kafka cluster configured in `application.properties`. `KafkaCertInitializer` also fails startup if any of `certs/ca.pem`, `certs/aiven-keystore.p12` or `certs/aiven-truststore.p12` is missing from the classpath.

## Architecture

Everything lives under `com.hotel`, split into feature packages:

- **`booking`**: the core domain (rooms, users, bookings and images, all in MongoDB). `BookingController` holds nearly all REST endpoints (`/rooms`, `/images`, `/users`, `/bookings`, `/home`, `/me`), and `HotelService` holds all business logic. The service reports errors by throwing `ResponseStatusException`, which `common/exception/GlobalExceptionHandler` turns into responses. `Result<T>` in `booking/service/result` is a secondary pattern that is barely used.
- **`common/security`**: stateless JWT auth. `AuthController` (`/auth/login`, `/auth/register`) issues tokens through `JwtService`, which puts a `role` claim in each token. `JwtFilter` puts a `UserPrincipal` (user id plus that role as an authority) into the `SecurityContext`. Everything except `/auth/**`, `/`, `/home` and `/error` requires authentication. CORS allows `localhost:5173` (the Vite dev UI) and `booking-ui-81fb.onrender.com`.
- **`hotelinfo`**: read-only hotel details from the `hotelinfo`, `hotelinfo_category` and `knowledge` Mongo collections. `GET /hotelinfo` feeds the UI's main page.
- **`kafka`**: `GlobalKafkaConsumer` implements a request/reply bridge that the external AI-assistant service uses to query this backend. It listens on `hotel-requests-topic`, ignores messages whose record key differs from `hotel.backend.id`, dispatches on the `event` field (`get_all_rooms`, `get_reservations`, `get_available_rooms_by_dates`, `create_booking` with `userId`, `roomIds[]`, `startDate`, `endDate`), and sends a JSON string `{correlationId, data}` to the `replyTo` topic from the payload. On an exception it replies `{correlationId, error}` (the `ResponseStatusException` reason) so the caller does not wait for its timeout. Payloads are JSON strings (String serializers), not typed objects. To add a new event type, add a branch to this class.
- **`ai`** and **`knowledge`**: **deprecated**. The old Gemini-based assistant (`/api-old/chat-old`, tool registry, embedding/vector search) has moved to a separate project. The code remains, marked `@Deprecated`; don't extend it.

## Gotchas

- Room availability (`HotelService.isRoomAvailable`) queries `CONFIRMED` bookings in Mongo on every call. The in-memory `bookingsByRoomId` map is built once at startup and never read or updated afterwards, so don't rely on it.
- `hotel.backend.id` identifies which hotel this backend instance serves. Multi-hotel support is anticipated (see the commented-out `getAllRooms(hotelId)` in the Kafka consumer) but not yet implemented.
- Many code comments are in Bulgarian.
