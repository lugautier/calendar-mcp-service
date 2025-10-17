package com.enterprise.calendar.mcp;

import io.modelcontextprotocol.spec.McpSchema.Tool;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CalendarToolsProviderTest {

    @Autowired
    private CalendarToolsProvider toolsProvider;

    @Test
    void shouldReturnNonNullToolsList() {
        List<Tool> tools = toolsProvider.getTools();

        assertThat(tools).isNotNull();
    }

    @Test
    void shouldRegisterGetEventsTool() {
        List<Tool> tools = toolsProvider.getTools();
        Tool getEventsTool = findToolByName(tools, "get_events");

        assertThat(getEventsTool.inputSchema()).isNotNull();
        assertThat(getEventsTool.inputSchema().toString()).contains("start_date");
        assertThat(getEventsTool.inputSchema().toString()).contains("end_date");
    }

    @Test
    void shouldRegisterBlockDatesTool() {
        List<Tool> tools = toolsProvider.getTools();
        Tool blockDatesTool = findToolByName(tools, "block_dates");

        assertThat(blockDatesTool.inputSchema()).isNotNull();
        assertThat(blockDatesTool.inputSchema().toString()).contains("start_datetime");
        assertThat(blockDatesTool.inputSchema().toString()).contains("end_datetime");
    }

    @Test
    void shouldRegisterFindAvailableSlotsTool() {
        List<Tool> tools = toolsProvider.getTools();
        Tool findSlotsTool = findToolByName(tools, "find_available_slots");

        assertThat(findSlotsTool.inputSchema()).isNotNull();
        assertThat(findSlotsTool.inputSchema().toString()).contains("start_date");
        assertThat(findSlotsTool.inputSchema().toString()).contains("end_date");
        assertThat(findSlotsTool.inputSchema().toString()).contains("duration_minutes");
    }

    @Test
    void shouldRegisterRescheduleEventTool() {
        List<Tool> tools = toolsProvider.getTools();
        Tool rescheduleTool = findToolByName(tools, "reschedule_event");

        assertThat(rescheduleTool.inputSchema()).isNotNull();
        assertThat(rescheduleTool.inputSchema().toString()).contains("event_id");
        assertThat(rescheduleTool.inputSchema().toString()).contains("new_start_datetime");
        assertThat(rescheduleTool.inputSchema().toString()).contains("new_end_datetime");
    }

    private Tool findToolByName(List<Tool> tools, String name) {
        return tools.stream()
            .filter(tool -> name.equals(tool.name()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Tool not found: " + name));
    }
}
