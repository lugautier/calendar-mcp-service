package com.enterprise.calendar.mcp;

import com.enterprise.calendar.mcp.schema.BlockDatesSchema;
import com.enterprise.calendar.mcp.schema.FindSlotsSchema;
import com.enterprise.calendar.mcp.schema.GetEventsSchema;
import com.enterprise.calendar.mcp.schema.RescheduleEventSchema;
import com.enterprise.calendar.service.CalendarService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class CalendarToolsProvider {

    private final CalendarService calendarService;
    private final ObjectMapper objectMapper;

    private List<ToolDefinition> getToolDefinitions() {
        return List.of(
            new ToolDefinition(
                "get_events",
                "Query calendar events by date range",
                GetEventsSchema::create,
                this::handleGetEvents
            ),
            new ToolDefinition(
                "block_dates",
                "Block time slots by creating Out of Office events",
                BlockDatesSchema::create,
                null  // To be implemented
            ),
            new ToolDefinition(
                "find_available_slots",
                "Find available time slots in calendar for scheduling",
                FindSlotsSchema::create,
                null  // To be implemented
            ),
            new ToolDefinition(
                "reschedule_event",
                "Reschedule an existing calendar event to a new date/time",
                RescheduleEventSchema::create,
                null  // To be implemented
            )
        );
    }

    public List<Tool> getTools() {
        return getToolDefinitions().stream()
            .map(this::buildTool)
            .toList();
    }

    private Tool buildTool(ToolDefinition definition) {
        return Tool.builder()
            .name(definition.name())
            .description(definition.description())
            .inputSchema(definition.schemaSupplier().get())
            .build();
    }

    public void registerTools(McpSyncServer server) {
        log.info("Registering MCP tools with handlers");

        getToolDefinitions().forEach(definition -> {
            if (definition.handler() != null) {
                var spec = new SyncToolSpecification(
                    buildTool(definition),
                    definition.handler()
                );
                server.addTool(spec);
                log.debug("Registered tool '{}' with handler", definition.name());
            } else {
                log.warn("Tool '{}' has no handler - skipping registration", definition.name());
            }
        });

        log.info("Registered {} tools with handlers",
            getToolDefinitions().stream().filter(d -> d.handler() != null).count());
    }

    private record ToolDefinition(
        String name,
        String description,
        Supplier<JsonSchema> schemaSupplier,
        BiFunction<McpSyncServerExchange, Map<String, Object>, CallToolResult> handler
    ) {}

    public CallToolResult handleGetEvents(McpSyncServerExchange exchange, Map<String, Object> arguments) {
        try {
            log.debug("Handling get_events with arguments: {}", arguments);

            String startDate = (String) arguments.get("start_date");
            String endDate = (String) arguments.get("end_date");

            var events = calendarService.getEvents(startDate, endDate);
            String json = objectMapper.writeValueAsString(events);

            log.info("get_events returned {} events", events.size());

            var textContent = new TextContent(json);
            return new CallToolResult(List.of(textContent), false, null, null);
        } catch (Exception e) {
            log.error("Failed to handle get_events: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to handle get_events", e);
        }
    }
}
