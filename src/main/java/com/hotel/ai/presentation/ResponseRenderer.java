package com.hotel.ai.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.ai.tool.ToolResult;
import com.hotel.booking.dto.BookingDTO;
import com.hotel.booking.dto.RoomDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResponseRenderer {

    public String render(String toolName, ToolResult result) {

        return switch (toolName) {

            case "get_reservations" ->
                    "Вашите активни резервации са: \n"
                            + bookingsResult(result);

            case "get_rooms_per_dates" ->
                    "Свободните стаи за избраните дати са: \n"
                            + roomsResult(result);

            case "get_rooms" ->
                    "Всички стаи са: \n"
                            + roomsResult(result);
            default ->
                    toJson(result);
        };
    }

    private String bookingsResult(ToolResult obj) {
        try {
            List<BookingDTO> bookingDTOList = (List<BookingDTO>) obj.getData();
            StringBuilder sb = new StringBuilder();
            for (BookingDTO booking : bookingDTOList) {
                sb.append(String.format("Стая номер: " + booking.getRoomNumber() + ", от дата: " + booking.getCheckInDate() + " до дата: " + booking.getCheckOutDate() + "\n"));
            }
            return sb.toString();
        } catch (Exception e) {
            return obj.toString();
        }
    }

    private String roomsResult(ToolResult obj) {
        try {
            List<RoomDTO> roomDTOList = (List<RoomDTO>) obj.getData();
            StringBuilder sb = new StringBuilder();
            for (RoomDTO room : roomDTOList) {
                sb.append("Стая номер: " + room.getRoomNumber() + " [" + room.getType() + "] - цена на вечер: " + room.getPricePerNight()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return obj.toString();
        }
    }
    private String toJson(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
}
