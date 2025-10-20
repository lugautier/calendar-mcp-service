package com.enterprise.calendar.service;

import com.enterprise.calendar.model.calendar.CalendarEvent;
import com.microsoft.graph.models.DateTimeTimeZone;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.Event;
import com.microsoft.graph.models.Location;
import com.microsoft.graph.models.Recipient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CalendarEventMapperTest {

    private CalendarEventMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CalendarEventMapper();
    }

    @Test
    void shouldMapIdAndSubject() {
        Event event = new Event();
        event.setId("event-123");
        event.setSubject("Team Meeting");

        CalendarEvent result = mapper.toCalendarEvent(event);

        assertThat(result.id()).isEqualTo("event-123");
        assertThat(result.subject()).isEqualTo("Team Meeting");
    }

    @Test
    void shouldMapDates() {
        Event event = new Event();
        event.setId("event-123");
        event.setSubject("Meeting");

        DateTimeTimeZone start = new DateTimeTimeZone();
        start.setDateTime("2025-10-20T10:00:00.0000000");
        start.setTimeZone("UTC");
        event.setStart(start);

        DateTimeTimeZone end = new DateTimeTimeZone();
        end.setDateTime("2025-10-20T11:00:00.0000000");
        end.setTimeZone("UTC");
        event.setEnd(end);

        CalendarEvent result = mapper.toCalendarEvent(event);

        assertThat(result.start()).isNotNull();
        assertThat(result.end()).isNotNull();
        assertThat(result.start().getHour()).isEqualTo(10);
        assertThat(result.end().getHour()).isEqualTo(11);
    }

    @Test
    void shouldMapLocation() {
        Event event = new Event();
        event.setId("event-123");
        event.setSubject("Meeting");

        Location location = new Location();
        location.setDisplayName("Conference Room A");
        event.setLocation(location);

        CalendarEvent result = mapper.toCalendarEvent(event);

        assertThat(result.location()).isEqualTo("Conference Room A");
    }

    @Test
    void shouldMapOrganizer() {
        Event event = new Event();
        event.setId("event-123");
        event.setSubject("Meeting");

        Recipient organizer = new Recipient();
        EmailAddress email = new EmailAddress();
        email.setName("John Doe");
        email.setAddress("john@company.com");
        organizer.setEmailAddress(email);
        event.setOrganizer(organizer);

        CalendarEvent result = mapper.toCalendarEvent(event);

        assertThat(result.organizerName()).isEqualTo("John Doe");
        assertThat(result.organizerEmail()).isEqualTo("john@company.com");
    }

    @Test
    void shouldMapIsAllDay() {
        Event event = new Event();
        event.setId("event-123");
        event.setSubject("All Day Event");
        event.setIsAllDay(true);

        CalendarEvent result = mapper.toCalendarEvent(event);

        assertThat(result.isAllDay()).isTrue();
    }

    @Test
    void shouldHandleNullLocation() {
        Event event = new Event();
        event.setId("event-123");
        event.setSubject("Meeting");

        CalendarEvent result = mapper.toCalendarEvent(event);

        assertThat(result.location()).isNull();
    }

    @Test
    void shouldHandleNullOrganizer() {
        Event event = new Event();
        event.setId("event-123");
        event.setSubject("Meeting");

        CalendarEvent result = mapper.toCalendarEvent(event);

        assertThat(result.organizerName()).isNull();
        assertThat(result.organizerEmail()).isNull();
    }

    @Test
    void shouldHandleNullDates() {
        Event event = new Event();
        event.setId("event-123");
        event.setSubject("Meeting");

        CalendarEvent result = mapper.toCalendarEvent(event);

        assertThat(result.start()).isNull();
        assertThat(result.end()).isNull();
    }

    @Test
    void shouldHandleNullIsAllDay() {
        Event event = new Event();
        event.setId("event-123");
        event.setSubject("Meeting");
        event.setIsAllDay(null);

        CalendarEvent result = mapper.toCalendarEvent(event);

        assertThat(result.isAllDay()).isFalse();
    }
}
