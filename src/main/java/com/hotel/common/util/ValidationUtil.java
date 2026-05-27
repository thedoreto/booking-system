package com.hotel.common.util;

import com.hotel.ai.dto.DateRange;
import com.hotel.booking.model.enums.RoomType;
import com.hotel.booking.service.result.Result;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtil {
    private ValidationUtil() {}

    private static final Pattern RANGE_PATTERN =
                Pattern.compile("(\\d{4}-\\d{2}-\\d{2}).*(\\d{4}-\\d{2}-\\d{2})");

    private static final Pattern SINGLE_DATE_PATTERN =
                Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");

    public static DateRange extractDateRange(String text) {

        if (text == null || text.isBlank()) { return null; }

        Matcher rangeMatcher = RANGE_PATTERN.matcher(text);
        if (rangeMatcher.find()) {
             LocalDate from = LocalDate.parse(rangeMatcher.group(1));
             LocalDate to = LocalDate.parse(rangeMatcher.group(2));
             return new DateRange(from, to);
        }

        Matcher singleMatcher = SINGLE_DATE_PATTERN.matcher(text);
        if (singleMatcher.find()) {

             LocalDate date = LocalDate.parse(singleMatcher.group(1));

             return new DateRange(date, date);
        }

        return null;
    }

    public static boolean isValidPeriod(LocalDate from, LocalDate to) {
        return from != null && to != null
                && !from.isBefore(LocalDate.now()) && from.isBefore(to);
    }

    public static Result<Void> requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            return Result.failure(message);
        }
        return Result.success();
    }


    public static boolean isValidRoomType(String s) {
        return Arrays.stream(RoomType.values())
                .anyMatch(t -> t.name().equals(s));
    }

    public static Result<Void> requireTrue(boolean condition, String message) {
        if (!condition) {
            return Result.failure(message);
        }
        return Result.success();
    }

    public static Result<Void> isValidEmail(String email) {
        if (email == null ||
                    !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            return Result.failure("Email is not valid");
        }
        return Result.success();
    }
}
