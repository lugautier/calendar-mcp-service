package com.enterprise.calendar.service;

import com.enterprise.calendar.model.calendar.CalendarEvent;
import com.microsoft.graph.models.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarService {

    private final GraphAPIService graphAPIService;
    private final CalendarEventMapper mapper;

    public List<CalendarEvent> getEvents(String startDate, String endDate) {
        log.debug("Getting calendar events from {} to {}", startDate, endDate);

        List<Event> graphEvents = graphAPIService.getEvents(startDate, endDate);

        return graphEvents.stream()
            .map(mapper::toCalendarEvent)
            .toList();
    }
}
