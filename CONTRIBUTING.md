# Contributing to rut-validator-kmp

Thank you for your interest in contributing to **rut-validator-kmp**! We are committed to maintaining a high-performance, robust, and mathematically sound library across all Kotlin Multiplatform targets.

---

## Code of Conduct

All contributors and maintainers are expected to adhere to our [Code of Conduct](CODE_OF_CONDUCT.md). Please read it before participating.

---

## Git Commit Standards

This repository strictly enforces the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) specification. Every commit message must be structured as follows:

```
<type>(<scope>): <short summary>

[optional body]

[optional footer(s)]
```

### Allowed Types
- **`feat`**: Introduces a new feature or public capability.
- **`fix`**: Patches a bug or addresses an edge-case calculation issue.
- **`docs`**: Documentation changes, KDoc updates, or README enhancements.
- **`chore`**: Build script updates, dependency bumps, or tool configurations.
- **`test`**: Adds or modifies unit tests and verification vectors.
- **`refactor`**: Code restructuring without altering public behavior.
- **`perf`**: Performance optimizations reducing allocations or execution time.

### Co-Authorship Requirement
When collaborating with AI assistants (e.g., Antigravity) or pairing with fellow developers, the commit trailer **must** include the co-author attribution:

```
Co-authored-by: Antigravity <antigravity@google.com>
```

---

## Development Principles & Best Practices

1. **Explicit API Mode**:
   - `explicitApi()` is turned on in Gradle.
   - All public classes, objects, interfaces, properties, and functions **must** explicitly state visibility (`public`).
   - All public functions and properties **must** declare explicit return types.
   - All public symbols **must** include thorough KDoc comments with `@param`, `@return`, and `@throws` where applicable.

2. **Zero-Allocation Hot Paths**:
   - Avoid creating temporary regex instances, string splits, or intermediate substrings in validation loops.
   - Validation and normalization should traverse characters iteratively.

3. **Multiplatform Parity**:
   - Core domain logic must remain pure Kotlin in `commonMain`. Do not introduce platform-specific imports (e.g. `java.*` or `android.*`) unless isolated behind `expect`/`actual` declarations.

4. **Testing Standards**:
   - Every feature or bug fix must come with accompanying tests in `commonTest`.
   - Test vectors must verify edge cases: boundary values (`1-9`), high numbers, check digits `'0'` and `'K'`, lowercase `'k'`, spaces, and malformed inputs.

---

## Verification Commands

Before opening a pull request, run the following commands locally:

```bash
# Run unit tests on the JVM target
./gradlew jvmTest

# Run Kotlin compilation and code style checks
./gradlew check

# Generate Dokka documentation
./gradlew dokkaGeneratePublicationHtml
```

---

## Submitting a Pull Request

1. Fork the repository and create your feature branch:
   ```bash
   git checkout -b feat/my-new-feature
   ```
2. Commit your changes following Conventional Commits and co-authorship guidelines.
3. Ensure all tests pass.
4. Push to your branch and open a Pull Request against `main`.
5. Provide a clear description in your PR of what changed and reference any relevant issues.
