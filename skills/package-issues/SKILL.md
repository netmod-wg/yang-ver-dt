---
name: package-issues
description: List open GitHub issues labeled "packages" for the current repository using `gh`, or show full markdown comments for a specified issue number. Use when a user asks for open YANG package issues or wants to read comments on a package issue.
---

# Package Issues

## Overview

Use the GitHub CLI to fetch open issues labeled `packages` in the current repo and present a markdown table with issue number, title, tags, and last update date. When `--issue <num>` is provided, fetch and print full markdown comments for that issue.

## Quick Start

Run:

```bash
autocomplete
scala-cli scripts/package_issues.scala
scala-cli scripts/package_issues.scala -- --issue 254
```

## Workflow

1. By default, run `gh issue list --state open --label packages --json number,title,labels,updatedAt --limit 200`.
2. Format labels as a comma-separated list.
3. Convert `updatedAt` to `YYYY-MM-DD`.
4. Output a markdown table with columns `Issue #`, `Title`, `Tags`, `Last Updated`.
5. If `--issue <num>` is provided, run `gh issue view <num> --json number,title,comments`.
6. Output full markdown comments with author and timestamp.

## Output Format

For list mode, return a table like:

```markdown
| Issue # | Title | Tags | Last Updated |
| --- | --- | --- | --- |
| 123 | Example issue title | packages, yang | 2026-02-03 |
```

For comments mode (`--issue <num>`), return full markdown like:

```markdown
## Issue 123: Example issue title

### Comment 1 (author, 2026-02-03)

Full comment body...
```

## Resources

### scripts/

- `scripts/package_issues.scala`: Lists open issues labeled `packages` or prints full markdown comments for a specified issue.
