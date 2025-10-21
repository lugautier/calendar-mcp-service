# Calendar MCP Service - Development Guide

## Project Overview

**Purpose**: MCP (Model Context Protocol) server exposing Microsoft 365 Calendar operations via JSON-RPC over HTTP.

**Role**: Backend service that translates MCP tool calls from AI assistants (like Claude via NestJS orchestrator) into Microsoft Graph API operations.

**Stack**: Java 21 LTS + Spring Boot 3.5.6 + Maven + Microsoft Graph SDK 6.15.0

**Communication**: HTTP JSON-RPC endpoint at `POST http://localhost:8080/mcp`

## Architecture (with Official MCP Java SDK)

### Core Components

- **McpServer (SDK)**: Provided by MCP SDK, handles all protocol operations
- **McpTransport (SDK)**: Spring WebMVC SSE transport for HTTP streaming
- **CalendarToolsProvider**: Registers our 4 calendar tools with the SDK
- **CalendarService**: Business logic for calendar operations (get_events, block_dates, etc.)
- **GraphAPIService**: MS Graph SDK wrapper, handles OAuth2 token management

### Package Structure

```
com.enterprise.calendar/
├── config/
│   ├── McpServerConfig.java        # MCP Server bean configuration
│   └── GraphConfig.java            # MS Graph OAuth2 config
├── mcp/
│   ├── CalendarToolsProvider.java  # Tool registration and handlers
│   └── schema/
│       ├── GetEventsSchema.java
│       ├── BlockDatesSchema.java
│       ├── FindSlotsSchema.java
│       └── RescheduleEventSchema.java
├── service/
│   ├── CalendarService.java        # Business logic
│   └── GraphAPIService.java        # MS Graph integration
├── model/
│   ├── calendar/                   # Business DTOs (CalendarEvent, AvailableSlot)
│   └── graph/                      # MS Graph DTOs (if needed)
├── exception/                      # GraphAPIException
└── util/                           # DateUtils, helpers
```

**Note:** No `controller/` or `model/mcp/` packages - the SDK provides all protocol infrastructure!

### MCP Tools (4 tools to implement)

1. **get_events**: Query calendar events by date range, optional filters
2. **block_dates**: Create "Out of Office" events to block time slots
3. **find_available_slots**: Analyze calendar, detect free time slots for scheduling
4. **reschedule_event**: Update existing event to new date/time

## Technology Stack

### Runtime & Build
- **Java 21** (Eclipse Temurin LTS) - Use records, pattern matching, enhanced switch
- **Maven 3.9+** with Toolchains - Multi-version Java support configured
- **Spring Boot 3.5.6** - Latest stable, requires Java 17+ minimum

### Key Dependencies
- **spring-boot-starter-web**: REST endpoints, Tomcat, Jackson
- **spring-boot-starter-validation**: Jakarta Bean Validation
- **spring-boot-starter-actuator**: Health endpoints, metrics
- **mcp-bom 0.12.1**: Bill of Materials for MCP SDK versions
- **mcp 0.12.1**: Core MCP SDK (protocol, session, default transports)
- **mcp-spring-webmvc 0.12.1**: Spring WebMVC SSE/Streamable-HTTP transport
- **mcp-test 0.12.1**: Testing utilities for MCP servers
- **microsoft-graph 6.15.0**: MS Graph Java SDK
- **azure-identity 1.13.2**: OAuth2 client credentials flow
- **lombok 1.18.34**: Reduce boilerplate (@Data, @Builder, @Slf4j)
- **springdoc-openapi 2.6.0**: Swagger UI at /swagger-ui.html

### Database
- **None required for POC** - Token stored in memory, no state persistence

---

## MCP SDK Integration

**This project uses the official MCP Java SDK (`io.modelcontextprotocol.sdk:mcp-spring-webmvc`)**

**Key point:** The SDK handles ALL protocol operations (JSON-RPC, transport, routing). We only code:
- Tool registration (CalendarToolsProvider)
- Business logic (CalendarService, GraphAPIService)

**Architecture:**
```
McpServer (SDK) → CalendarToolsProvider → CalendarService → GraphAPIService
```

**See README.md for SDK setup details and code examples.**

## Microsoft Graph Integration

### Authentication
- **Type**: OAuth2 Client Credentials (daemon application)
- **Flow**: Token fetched at startup, auto-refresh before expiration
- **Storage**: In-memory for POC (no database)
- **Scope**: `https://graph.microsoft.com/.default`

### Required Azure AD Permissions
- `Calendars.ReadWrite` (Application-level)
- `User.Read.All` (Application-level, for multi-user availability)

### MS Graph Endpoints Used
- `GET /me/calendar/events` - List events
- `POST /me/calendar/events` - Create event (block dates)
- `PATCH /me/calendar/events/{id}` - Update event (reschedule)
- `GET /me/calendarView` - Filtered view by date range
- `POST /me/findMeetingTimes` - Find available slots

### Configuration (application.yml)
```yaml
microsoft:
  graph:
    tenant-id: ${MS_TENANT_ID}
    client-id: ${MS_CLIENT_ID}
    client-secret: ${MS_CLIENT_SECRET}
    scope: https://graph.microsoft.com/.default
```

**NEVER hardcode credentials** - Always use environment variables via `.env` file (see `.env.example`)

## Development Best Practices

### Code Style
- **Use Lombok**: @Data, @Builder, @Slf4j, @RequiredArgsConstructor
- **Use Java 21 features**: Records for DTOs, pattern matching, enhanced switch
- **Use Spring conventions**: @RestController, @Service, @Configuration
- **Dependency Injection**: Constructor injection (final fields + @RequiredArgsConstructor)

### Validation
- **Input validation**: Jakarta Bean Validation (@NotNull, @Valid, @Pattern)
- **Date formats**: ISO8601 only (yyyy-MM-dd, yyyy-MM-dd'T'HH:mm:ss)
- **Event IDs**: Validate UUID format before MS Graph calls
- **Sanitization**: Never trust external input, validate all parameters

### Error Handling
- **Custom exceptions**: MCPException, GraphAPIException with clear messages
- **Global handler**: @ControllerAdvice for consistent error responses
- **Logging**: Use @Slf4j, log errors with context (request ID, user, tool name)
- **Retry logic**: Max 3 retries with exponential backoff for MS Graph transient errors

### Testing
- **Unit tests**: JUnit 5 + Mockito for services
- **Integration tests**: @SpringBootTest with @MockBean for MS Graph
- **Mock MS Graph**: Never call real MS Graph in tests, use WireMock or MockBean
- **Coverage target**: >70% for business logic

### Configuration Management
- **Profiles**: `dev` (debug logging), `prod` (info logging)
- **Environment variables**: All secrets via environment, template in `.env.example`
- **Port**: 8080 (hardcoded, expected by NestJS orchestrator)

## Maven & Build

### Multi-Version Java Setup
- **Toolchains configured**: Project uses Java 21 via `maven-toolchains-plugin`
- **Global registry**: `~/.m2/toolchains.xml` declares available JDKs
- **Per-project requirement**: `pom.xml` specifies Java 21
- **Benefit**: Work on multiple projects with different Java versions without PATH changes

### Common Commands
```bash
# Build
./mvnw clean install

# Run locally
./mvnw spring-boot:run

# Run with dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
./mvnw test

# Skip tests
./mvnw clean install -DskipTests

# Package JAR
./mvnw clean package
```

### Build Output
- JAR location: `target/calendar-mcp-service-0.0.1-SNAPSHOT.jar`
- Executable: `java -jar target/*.jar`

## Communication with NestJS Orchestrator

### Flow
1. User asks Claude: "What are my meetings tomorrow?"
2. Claude decides to use `calendar_server.get_events` tool
3. NestJS orchestrator constructs JSON-RPC request
4. POST to `http://localhost:8080/mcp` with MCP payload
5. Java service processes, calls MS Graph, returns MCP response
6. NestJS forwards result to Claude
7. Claude synthesizes natural language answer to user

### Contract
- **Java service is stateless**: No session, no user context from orchestrator
- **NestJS discovers tools**: Via `tools/list` method at startup
- **Java only knows MCP protocol**: Doesn't care who calls it (testable with cURL/Postman)

## Security Considerations

### Credentials
- Azure credentials in `.env` file (NEVER commit)
- `.gitignore` configured to exclude `.env` but include `.env.example`
- Token stored in memory, not logged

### Input Validation
- Validate all dates (format, range, logical consistency)
- Validate event IDs before passing to MS Graph
- Sanitize all user-provided strings
- Never execute dynamic code from input

### Error Messages
- Client-facing: Generic, no stack traces, no internal paths
- Server logs: Detailed with request context for debugging

## Testing Strategy

### Manual Testing (Postman/cURL)
```bash
# Test tools/list
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","method":"tools/list","id":1}'

# Test get_events
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","method":"tools/call","params":{"name":"get_events","arguments":{"start_date":"2025-10-15","end_date":"2025-10-31"}},"id":2}'
```

### Unit Test Example (SDK-based)
```java
@Test
void shouldRegisterGetEventsTool() {
    // Test tool registration
    List<Tool> tools = toolsProvider.getTools();

    assertThat(tools).hasSize(4);
    assertThat(tools).extracting("name").contains("get_events");
}

```

## Logging Strategy

- **DEBUG**: Request/response payloads (dev profile only)
- **INFO**: Tool execution start/success (request ID, tool name, duration)
- **WARN**: Validation failures, non-critical MS Graph errors
- **ERROR**: Exceptions, MS Graph failures, critical errors

## Performance Considerations

- **Token caching**: Reuse MS Graph token until near expiration
- **Timeout**: 30s max per request to MS Graph
- **Pagination**: Handle large calendar responses (>100 events)
- **Async**: Consider @Async for non-blocking MS Graph calls (future enhancement)

## Documentation

- **README.md**: Project overview, setup instructions, usage examples
- **TODO.md**: Full specification, 7-day plan, technical details
- **Swagger UI**: Auto-generated at `http://localhost:8080/swagger-ui.html`
- **Code comments**: Javadoc for public APIs, inline for complex logic

## Guidelines for Claude Code

When working on this project:

1. **Always check TODO.md** for detailed specifications and current progress at the start of any session
2. **Use the MCP SDK**: Don't implement custom JSON-RPC parsing or protocol handling
3. **Use Java 21 features**: Records for DTOs, pattern matching, switch expressions
4. **Follow Spring Boot conventions**: Annotations, dependency injection, profiles
5. **Validate all inputs**: Never trust external data, especially dates
6. **Mock MS Graph in tests**: Never call real API during test execution
7. **Use Lombok**: Reduce boilerplate with @Data, @Builder, @Slf4j
8. **Log with context**: Include tool name, arguments, execution time in logs
9. **Keep secrets safe**: Never hardcode credentials, use environment variables
11. **Explain all Java concepts in French before implementing**: Consider the developer is new to Java/Spring Boot
12. **No comments that are not production-ready**. Only write necessary comments.

### SDK-Specific Best Practices

**Tool Registration:**
- Use `Tool.builder()` from SDK
- Define clear JSON schemas (JSON Schema Draft 7)
- Handlers should be pure: arguments → business logic → CallToolResult
- Extract arguments validation to separate methods

**Error Handling:**
- Throw exceptions with clear messages in handlers
- SDK automatically converts to JSON-RPC errors
- Use custom exceptions (GraphAPIException) for business errors
- Log before throwing

**Testing:**
- Test tool registration: `toolsProvider.getTools()` returns expected tools
- Test handlers in isolation: mock CalendarService
- Test business logic: mock GraphAPIService
- Use `@MockBean` for MS Graph SDK
- Use `mcp-test` utilities for integration tests

**Code Organization:**
- McpServerConfig: Bean definitions only
- CalendarToolsProvider: Tool registration + handlers
- Schema classes: One per tool, static factory methods
- CalendarService: Pure business logic, no MCP coupling
- GraphAPIService: MS Graph only, no MCP coupling

### Test-Driven Development (TDD) Workflow

**When the user requests TDD implementation (RED → GREEN → REFACTOR), follow this strict workflow:**

#### 1. TodoWrite Tool Usage in TDD

**ALWAYS use TodoWrite to track TDD progress:**
- Create a todo list at the start of TDD session with planned test cycles
- Mark each test as `in_progress` when starting RED phase
- Mark as `completed` when that cycle's REFACTOR phase is done
- Update the list as new tests are discovered during implementation

**Example todo list for TDD:**
```
1. [in_progress] Test 1: POST /mcp endpoint accepts JSON (RED→GREEN→REFACTOR)
2. [pending] Test 2: Response has JSON-RPC structure (RED→GREEN→REFACTOR)
3. [pending] Test 3: Response echoes request ID (RED→GREEN→REFACTOR)
4. [pending] Test 4: tools/list returns 4 tools (RED→GREEN→REFACTOR)
```

#### 2. Commit Points in TDD

**Notify me when we're at a good commit point**

**Good commit points:**
- After 1-3 related TDD cycles are complete (all GREEN + REFACTORED)
- After a complete feature slice works end-to-end
- Before switching to a different component or layer

**Example commit flow:**
1. Complete Test 1-3 (basic endpoint + response structure) → **COMMIT 1**
2. Complete Test 4-6 (DTOs with serialization) → **COMMIT 2**
3. Complete Test 7-9 (service layer with tools/list) → **COMMIT 3**

#### 3. TDD Session Flow Example

**User says:** "Let's implement the MCP protocol layer using TDD"

**Claude's workflow:**
1. **Create todo list** with planned TDD cycles (TodoWrite)
2. **Test 1 - RED**: Write failing test for POST /mcp endpoint
3. **Test 1 - GREEN**: Create minimal MCPController to pass test
4. **Test 1 - REFACTOR**: Clean up code (if needed)
5. **Test 2 - RED**: Write failing test for JSON-RPC response structure
6. **Test 2 - GREEN**: Add MCPResponse DTO, make test pass
7. **Test 2 - REFACTOR**: Apply Lombok, improve naming
8. **Notify user**: "Tests 1-2 complete. Good commit point."
9. **User stages and commits**: `git add ...` then `/commit-tdd`
10. **Repeat** for next TDD cycles

Don't forget to explain all Java concepts in French before implementing.

### Git Workflow Rules

**CRITICAL: Never manage git staging or commits autonomously**

- **NEVER** run `git add` to stage files yourself
- **NEVER** run `git commit` unless the user explicitly types a commit command (`/commit`, `/commit-short`, `/commit-tdd`)
- **ALWAYS** wait for the user to decide when and what to commit
- **ONLY** create commits when the user invokes a commit slash command

The user controls the git workflow entirely. Your role is to write code and tests, not to manage version control.

## Quick Reference

- **Port**: 8080
- **Endpoint**: POST /mcp
- **Health**: GET /actuator/health
- **Swagger**: http://localhost:8080/swagger-ui.html
- **Java Version**: 21 (managed by Maven Toolchains)
- **Profiles**: default, dev
- **Protocol**: MCP (JSON-RPC 2.0)
- **Integration**: Microsoft Graph SDK 6.15.0
