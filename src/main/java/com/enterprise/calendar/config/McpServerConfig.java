package com.enterprise.calendar.config;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpServerConfig {

    @Bean
    public WebMvcSseServerTransportProvider transportProvider() {
        return WebMvcSseServerTransportProvider.builder()
            .messageEndpoint("/mcp")
            .build();
    }

    @Bean
    public McpSyncServer mcpServer(WebMvcSseServerTransportProvider transportProvider) {
        return McpServer.sync(transportProvider)
            .serverInfo("calendar-mcp-service", "1.0.0")
            .build();
    }
}
