package com.enterprise.calendar.util;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtils {

    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public static OffsetDateTime parseStartOfDay(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString, ISO_DATE_FORMATTER);
            return date.atStartOfDay().atOffset(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + dateString + ". Expected format: yyyy-MM-dd", e);
        }
    }

    public static OffsetDateTime parseEndOfDay(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString, ISO_DATE_FORMATTER);
            return date.atTime(23, 59, 59).atOffset(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + dateString + ". Expected format: yyyy-MM-dd", e);
        }
    }

    public static void validateDateRange(String startDate, String endDate) {
        OffsetDateTime start = parseStartOfDay(startDate);
        OffsetDateTime end = parseEndOfDay(endDate);

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
    }
}
