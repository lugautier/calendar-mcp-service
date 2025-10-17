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

        assertThat(tools).hasSize(1);
        assertThat(tools.getFirst().name()).isEqualTo("get_events");
    }

    @Test
    void shouldHaveValidJsonSchemaForGetEvents() {
        List<Tool> tools = toolsProvider.getTools();
        Tool tool = tools.getFirst();

        assertThat(tool.inputSchema()).isNotNull();
        assertThat(tool.inputSchema().toString()).contains("start_date");
        assertThat(tool.inputSchema().toString()).contains("end_date");
    }
}
