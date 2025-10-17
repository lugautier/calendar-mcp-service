package com.enterprise.calendar.mcp;

import com.enterprise.calendar.mcp.schema.BlockDatesSchema;
import com.enterprise.calendar.mcp.schema.FindSlotsSchema;
import com.enterprise.calendar.mcp.schema.GetEventsSchema;
import com.enterprise.calendar.mcp.schema.RescheduleEventSchema;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

@Component
public class CalendarToolsProvider {

    private static final List<ToolDefinition> TOOL_DEFINITIONS = List.of(
        new ToolDefinition(
            "get_events",
            "Query calendar events by date range",
            GetEventsSchema::create
        ),
        new ToolDefinition(
            "block_dates",
            "Block time slots by creating Out of Office events",
            BlockDatesSchema::create
        ),
        new ToolDefinition(
            "find_available_slots",
            "Find available time slots in calendar for scheduling",
            FindSlotsSchema::create
        ),
        new ToolDefinition(
            "reschedule_event",
            "Reschedule an existing calendar event to a new date/time",
            RescheduleEventSchema::create
        )
    );

    public List<Tool> getTools() {
        return TOOL_DEFINITIONS.stream()
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

    private record ToolDefinition(
        String name,
        String description,
        Supplier<JsonSchema> schemaSupplier
    ) {}
}
