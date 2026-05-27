package com.hotel.ai.tool;

import com.hotel.ai.context.ToolContext;
import com.hotel.booking.dto.RoomDTO;
import com.hotel.booking.service.HotelService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoomsTool implements Tool {

    private HotelService hotelService;

    public RoomsTool(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @Override
    public String name() {
        return "get_rooms";
    }

    @Override
    public ToolResult execute(ToolContext ctx) {
        List<RoomDTO> roomDTOS = hotelService.getAllRooms();
        return ToolResult.ok(roomDTOS);
    }
}