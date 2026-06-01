package com.hotel.common.util;

import com.hotel.ai.dto.DateRange;
import com.hotel.booking.model.enums.RoomType;
import com.hotel.booking.service.result.Result;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtil {
    private ValidationUtil() {}

    private static final Pattern RANGE_FULL_PATTERN =
            Pattern.compile(
                    "(\\d{1,2}\\s+[\\p{L}]+)\\s+до\\s+(\\d{1,2}\\s+[\\p{L}]+)",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
            );

    private static final Pattern RANGE_SHARED_MONTH_PATTERN =
            Pattern.compile(
                    "(\\d{1,2})\\s*до\\s*(\\d{1,2})\\s+([\\p{L}]+)",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
            );

    private static final Pattern SINGLE_DATE_PATTERN =
            Pattern.compile("(\\d{1,2}\\s+[\\p{L}]+)",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
            );



    private static final DateTimeFormatter BG =
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("d MMMM")
                    .toFormatter(new Locale("bg"));

    private static final Map<String, Integer> MONTHS = Map.ofEntries(
            Map.entry("януари", 1),
            Map.entry("февруари", 2),
            Map.entry("март", 3),
            Map.entry("април", 4),
            Map.entry("май", 5),
            Map.entry("юни", 6),
            Map.entry("юли", 7),
            Map.entry("август", 8),
            Map.entry("септември", 9),
            Map.entry("октомври", 10),
            Map.entry("ноември", 11),
            Map.entry("декември", 12),

            Map.entry("jan", 1),
            Map.entry("january", 1),
            Map.entry("feb", 2),
            Map.entry("february", 2),
            Map.entry("mar", 3),
            Map.entry("march", 3),
            Map.entry("apr", 4),
            Map.entry("april", 4),
            Map.entry("may", 5),
            Map.entry("jun", 6),
            Map.entry("june", 6),
            Map.entry("jul", 7),
            Map.entry("july", 7),
            Map.entry("aug", 8),
            Map.entry("august", 8),
            Map.entry("sep", 9),
            Map.entry("september", 9),
            Map.entry("oct", 10),
            Map.entry("october", 10),
            Map.entry("nov", 11),
            Map.entry("november", 11),
            Map.entry("dec", 12),
            Map.entry("december", 12)
    );

    public static DateRange extractDateRange(String text) {
        System.out.println("Extracting date range from: " + text);

        if (text == null || text.isBlank()) {
            return null;
        }

        // 1) FULL RANGE: 1 юни до 5 юни
        Matcher full = RANGE_FULL_PATTERN.matcher(text);
        if (full.find()) {
            System.out.println("Found full range match: " + full.group(1) + " to " + full.group(2));
            LocalDate from = parseFlexibleDate(full.group(1));
            LocalDate to = parseFlexibleDate(full.group(2));
            return new DateRange(from, to);
        }

        // 2) SHARED MONTH: 5 до 7 юни
        Matcher shared = RANGE_SHARED_MONTH_PATTERN.matcher(text);
        if (shared.find()) {
            int dayFrom = Integer.parseInt(shared.group(1));
            int dayTo = Integer.parseInt(shared.group(2));
            String monthText = shared.group(3).toLowerCase();
            Integer month = MONTHS.get(monthText);
            if (month == null) {
                throw new IllegalArgumentException("Unknown month: " + monthText);
            }

            int year = LocalDate.now().getYear();
            LocalDate from = LocalDate.of(year, month, dayFrom);
            LocalDate to = LocalDate.of(year, month, dayTo);
            return new DateRange(from, to);
        }

        // 3) SINGLE DATE
        Matcher single = SINGLE_DATE_PATTERN.matcher(text);
        if (single.find()) {
            LocalDate date = parseFlexibleDate(single.group(1));
            return new DateRange(date, date.plusDays(1));
        }

        return null;
    }

    private static LocalDate parseFlexibleDate(String input) {
        System.out.println("Parsing date: " + input);
        String text = normalize(input);
        Matcher m = Pattern.compile("(\\d{1,2})\\s+([a-zа-я]+)").matcher(text);
        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid date: " + input);
        }
        int day = Integer.parseInt(m.group(1));
        String monthText = m.group(2);

        Integer month = MONTHS.get(monthText);
        if (month == null) {
            throw new IllegalArgumentException("Unknown month: " + monthText);
        }
        return LocalDate.of(2026, month, day);
    }

    private static String normalize(String input) {
        return input
                .trim()
                .toLowerCase()
                .replace(".", "")
                .replaceAll("\\s+", " ");
    }

    private static LocalDate parseDate(String value) {
        value = value.trim();
        try {
            return LocalDate.parse(value);
        } catch (Exception ignored) {}

        try {
            return LocalDate.parse(value + " 2026", BG);
        } catch (Exception ignored) {}
        throw new IllegalArgumentException("Bad date: " + value);
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
