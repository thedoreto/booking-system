package com.hotel.ai.tool;

import com.hotel.ai.context.ToolContext;
import com.hotel.ai.dto.DateRange;
import com.hotel.booking.service.HotelService;
import com.hotel.common.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class RoomsPerDates implements Tool {

    private static final Logger log = LoggerFactory.getLogger(RoomsPerDates.class);
    private HotelService hotelService;

    public RoomsPerDates(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @Override
    public String name() {
        return "get_rooms_per_dates";
    }

    @Override
    public ToolResult execute(ToolContext ctx) {

        log.info("Executing tool {} with context: {}", name(), ctx);

        DateRange dateRange = ctx.getDateRange();
        if (dateRange == null || dateRange.getFrom() == null || dateRange.getTo() == null) {
            return ToolResult.error("Missing dates");
        }
        LocalDate from = dateRange.getFrom();
        LocalDate to = dateRange.getTo();

        if (!ValidationUtil.isValidPeriod(from, to)) {
            return ToolResult.error("DATES MUST BE IN THE FUTURE AND CHECKIN MUST BE BEFORE CHECKOUT");
        }

        return ToolResult.ok(
                hotelService.findAvailableRooms(from, to)
        );
    }


}
