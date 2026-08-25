package com.hotel.ai.tool;

import com.hotel.ai.context.ToolContext;
import com.hotel.booking.dto.BookingDTO;
import com.hotel.booking.service.HotelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @deprecated This client is part of the legacy AI implementation.
 *             The new AI implementation uses LangChain4j.
 */
@Deprecated
@Component
public class ReservationsTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(ReservationsTool.class);
    private HotelService hotelService;

    public ReservationsTool(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @Override
    public String name() {
        return "get_reservations";
    }

    @Override
    public boolean isToolCachable() {
        return false;
    }

    @Override
    public ToolResult execute(ToolContext ctx) {
        log.info("Executing tool {} with context: {}", name(), ctx.getAuth());
        List<BookingDTO> bookingDTOS = hotelService.getActiveBookings(ctx.getAuth());
        return ToolResult.ok(bookingDTOS);
    }
}
