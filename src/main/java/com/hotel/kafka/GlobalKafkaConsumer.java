package com.hotel.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.ai.service.AgentService;
import com.hotel.booking.dto.BookingDTO;
import com.hotel.booking.dto.RoomDTO;
import com.hotel.booking.service.HotelService;
import com.hotel.common.security.UserPrincipal;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class GlobalKafkaConsumer {

    private final HotelService hotelService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());// За парсиране на JSON стринга

    @Value("${hotel.backend.id}")
    private String currentHotelId;

    private static final Logger log = LoggerFactory.getLogger(GlobalKafkaConsumer.class);

    public GlobalKafkaConsumer(HotelService hotelService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.hotelService = hotelService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "hotel-requests-topic", groupId = "hotel-backend-${hotel.backend.id}")
    public void handleIncomingRequests(ConsumerRecord<String, String> record) { // Тук е String вместо Map
        String hotelIdKey = record.key();

        // 2. Взимаме JSON съобщението
        String jsonPayload = record.value();

        // 3. Филтриране: Проверяваме дали това съобщение е за този конкретен хотел бекенд
        // (Ако бекендът обслужва само един хотел, проверяваме дали съвпада с неговото ID)
        if (!currentHotelId.equals(hotelIdKey)) {
            log.debug("Skipping Kafka message for hotelId={} (this backend is {})", hotelIdKey, currentHotelId);
            return; // Съобщението е за друг хотел, подминаваме го
        }
        String correlationId = null;
        String replyTo = null;
        try {
            // Парсираме получения JSON стринг до Map
            Map<String, Object> payload = objectMapper.readValue(record.value(), Map.class);

            String event = (String) payload.get("event");
            correlationId = (String) payload.get("correlationId");
            replyTo = (String) payload.get("replyTo");
          //  String hotelId = (String) payload.get("hotelId");

            System.out.println("event: " + event);
            System.out.println("correlationId: " + correlationId);
            System.out.println("replyTo: " + replyTo);
         //   System.out.println("hotelId: " + hotelId);

            if ("get_all_rooms".equals(event)) {
                List<RoomDTO> rooms = hotelService.getAllRooms();
            //    List<RoomDTO> rooms = hotelService.getAllRooms(hotelId); change this line when all collections are combined into one

                Map<String, Object> responseMap = Map.of(
                        "correlationId", correlationId,
                        "data", rooms
                );

                // Превръщаме Map-а в JSON стринг, за да може StringSerializer да го прати успешно
                String jsonResponse = objectMapper.writeValueAsString(responseMap);

                kafkaTemplate.send(replyTo, correlationId, jsonResponse);
            } else if ("get_reservations".equals(event)) {
                String userId = (String) payload.get("userId");

                List<BookingDTO> reservations = hotelService.getBookingByUserId(userId);

                Map<String, Object> responseMap = Map.of(
                        "correlationId", correlationId,
                        "data", reservations
                );

                kafkaTemplate.send(replyTo, correlationId, objectMapper.writeValueAsString(responseMap));
            } else if ("get_available_rooms_by_dates".equals(event)) {
                LocalDate startDate = LocalDate.parse((String) payload.get("startDate"));
                LocalDate endDate = LocalDate.parse((String) payload.get("endDate"));

                System.out.println("startDate: " + startDate + ", endDate: " + endDate);

                List<RoomDTO> availableRooms = hotelService.findAvailableRooms(startDate, endDate);

                Map<String, Object> responseMap = Map.of(
                        "correlationId", correlationId,
                        "data", availableRooms
                );

                kafkaTemplate.send(replyTo, correlationId, objectMapper.writeValueAsString(responseMap));
            } else if ("create_booking".equals(event)) {
                String userId = (String) payload.get("userId");
                List<String> roomIds = (List<String>) payload.get("roomIds");
                LocalDate startDate = LocalDate.parse((String) payload.get("startDate"));
                LocalDate endDate = LocalDate.parse((String) payload.get("endDate"));

                List<BookingDTO> requested = roomIds.stream()
                        .map(roomId -> new BookingDTO(null, userId, null, roomId, null, null,
                                startDate, endDate, 0, 0, null))
                        .toList();
                List<BookingDTO> created = hotelService.createBooking(requested);

                Map<String, Object> responseMap = Map.of(
                        "correlationId", correlationId,
                        "data", created
                );

                kafkaTemplate.send(replyTo, correlationId, objectMapper.writeValueAsString(responseMap));
            }
        } catch (Exception e) {
            log.error("Error processing Kafka message", e);
            e.printStackTrace();
            sendError(replyTo, correlationId, e);
        }
    }

    // Връщаме грешката на изпращача, за да не чака до timeout
    private void sendError(String replyTo, String correlationId, Exception e) {
        if (replyTo == null || correlationId == null) {
            return;
        }
        String message = e instanceof ResponseStatusException rse && rse.getReason() != null
                ? rse.getReason()
                : "Internal error";
        try {
            Map<String, Object> responseMap = Map.of(
                    "correlationId", correlationId,
                    "error", message
            );
            kafkaTemplate.send(replyTo, correlationId, objectMapper.writeValueAsString(responseMap));
        } catch (Exception sendException) {
            log.error("Failed to send error reply", sendException);
        }
    }
}
