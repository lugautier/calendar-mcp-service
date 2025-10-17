package com.enterprise.calendar.mcp.schema;

import io.modelcontextprotocol.spec.McpSchema;

import java.util.List;
import java.util.Map;

public class GetEventsSchema {

    public static McpSchema.JsonSchema create() {
        Map<String, Object> properties = Map.of(
            "start_date", Map.of(
                "type", "string",
                "description", "Start date in ISO8601 format (yyyy-MM-dd)"
            ),
            "end_date", Map.of(
                "type", "string",
                "description", "End date in ISO8601 format (yyyy-MM-dd)"
            )
        );

        return new McpSchema.JsonSchema(
            "object",
            properties,
            List.of("start_date", "end_date"),
            null,
            null,
            null
        );
    }

    private GetEventsSchema() {
        // Utility class, prevent instantiation
    }
}
