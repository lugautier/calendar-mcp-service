---
description: Create a concise git commit with analyzed staged changes
---

Analyze all staged changes in the repository (git diff --cached), then create a git commit with:

1. A concise, conventional commit message title (e.g., "feat: add embedding service")
2. A brief description body (2-4 lines max):
   - What was changed (high-level summary)
   - Why it was changed (key motivation in one line)

IMPORTANT: Keep it short and quickly readable. No detailed explanations.

Follow these conventional commit prefixes:
- feat: New feature
- fix: Bug fix
- docs: Documentation changes
- refactor: Code refactoring
- test: Test additions/changes
- chore: Build process or auxiliary tool changes

IMPORTANT:
- Only commit the staged changes
- Do not stage additional files
- Do not push to remote unless explicitly requested
- Do not include any disclaimers or notes about AI assistance in the commit message