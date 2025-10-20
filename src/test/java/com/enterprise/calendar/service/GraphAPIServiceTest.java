package com.enterprise.calendar.service;

import com.microsoft.graph.models.Event;
import com.microsoft.graph.models.EventCollectionResponse;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.graph.users.item.calendarview.CalendarViewRequestBuilder;
import com.microsoft.graph.users.item.UserItemRequestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GraphAPIServiceTest {

    @Mock
    private GraphServiceClient graphServiceClient;

    @Mock
    private UserItemRequestBuilder userItemRequestBuilder;

    @Mock
    private CalendarViewRequestBuilder calendarViewRequestBuilder;

    private GraphAPIService graphAPIService;

    @BeforeEach
    void setUp() {
        graphAPIService = new GraphAPIService(graphServiceClient);
    }

    @Test
    void shouldReturnEventsFromMSGraph() {
        Event event1 = new Event();
        event1.setId("event-1");
        event1.setSubject("Meeting 1");

        Event event2 = new Event();
        event2.setId("event-2");
        event2.setSubject("Meeting 2");

        EventCollectionResponse response = new EventCollectionResponse();
        response.setValue(List.of(event1, event2));

        when(graphServiceClient.me()).thenReturn(userItemRequestBuilder);
        when(userItemRequestBuilder.calendarView()).thenReturn(calendarViewRequestBuilder);
        when(calendarViewRequestBuilder.get(any())).thenReturn(response);

        List<Event> events = graphAPIService.getEvents("2025-10-15", "2025-10-31");

        assertThat(events).hasSize(2);
        assertThat(events.get(0).getSubject()).isEqualTo("Meeting 1");
        assertThat(events.get(1).getSubject()).isEqualTo("Meeting 2");
    }
}
