package com.enterprise.calendar.model.calendar;

import java.time.OffsetDateTime;

public record CalendarEvent(
    String id,
    String subject,
    OffsetDateTime start,
    OffsetDateTime end,
    String location,
    String organizerName,
    String organizerEmail,
    boolean isAllDay
) {}
