# Calendar MCP Service

MCP (Model Context Protocol) server that exposes Microsoft 365 Calendar operations using the official **MCP Java SDK**.

## Overview

This Spring Boot service provides a standardized MCP interface for calendar operations, enabling AI assistants (like Claude) to interact with Microsoft 365 calendars through a dedicated orchestrator.

**Key Technologies**:
- Java 21 LTS + Spring Boot 3.5.6
- **Official MCP Java SDK 0.12.1** (handles all protocol operations)
- Microsoft Graph SDK 6.15.0 (calendar integration)

**Protocol**: The MCP SDK automatically handles JSON-RPC 2.0, transport (SSE/HTTP), and method routing. We only implement business logic.

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│  MCP Client (NestJS Orchestrator)                      │
│  {"method": "tools/call", "params": {...}}             │
└──────────────────┬──────────────────────────────────────┘
                   │ HTTP/SSE
                   ▼
┌─────────────────────────────────────────────────────────┐
│  McpServer (SDK - Spring Bean)                         │
│  ├─ Transport Layer (mcp-spring-webmvc)                │
│  ├─ Protocol Parser (JSON-RPC 2.0)                     │
│  └─ Method Router                                       │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────┐
│  CalendarToolsProvider                      │
│  ├─ get_events handler                                  │
│  ├─ block_dates handler                                 │
│  ├─ find_available_slots handler                        │
│  └─ reschedule_event handler                            │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────┐
│  CalendarService (Business Logic)                      │
│  ├─ Date validation                                     │
│  ├─ Event transformation                                │
│  └─ Slot detection algorithms                           │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────┐
│  GraphAPIService (MS Graph Integration)                │
│  ├─ OAuth2 authentication                               │
│  ├─ Token management                                    │
│  └─ MS Graph SDK calls                                  │
└─────────────────────────────────────────────────────────┘
```

---

## Features

The service exposes 4 MCP tools:

### 1. `get_events`
Query calendar events by date range with optional filters.

**Input Schema:**
```json
{
  "start_date": "2025-10-15",  // ISO8601: yyyy-MM-dd
  "end_date": "2025-10-31",
  "filter": "optional filter string"
}
```

### 2. `block_dates`
Create "Out of Office" events to block time slots.

**Input Schema:**
```json
{
  "start_date": "2025-10-20",
  "end_date": "2025-10-22",
  "reason": "Vacation"
}
```

### 3. `find_available_slots`
Analyze calendar and detect free time slots for scheduling.

**Input Schema:**
```json
{
  "duration": 60,  // minutes
  "date_range": {
    "start": "2025-10-15",
    "end": "2025-10-31"
  },
  "participants": ["optional", "list", "of", "emails"]
}
```

### 4. `reschedule_event`
Move existing meeting to new time slot.

**Input Schema:**
```json
{
  "event_id": "uuid-of-event",
  "new_start_date": "2025-10-16T14:00:00",
  "new_end_date": "2025-10-16T15:00:00"
}
```

---

## Tech Stack

### Core
- **Java**: 21.0.5 (Eclipse Temurin LTS)
- **Spring Boot**: 3.5.6
- **Maven**: 3.9.9 with Toolchains

### MCP SDK
- **mcp-bom**: 0.12.1 (Bill of Materials)
- **mcp**: 0.12.1 (Core protocol & default transports)
- **mcp-spring-webmvc**: 0.12.1 (Spring SSE/HTTP transport)
- **mcp-test**: 0.12.1 (Testing utilities)

### Microsoft Integration
- **microsoft-graph**: 6.15.0 (MS Graph Java SDK)
- **azure-identity**: 1.13.2 (OAuth2 client credentials)

### Utilities
- **lombok**: 1.18.34 (Reduce boilerplate)
- **springdoc-openapi**: 2.6.0 (Swagger UI)

---

## Prerequisites

- **Java 21+** 
- **Maven 3.9+** (wrapper included: `./mvnw`)
- **Azure AD Application** with calendar permissions
- **Microsoft 365 Account** for testing

---

## Setup

### 1. Clone & Build

```bash
git clone <repository-url>
cd calendar-mcp-service
./mvnw clean install
```

### 2. Configure Azure Credentials

Edit `.env`:

```properties
MS_TENANT_ID=your-azure-tenant-id
MS_CLIENT_ID=your-app-client-id
MS_CLIENT_SECRET=your-client-secret
```

### 3. Azure AD Permissions

Your Azure AD app requires these **Application-level** permissions:
- `Calendars.ReadWrite` - Read and write calendar events
- `User.Read.All` - Read user profiles (for multi-user availability)

Grant admin consent in Azure Portal.

---

## Run

```bash
# Development mode
./mvnw spring-boot:run

# With dev profile (DEBUG logging)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Production JAR
./mvnw clean package
java -jar target/calendar-mcp-service-0.0.1-SNAPSHOT.jar
```

**Service runs on:** `http://localhost:8080`

**Health check:**
```bash
curl http://localhost:8080/actuator/health
```

---

## How It Works (SDK Integration)

### 1. McpServer Configuration

The SDK's `McpServer` is configured as a Spring bean:

```java
@Configuration
public class McpServerConfig {

    @Bean
    public McpServer mcpServer(CalendarToolsProvider toolsProvider) {
        return McpServer.builder()
            .serverInfo(new ServerInfo("calendar-mcp-service", "1.0.0"))
            .capabilities(ServerCapabilities.builder()
                .tools(ToolsCapability.builder().build())
                .build())
            .toolsProvider(toolsProvider::getTools)
            .build();
    }

    @Bean
    public McpTransport mcpTransport() {
        return new SpringWebMvcSseServerTransport();
    }
}
```

### 2. Tool Registration

Tools are registered via `CalendarToolsProvider`:

```java
@Component
@RequiredArgsConstructor
public class CalendarToolsProvider {

    private final CalendarService calendarService;
    private final ObjectMapper objectMapper;

    public List<Tool> getTools() {
        return List.of(
            buildGetEventsTool(),
            buildBlockDatesTool(),
            // ... other tools
        );
    }

    private Tool buildGetEventsTool() {
        return Tool.builder()
            .name("get_events")
            .description("Query calendar events by date range")
            .inputSchema(GetEventsSchema.create())
            .handler(this::handleGetEvents)
            .build();
    }

    private CallToolResult handleGetEvents(Map<String, Object> arguments) {
        String startDate = (String) arguments.get("start_date");
        String endDate = (String) arguments.get("end_date");

        List<CalendarEvent> events = calendarService.getEvents(startDate, endDate);

        return CallToolResult.content(
            TextContent.of(objectMapper.writeValueAsString(events))
        );
    }
}
```

### 3. JSON Schema Definition

Each tool has a JSON Schema for input validation:

```java
public class GetEventsSchema {
    public static JsonNode create() {
        return JsonNodeFactory.instance.objectNode()
            .put("type", "object")
            .set("properties", JsonNodeFactory.instance.objectNode()
                .set("start_date", JsonNodeFactory.instance.objectNode()
                    .put("type", "string")
                    .put("description", "Start date (ISO8601: yyyy-MM-dd)"))
                .set("end_date", JsonNodeFactory.instance.objectNode()
                    .put("type", "string")
                    .put("description", "End date (ISO8601: yyyy-MM-dd)"))
            )
            .set("required", JsonNodeFactory.instance.arrayNode()
                .add("start_date")
                .add("end_date"));
    }
}
```

### 4. SDK Automatic Handling

When a client sends:

```json
{
  "jsonrpc": "2.0",
  "method": "tools/call",
  "params": {
    "name": "get_events",
    "arguments": {
      "start_date": "2025-10-15",
      "end_date": "2025-10-31"
    }
  },
  "id": 1
}
```

The SDK:
1. Parses the JSON-RPC request
2. Finds the `get_events` tool
3. Calls `handleGetEvents(arguments)`
4. Wraps the result in JSON-RPC response
5. Sends via transport

---

## Testing

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=CalendarToolsProviderTest

# Integration tests
./mvnw verify
```

### Test Structure

```
src/test/java/
├── mcp/
│   └── CalendarToolsProviderTest.java  # Tool registration tests
├── service/
│   ├── CalendarServiceTest.java        # Business logic tests
│   └── GraphAPIServiceTest.java        # MS Graph integration tests
└── integration/
    └── McpServerIntegrationTest.java   # E2E tests with mcp-test
```

---

## Manual Testing with cURL

### List available tools

```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/list",
    "id": 1
  }'
```

### Call get_events tool

```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "params": {
      "name": "get_events",
      "arguments": {
        "start_date": "2025-10-15",
        "end_date": "2025-10-31"
      }
    },
    "id": 2
  }'
```

---

## Project Structure

```
calendar-mcp-service/
├── src/main/java/com/enterprise/calendar/
│   ├── CalendarMCPApplication.java
│   ├── config/
│   │   ├── McpServerConfig.java        # MCP SDK configuration
│   │   └── GraphConfig.java            # MS Graph OAuth2 config
│   ├── mcp/
│   │   ├── CalendarToolsProvider.java  # Tool registration
│   │   └── schema/                     # JSON schemas for tools
│   ├── service/
│   │   ├── CalendarService.java        # Business logic
│   │   └── GraphAPIService.java        # MS Graph integration
│   ├── model/
│   │   └── calendar/                   # DTOs (CalendarEvent, etc.)
│   ├── exception/
│   │   └── GraphAPIException.java
│   └── util/
│       └── DateUtils.java
├── src/main/resources/
│   ├── application.yml
│   └── application-dev.yml
├── pom.xml
├── .env.example
└── README.md
```

**Note:** No `controller/` or `model/mcp/` packages - the SDK handles the protocol layer!

---

## API Documentation

**Swagger UI:** `http://localhost:8080/swagger-ui.html`

---

## MCP Communication

**Protocol:** JSON-RPC 2.0 (handled by SDK)
**Transport:** SSE (Server-Sent Events) or Streamable-HTTP
**Port:** 8080

**Designed to work with:** NestJS MCP orchestrator that routes Claude's tool calls to this service.

---

## Multi-Version Java Setup

This project uses **Maven Toolchains** for multi-version Java management.

**Configuration:** See `pom.xml` and `~/.m2/toolchains.xml`

---

## License

MIT

---

## References

- [MCP Java SDK Documentation](https://github.com/modelcontextprotocol/java-sdk)
- [MCP Specification](https://modelcontextprotocol.org)
- [Microsoft Graph API](https://learn.microsoft.com/en-us/graph/)
- [Spring Boot 3.5 Docs](https://docs.spring.io/spring-boot/docs/3.5.x/reference/)