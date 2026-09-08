# AGENTS.md — Agent & Developer Automation Instructions

This document provides instructions, constraints, and architecture invariants for AI agents (such as Antigravity, Claude, Copilot) and automated tooling interacting with the `rut-validator-kmp` codebase.

---

## 1. Project Context & Philosophy

- **Repository**: `ezer-mackenzie/rut-validator-kmp`
- **Domain**: Chilean RUT/RUN (*Rol Único Tributario*) validation, parsing, formatting, and check digit generation.
- **Technology Stack**: Pure Kotlin Multiplatform (KMP 2.x), Gradle Kotlin DSL, Dokka 2.x, Vanniktech Maven Publish.
- **Core Principle**: Correctness first, zero runtime external dependencies, zero-allocation hot paths, and strict API stability.

---

## 2. Architectural Invariants

### 2.1 Package & Module Structure
- All core logic resides in `library/src/commonMain/kotlin/com/ezermackenzie/rut/`.
- Tests reside in `library/src/commonTest/kotlin/com/ezermackenzie/rut/`.
- Do **not** introduce platform-specific imports (such as `java.*` or `android.*`) inside `commonMain` or `commonTest`. Core RUT arithmetic and character manipulation is 100% platform-independent.

### 2.2 Explicit API Mode
- The Gradle configuration enforces `explicitApi()`.
- Every public declaration **must** have:
  1. An explicit visibility modifier (`public`).
  2. An explicit return type (no inferring return types on public members).
  3. Detailed KDoc documentation explaining behavior, parameters, return types, and exceptions.

### 2.3 Domain Model Design
- `Rut` is an immutable, mathematically valid representation.
- Instances can only be instantiated through validation routines:
  - `Rut.parse(input)` (throws `RutParseException`)
  - `Rut.parseOrNull(input)` (returns `null` on invalid)
  - `Rut.of(number)` (computes check digit)
  - `Rut.of(number, checkDigit)` (validates check digit)
- `RutValidationResult` is an exhaustive sealed interface hierarchy (`Valid` and `Invalid.*`). Always maintain this hierarchy to enable pattern-matching callers.

---

## 3. Git Commit Standards & Hygiene

When creating commits:
1. **Granularity**: Keep commits atomic and focused. Do not batch unrelated tasks (features, chores, docs, tests) into a single mega-commit.
2. **Conventional Commits**: Commit messages must adhere to the format:
   ```
   <type>(<scope>): <short description in present imperative tense>
   ```
   Allowed types: `feat`, `fix`, `chore`, `docs`, `test`, `refactor`, `perf`.
3. **Co-Authorship**: Every commit created in collaboration with an AI assistant or pair-programmer **must** include the co-author trailer:
   ```
   Co-authored-by: Antigravity <antigravity@google.com>
   ```

---

## 4. Build, Test, and Verification Commands

Agents must run verification before declaring tasks complete:

```powershell
# Run common unit tests on the JVM target (fastest host runner)
.\gradlew.bat jvmTest

# Run general verification tasks
.\gradlew.bat check

# Build Dokka API documentation
.\gradlew.bat dokkaGeneratePublicationHtml
```

---

## 5. Roadmap & Long-term Evolution

Refer to [temp/ROADMAP.md](temp/ROADMAP.md) for the milestone checklist, SemVer 2.0.0 policies, and binary compatibility guidelines.
