package com.enterprise.calendar.mcp;

import com.enterprise.calendar.model.calendar.CalendarEvent;
import com.enterprise.calendar.service.CalendarService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarToolsProviderHandlerTest {

    @Mock
    private CalendarService calendarService;

    @Mock
    private McpSyncServerExchange exchange;

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private CalendarToolsProvider toolsProvider;

    @Test
    void shouldHandleGetEventsCall() throws Exception {
        CalendarEvent event = new CalendarEvent(
            "event-123",
            "Team Meeting",
            null, null, null, null, null, false
        );

        when(calendarService.getEvents("2025-10-20", "2025-10-31"))
            .thenReturn(List.of(event));

        Map<String, Object> arguments = Map.of(
            "start_date", "2025-10-20",
            "end_date", "2025-10-31"
        );

        CallToolResult result = toolsProvider.handleGetEvents(exchange, arguments);

        assertThat(result).isNotNull();
        assertThat(result.content()).isNotEmpty();

        TextContent textContent = (TextContent) result.content().getFirst();
        String jsonResponse = textContent.text();
        List<CalendarEvent> events = objectMapper.readValue(jsonResponse, new TypeReference<>() {});

        assertThat(events).hasSize(1);
        assertThat(events.getFirst().id()).isEqualTo("event-123");
        assertThat(events.getFirst().subject()).isEqualTo("Team Meeting");
    }
}
