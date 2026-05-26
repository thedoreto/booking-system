package com.hotel.ai.tool;

import org.springframework.stereotype.Component;

@Component
public class RoomsTool implements Tool {

    @Override
    public String name() {
        return "get_rooms";
    }

    @Override
    public String execute(String input) {
        return "ROOMS: 3 свободни стаи за " + input;
    }
}