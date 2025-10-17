package com.enterprise.calendar.config;

import io.modelcontextprotocol.server.McpSyncServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class McpServerConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldCreateMcpServerBean() {
        McpSyncServer mcpServer = applicationContext.getBean(McpSyncServer.class);

        assertThat(mcpServer).isNotNull();
    }
}

