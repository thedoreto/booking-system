package com.hotel.ai.dto;

import java.time.LocalDate;

public class DateRange {
    private LocalDate from;
    private LocalDate to;

    public DateRange(LocalDate from, LocalDate to) {
        this.from = from;
        this.to = to;
    }

    public LocalDate getFrom() { return from; }
    public LocalDate getTo() { return to; }
}
