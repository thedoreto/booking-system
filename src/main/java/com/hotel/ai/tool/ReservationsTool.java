package com.hotel.ai.tool;

import org.springframework.stereotype.Component;

@Component
public class ReservationsTool implements Tool {

    @Override
    public String name() {
        return "get_reservations";
    }

    @Override
    public String execute(String input) {
        return "RESERVATIONS: 3 потвърдени резервации за " + input;
    }
}
