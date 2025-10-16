---
description: Create a TDD-style git commit with RED-GREEN-REFACTOR cycles documented
---

Analyze all staged changes in the repository (git diff --cached), then create a git commit with:

1. A conventional commit message title with (with TDD) flag:
   - Format: "feat: (with TDD) description" or "test: (with TDD) description"
   - Use "feat" if the commit contains production code + tests
   - Use "test" only if it's purely adding test infrastructure

2. A detailed description body documenting the TDD process:
   - Group changes into TDD cycles
   - For each cycle, document:
     * RED: The failing test that was written
     * GREEN: The minimal code to make it pass
     * REFACTOR: Any improvements made (or state "none needed")
   - At the end, list key files changed

Example format:
```
feat: (with TDD) implement JSON-RPC protocol foundation

TDD Cycle 1 - Basic endpoint:
RED: Test POST /mcp accepts JSON and returns 200
GREEN: Implement MCPController with minimal handler
REFACTOR: (none needed)

TDD Cycle 2 - JSON-RPC structure:
RED: Test response has jsonrpc, result, id fields
GREEN: Update response with proper structure
REFACTOR: (none needed)

TDD Cycle 3 - Tool discovery:
RED: Test tools/list returns 4 tools with names
GREEN: Create MCPProtocolService, implement tools/list
REFACTOR: Extract tool definitions to separate method

Files changed:
- MCPController.java (production)
- MCPProtocolService.java (production)
- MCPRequest.java, MCPResponse.java (DTOs)
- MCPControllerTest.java (tests)
```

Follow these conventional commit prefixes:
- feat: New feature with tests (most TDD commits)
- test: Test infrastructure only
- refactor: Pure refactoring with existing tests
- fix: Bug fix with regression test

IMPORTANT:
- Document the TDD thought process clearly
- Show RED-GREEN-REFACTOR for each cycle
- Make it educational for reviewers
- Only commit the staged changes
- Do not stage additional files
- Do not push to remote unless explicitly requested
- Do not include any disclaimers or notes about AI assistance in the commit message
