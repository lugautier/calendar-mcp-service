package com.enterprise.calendar.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CalendarToolsProvider {

    public List<Tool> getTools() {
        Map<String, Object> properties = Map.of(
            "start_date", Map.of(
                "type", "string",
                "description", "Start date (yyyy-MM-dd)"
            ),
            "end_date", Map.of(
                "type", "string",
                "description", "End date (yyyy-MM-dd)"
            )
        );

        McpSchema.JsonSchema inputSchema = new McpSchema.JsonSchema(
            "object",                           // type
            properties,                         // properties
            List.of("start_date", "end_date"), // required
            null,                              // additionalProperties
            null,                              // defs
            null                               // definitions
        );

        Tool tool = Tool.builder()
            .name("get_events")
            .description("Query calendar events by date range")
            .inputSchema(inputSchema)
            .build();

        return List.of(tool);
    }
}
