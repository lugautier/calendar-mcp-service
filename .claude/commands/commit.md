---
description: Create a detailed git commit with analyzed staged changes
---

Analyze all staged changes in the repository (git diff --cached), then create a git commit with:

1. A concise, conventional commit message title (e.g., "feat: add embedding service", "fix: resolve TypeORM connection", "docs: update README")
2. A detailed description body that includes:
   - What was changed (specific files/components)
   - Why it was changed (purpose/motivation)
   - Any important implementation details
   - Breaking changes if any

Follow these conventional commit prefixes:
- feat: New feature
- fix: Bug fix
- docs: Documentation changes
- refactor: Code refactoring
- test: Test additions/changes
- chore: Build process or auxiliary tool changes
- style: Code style changes (formatting, etc.)

IMPORTANT:
- Only commit the staged changes
- Do not stage additional files
- Do not push to remote unless explicitly requested
- Do not include any disclaimers or notes about AI assistance in the commit message