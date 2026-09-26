# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Architecture rules (mandatory)
They apply to all three projects (booking-ui, booking-ai, booking-system). Before any change, check it does not break them; if a task needs to break one, stop and ask.
1. **As few LLM (Gemini) calls as possible.** This is the most important rule. Anything that can work without the LLM (buttons, date picker, room selection, booking, cancelling, knowledge buttons) works without it.
2. **The UI chat window knows nothing** except that it is a chat window that sends data to the AI assistant (booking-ai). The chat never calls booking-system, has no business logic and does not know what a button does – it shows what the AI assistant returns.
3. **The AI assistant sends requests to the backend (booking-system) only through Kafka.**
4. **The AI assistant gets data from the backend only through Kafka.** booking-ai's own database (`HotelAI`) is not the backend. So data the chat needs (for example room images) must reach booking-ai through Kafka replies.
5. **Rule 2 is only about the chat.** The site pages in booking-ui (Hotel Info, Rooms, Bookings, …) are the hotel's website and call this REST API directly – that is correct and stays.
6. **The chat actions keep their own endpoints in booking-ai** (`/api/chat`, `/api/rooms/available`, `/api/bookings`, `/api/bookings/cancel`, `/api/shortcuts`, `/api/rooms/types`) – they are not merged into `/api/chat`, because the separate endpoints skip the LLM (rule 1).

## Build & run

Spring Boot 3.2 / Java 17 / Maven (no Maven wrapper — use a system `mvn`).

```bash
mvn clean package            # build jar into target/
mvn spring-boot:run          # run locally on :8080
mvn test                     # run tests
mvn test -Dtest=ClassName#method   # single test
docker build -t hotel-booking . && docker run -e PORT=8080 -p 8080:8080 hotel-booking
```

Unit tests (no cloud, Mockito): `GlobalKafkaConsumerTest` (JWT in Kafka requests) and `HotelServiceImagesTest`. Run them with `mvn -o test -Dtest='GlobalKafkaConsumerTest,HotelServiceImagesTest'`. The Dockerfile builds with `-DskipTests` and passes `$PORT` as `server.port` (it's meant for Render/Railway).

The app will not start without access to the MongoDB URI and the Aiven Kafka cluster configured in `application.properties`. `KafkaCertInitializer` also fails startup if any of `certs/ca.pem`, `certs/aiven-keystore.p12` or `certs/aiven-truststore.p12` is missing from the classpath.

## Working with the user
- **Manual checks:** the user reads the code and tests locally and on Render herself before accepting changes. Do not add tasks or notes like "test in the browser", "check in Atlas", "check/delete after deploy" or "not tested in the browser" – not in replies, not in `SESSIONS_LOG.md` or `../booking-ai/PLAN.md`. Only say what you checked yourself (compile, unit tests, build).

## Architecture

Everything lives under `com.hotel`, split into feature packages:

- **`booking`**: the core domain (rooms, users, bookings and images, all in MongoDB). `BookingController` holds nearly all REST endpoints (`/rooms`, `/images`, `/users`, `/bookings`, `/home`, `/me`), and `HotelService` holds all business logic. The service reports errors by throwing `ResponseStatusException`, which `common/exception/GlobalExceptionHandler` turns into responses. `Result<T>` in `booking/service/result` is a secondary pattern that is barely used.
- **`common/security`**: stateless JWT auth. `AuthController` (`/auth/login`, `/auth/register`) issues tokens through `JwtService`, which puts a `role` claim in each token. `JwtFilter` puts a `UserPrincipal` (user id plus that role as an authority) into the `SecurityContext`. `GET /hotelinfo`, `GET /rooms/**` and `GET /images/**` are public so an anonymous guest can browse the hotel info and rooms (read only). Everything else except `/auth/**`, `/`, `/home` and `/error` requires authentication. Room and image writes (`PUT`/`POST`/`DELETE`) only need a login, not the ADMIN role. CORS allows `localhost:5173` (the Vite dev UI) and `booking-ui-81fb.onrender.com`.
- **`hotelinfo`**: read-only hotel details from the `hotelinfo`, `hotelinfo_category` and `knowledge` Mongo collections. `GET /hotelinfo` feeds the UI's main page.
- **`kafka`**: `GlobalKafkaConsumer` implements a request/reply bridge that the external AI-assistant service uses to query this backend. It listens on `hotel-requests-topic`, ignores messages whose record key differs from `hotel.backend.id`, dispatches on the `event` field. The user-scoped events (`create_booking`, `get_upcoming_bookings`, `cancel_booking`, `get_reservations`) take the user from the JWT in the `token` field (`userIdFromToken`, verified with `jwt.secret`); a `userId` in the payload is ignored, and a missing/invalid/expired token is answered with `Login required` / `Invalid token` / `Session expired`. Events (`get_all_rooms`, `get_reservations`, `get_available_rooms_by_dates` – rooms with their images as `RoomWithImagesDTO` `{id, roomNumber, type, pricePerNight, images: [{id, url, title}]}` so the chat never calls this REST API, `create_booking` with `token`, `roomIds[]`, `startDate`, `endDate`), and sends a JSON string `{correlationId, data}` to the `replyTo` topic from the payload. On an exception it replies `{correlationId, error}` (the `ResponseStatusException` reason) so the caller does not wait for its timeout. Payloads are JSON strings (String serializers), not typed objects. To add a new event type, add a branch to this class.
- **`ai`** and **`knowledge`**: **deprecated**. The old Gemini-based assistant (`/api-old/chat-old`, tool registry, embedding/vector search) has moved to a separate project. The code remains, marked `@Deprecated`; don't extend it.

## Gotchas

- Room availability (`HotelService.isRoomAvailable`) queries `CONFIRMED` bookings in Mongo on every call. The in-memory `bookingsByRoomId` map is built once at startup and never read or updated afterwards, so don't rely on it.
- `hotel.backend.id` identifies which hotel this backend instance serves. Multi-hotel support is anticipated (see the commented-out `getAllRooms(hotelId)` in the Kafka consumer) but not yet implemented.
- Many code comments are in Bulgarian.
