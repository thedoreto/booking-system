package com.hotel.booking.dto;

import java.util.List;

// Свободна стая за чата (Kafka към AI асистента) – със снимките ѝ, в реда на imageIds.
// Чатът не вика booking-system, затова снимките идват готови в отговора.
public record RoomWithImagesDTO(String id, int roomNumber, String type, double pricePerNight, List<ImageDTO> images) {
}
