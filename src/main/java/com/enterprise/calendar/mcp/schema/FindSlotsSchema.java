package com.enterprise.calendar.mcp.schema;

import io.modelcontextprotocol.spec.McpSchema;

import java.util.List;
import java.util.Map;

public class FindSlotsSchema {

    public static McpSchema.JsonSchema create() {
        Map<String, Object> properties = Map.of(
            "start_date", Map.of(
                "type", "string",
                "description", "Start date in ISO8601 format (yyyy-MM-dd)"
            ),
            "end_date", Map.of(
                "type", "string",
                "description", "End date in ISO8601 format (yyyy-MM-dd)"
            ),
            "duration_minutes", Map.of(
                "type", "integer",
                "description", "Required duration in minutes for the time slot"
            )
        );

        return new McpSchema.JsonSchema(
            "object",
            properties,
            List.of("start_date", "end_date", "duration_minutes"),
            null,
            null,
            null
        );
    }

    private FindSlotsSchema() {
        // Utility class, prevent instantiation
    }
}
