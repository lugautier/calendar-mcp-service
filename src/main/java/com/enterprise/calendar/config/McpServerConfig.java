package com.enterprise.calendar.config;

import com.enterprise.calendar.mcp.CalendarToolsProvider;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class McpServerConfig {

    private final CalendarToolsProvider toolsProvider;

    @Bean
    public WebMvcSseServerTransportProvider transportProvider() {
        return WebMvcSseServerTransportProvider.builder()
            .messageEndpoint("/mcp")
            .build();
    }

    @Bean
    public McpSyncServer mcpServer(WebMvcSseServerTransportProvider transportProvider) {
        McpSyncServer server = McpServer.sync(transportProvider)
            .serverInfo("calendar-mcp-service", "1.0.0")
            .capabilities(McpSchema.ServerCapabilities.builder()
                .tools(true)
                .build())
            .build();

        toolsProvider.registerTools(server);

        return server;
    }
}
