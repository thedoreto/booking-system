package com.hotel.ai.tool;

import com.hotel.ai.context.ToolContext;
import com.hotel.booking.dto.RoomDTO;
import com.hotel.booking.service.HotelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @deprecated This client is part of the legacy AI implementation.
 *             The new AI implementation uses LangChain4j.
 */
@Deprecated
@Component
public class RoomsTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(RoomsTool.class);
    private HotelService hotelService;

    public RoomsTool(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @Override
    public String name() {
        return "get_rooms";
    }

    @Override
    public boolean isToolCachable() {
        return true;
    }

    @Override
    public ToolResult execute(ToolContext ctx) {
        log.info("Executing tool {} with context: {}", name(), ctx);
        List<RoomDTO> roomDTOS = hotelService.getAllRooms();
        return ToolResult.ok(roomDTOS);
    }
}