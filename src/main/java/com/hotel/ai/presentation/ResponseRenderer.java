package com.hotel.ai.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class ResponseRenderer {

    public String render(String toolName, Object result) {

        return switch (toolName) {

            case "get_active_bookings" ->
                    "Вашите активни резервации са: "
                            + toJson(result);

            case "get_rooms_per_dates" ->
                    "Свободните стаи за избраните дати са: "
                            + toJson(result);

            default ->
                    toJson(result);
        };
    }

    private String toJson(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
}
