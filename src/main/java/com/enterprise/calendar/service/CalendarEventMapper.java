package com.enterprise.calendar.service;

import com.enterprise.calendar.model.calendar.CalendarEvent;
import com.microsoft.graph.models.Event;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class CalendarEventMapper {

    public CalendarEvent toCalendarEvent(Event event) {
        return new CalendarEvent(
            event.getId(),
            event.getSubject(),
            parseDateTime(event.getStart()),
            parseDateTime(event.getEnd()),
            getLocationName(event),
            getOrganizerName(event),
            getOrganizerEmail(event),
            event.getIsAllDay() != null && event.getIsAllDay()
        );
    }

    private OffsetDateTime parseDateTime(com.microsoft.graph.models.DateTimeTimeZone dateTime) {
        if (dateTime == null || dateTime.getDateTime() == null) {
            return null;
        }

        String dateTimeString = dateTime.getDateTime();
        if (dateTimeString.contains(".")) {
            dateTimeString = dateTimeString.substring(0, dateTimeString.indexOf('.'));
        }

        return OffsetDateTime.parse(dateTimeString + "Z");
    }

    private String getLocationName(Event event) {
        return event.getLocation() != null ? event.getLocation().getDisplayName() : null;
    }

    private String getOrganizerName(Event event) {
        if (event.getOrganizer() == null || event.getOrganizer().getEmailAddress() == null) {
            return null;
        }
        return event.getOrganizer().getEmailAddress().getName();
    }

    private String getOrganizerEmail(Event event) {
        if (event.getOrganizer() == null || event.getOrganizer().getEmailAddress() == null) {
            return null;
        }
        return event.getOrganizer().getEmailAddress().getAddress();
    }
}
