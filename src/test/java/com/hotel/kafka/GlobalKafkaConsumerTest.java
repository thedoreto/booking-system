package com.hotel.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.booking.service.HotelService;
import com.hotel.common.security.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Действията от името на потребител (Kafka от AI асистента) вземат потребителя от JWT-то, не от userId в заявката
class GlobalKafkaConsumerTest {

    private static final String HOTEL = "seven_stars";
    private static final String SECRET = "test-secret-test-secret-test-secret-123";

    private final HotelService hotelService = mock(HotelService.class);
    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
    private final JwtService jwtService = new JwtService(SECRET);
    private final GlobalKafkaConsumer consumer = new GlobalKafkaConsumer(hotelService, kafkaTemplate, jwtService);
    private final ObjectMapper objectMapper = new ObjectMapper();

    GlobalKafkaConsumerTest() {
        ReflectionTestUtils.setField(consumer, "currentHotelId", HOTEL);
    }

    @Test
    void usesUserFromTokenAndIgnoresUserIdInRequest() throws Exception {
        when(hotelService.getUpcomingBookings("user-1")).thenReturn(List.of());

        send(Map.of("event", "get_upcoming_bookings", "userId", "someone-else",
                "token", jwtService.generateToken("user-1", "a@b.bg", "USER")));

        verify(hotelService).getUpcomingBookings("user-1");
        verify(hotelService, never()).getUpcomingBookings("someone-else");
        assertThat(reply()).containsKey("data");
    }

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        send(Map.of("event", "cancel_booking", "userId", "user-1", "bookingId", "b-1"));

        verify(hotelService, never()).cancelBooking(anyString(), anyString());
        assertThat(reply()).containsEntry("error", "Login required");
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() throws Exception {
        String forged = Jwts.builder()
                .claim("userId", "user-1")
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor("another-secret-another-secret-another-1".getBytes()), SignatureAlgorithm.HS256)
                .compact();

        send(Map.of("event", "get_reservations", "token", forged));

        verify(hotelService, never()).getBookingByUserId(anyString());
        assertThat(reply()).containsEntry("error", "Invalid token");
    }

    @Test
    void rejectsExpiredToken() throws Exception {
        String expired = Jwts.builder()
                .claim("userId", "user-1")
                .setExpiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()), SignatureAlgorithm.HS256)
                .compact();

        send(Map.of("event", "create_booking", "token", expired,
                "roomIds", List.of("r-1"), "startDate", "2026-10-30", "endDate", "2026-11-02"));

        verify(hotelService, never()).createBooking(org.mockito.ArgumentMatchers.anyList());
        assertThat(reply()).containsEntry("error", "Session expired");
    }

    private void send(Map<String, Object> request) throws Exception {
        Map<String, Object> payload = new HashMap<>(request);
        payload.put("correlationId", "c-1");
        payload.put("replyTo", "hotel-replies-topic");
        consumer.handleIncomingRequests(new ConsumerRecord<>("hotel-requests-topic", 0, 0, HOTEL,
                objectMapper.writeValueAsString(payload)));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> reply() throws Exception {
        ArgumentCaptor<Object> message = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(org.mockito.ArgumentMatchers.eq("hotel-replies-topic"),
                org.mockito.ArgumentMatchers.eq("c-1"), message.capture());
        return objectMapper.readValue((String) message.getValue(), Map.class);
    }
}
