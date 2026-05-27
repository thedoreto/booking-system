package com.hotel.ai.tool;

import com.hotel.ai.context.ToolContext;
import com.hotel.booking.dto.BookingDTO;
import com.hotel.booking.service.HotelService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservationsTool implements Tool {

    private HotelService hotelService;

    public ReservationsTool(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @Override
    public String name() {
        return "get_reservations";
    }

    @Override
    public ToolResult execute(ToolContext ctx) {
        List<BookingDTO> bookingDTOS = hotelService.getActiveBookings(ctx.getAuth());
        return ToolResult.ok(bookingDTOS);
    }
}
