package com.enterprise.calendar.mcp.schema;

import io.modelcontextprotocol.spec.McpSchema;

import java.util.List;
import java.util.Map;

public class RescheduleEventSchema {

    public static McpSchema.JsonSchema create() {
        Map<String, Object> properties = Map.of(
            "event_id", Map.of(
                "type", "string",
                "description", "The ID of the event to reschedule"
            ),
            "new_start_datetime", Map.of(
                "type", "string",
                "description", "New start datetime in ISO8601 format (yyyy-MM-dd'T'HH:mm:ss)"
            ),
            "new_end_datetime", Map.of(
                "type", "string",
                "description", "New end datetime in ISO8601 format (yyyy-MM-dd'T'HH:mm:ss)"
            )
        );

        return new McpSchema.JsonSchema(
            "object",
            properties,
            List.of("event_id", "new_start_datetime", "new_end_datetime"),
            null,
            null,
            null
        );
    }

    private RescheduleEventSchema() {
        // Utility class, prevent instantiation
    }
}
