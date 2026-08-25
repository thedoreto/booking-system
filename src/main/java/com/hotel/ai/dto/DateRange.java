package com.hotel.ai.dto;

import java.time.LocalDate;

/**
 * @deprecated Part of the legacy AI implementation.
 *             The new AI implementation uses LangChain4j.
 */
@Deprecated
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
