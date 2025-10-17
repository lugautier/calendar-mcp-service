package com.enterprise.calendar.mcp.schema;

import io.modelcontextprotocol.spec.McpSchema;

import java.util.List;
import java.util.Map;

public class BlockDatesSchema {

    public static McpSchema.JsonSchema create() {
        Map<String, Object> properties = Map.of(
            "start_datetime", Map.of(
                "type", "string",
                "description", "Start datetime in ISO8601 format (yyyy-MM-dd'T'HH:mm:ss)"
            ),
            "end_datetime", Map.of(
                "type", "string",
                "description", "End datetime in ISO8601 format (yyyy-MM-dd'T'HH:mm:ss)"
            ),
            "title", Map.of(
                "type", "string",
                "description", "Event title (optional, defaults to 'Out of Office')"
            ),
            "description", Map.of(
                "type", "string",
                "description", "Event description (optional)"
            )
        );

        return new McpSchema.JsonSchema(
            "object",
            properties,
            List.of("start_datetime", "end_datetime"),
            null,
            null,
            null
        );
    }

    private BlockDatesSchema() {
        // Utility class, prevent instantiation
    }
}
