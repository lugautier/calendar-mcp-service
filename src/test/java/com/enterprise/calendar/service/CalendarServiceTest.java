package com.enterprise.calendar.service;

import com.enterprise.calendar.model.calendar.CalendarEvent;
import com.microsoft.graph.models.DateTimeTimeZone;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.Event;
import com.microsoft.graph.models.Location;
import com.microsoft.graph.models.Recipient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @Mock
    private GraphAPIService graphAPIService;

    private CalendarEventMapper mapper;
    private CalendarService calendarService;

    @BeforeEach
    void setUp() {
        mapper = new CalendarEventMapper();
        calendarService = new CalendarService(graphAPIService, mapper);
    }

    @Test
    void shouldTransformGraphEventsToCalendarEvents() {
        Event graphEvent = new Event();
        graphEvent.setId("event-123");
        graphEvent.setSubject("Team Meeting");

        DateTimeTimeZone start = new DateTimeTimeZone();
        start.setDateTime("2025-10-20T10:00:00.0000000");
        start.setTimeZone("UTC");
        graphEvent.setStart(start);

        DateTimeTimeZone end = new DateTimeTimeZone();
        end.setDateTime("2025-10-20T11:00:00.0000000");
        end.setTimeZone("UTC");
        graphEvent.setEnd(end);

        Location location = new Location();
        location.setDisplayName("Conference Room A");
        graphEvent.setLocation(location);

        Recipient organizer = new Recipient();
        EmailAddress email = new EmailAddress();
        email.setName("John Doe");
        email.setAddress("john@company.com");
        organizer.setEmailAddress(email);
        graphEvent.setOrganizer(organizer);

        when(graphAPIService.getEvents("2025-10-20", "2025-10-20"))
            .thenReturn(List.of(graphEvent));

        List<CalendarEvent> events = calendarService.getEvents("2025-10-20", "2025-10-20");

        assertThat(events).hasSize(1);
        CalendarEvent event = events.getFirst();
        assertThat(event.id()).isEqualTo("event-123");
        assertThat(event.subject()).isEqualTo("Team Meeting");
        assertThat(event.location()).isEqualTo("Conference Room A");
        assertThat(event.organizerName()).isEqualTo("John Doe");
        assertThat(event.organizerEmail()).isEqualTo("john@company.com");
        assertThat(event.start()).isNotNull();
        assertThat(event.end()).isNotNull();
    }

    @Test
    void shouldHandleMultipleEvents() {
        Event event1 = new Event();
        event1.setId("event-1");
        event1.setSubject("Meeting 1");

        Event event2 = new Event();
        event2.setId("event-2");
        event2.setSubject("Meeting 2");

        when(graphAPIService.getEvents("2025-10-20", "2025-10-25"))
            .thenReturn(List.of(event1, event2));

        List<CalendarEvent> events = calendarService.getEvents("2025-10-20", "2025-10-25");

        assertThat(events).hasSize(2);
        assertThat(events.get(0).id()).isEqualTo("event-1");
        assertThat(events.get(1).id()).isEqualTo("event-2");
    }

    @Test
    void shouldHandleEmptyEventsList() {
        when(graphAPIService.getEvents("2025-10-20", "2025-10-20"))
            .thenReturn(List.of());

        List<CalendarEvent> events = calendarService.getEvents("2025-10-20", "2025-10-20");

        assertThat(events).isEmpty();
    }
}
