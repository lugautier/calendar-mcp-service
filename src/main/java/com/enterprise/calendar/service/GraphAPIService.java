package com.enterprise.calendar.service;

import com.enterprise.calendar.exception.GraphAPIException;
import com.enterprise.calendar.util.DateUtils;
import com.microsoft.graph.models.Event;
import com.microsoft.graph.models.odataerrors.ODataError;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraphAPIService {

    private final GraphServiceClient graphServiceClient;

    public List<Event> getEvents(String startDate, String endDate) {
        DateUtils.validateDateRange(startDate, endDate);

        try {
            log.debug("Fetching events from MS Graph: {} to {}", startDate, endDate);

            OffsetDateTime start = DateUtils.parseStartOfDay(startDate);
            OffsetDateTime end = DateUtils.parseEndOfDay(endDate);

            var response = graphServiceClient.me()
                .calendarView()
                .get(config -> {
                    if (config.queryParameters != null) {
                        config.queryParameters.startDateTime = start.toString();
                        config.queryParameters.endDateTime = end.toString();
                    }
                });

            if (response == null || response.getValue() == null) {
                log.warn("MS Graph returned null response for events");
                return List.of();
            }

            log.info("Successfully fetched {} events from MS Graph", response.getValue().size());
            return response.getValue();

        } catch (ODataError e) {
            log.error("MS Graph API error: {}", e.getMessage(), e);
            throw new GraphAPIException("Failed to fetch calendar events from MS Graph", e);
        } catch (IllegalArgumentException e) {
            log.error("Invalid date parameters: {}", e.getMessage());
            throw e;
        }
    }
}
