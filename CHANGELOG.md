# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-09-08

### Added
- **Pure Kotlin Multiplatform Core**:
  - `Rut` immutable domain entity representing mathematically valid Chilean RUT / RUN numbers with Modulo 11 check digit calculation.
  - `RutFormatStyle` enum supporting `DOTS_AND_HYPHEN` (`12.345.678-5`), `HYPHEN_ONLY` (`12345678-5`), and `UNFORMATTED` (`123456785`).
  - `RutValidationResult` exhaustive sealed interface hierarchy (`Valid`, `Invalid.EmptyInput`, `Invalid.InvalidLength`, `Invalid.InvalidCharacter`, `Invalid.InvalidCheckDigit`, `Invalid.InvalidNumber`).
  - `RutParseException` strongly typed exception with diagnostic failure payload.
  - Kotlin destructuring declarations support (`val (number, checkDigit) = rut`).
  - `Rut.clean()` and `CharSequence.cleanRut()` for normalizing dirty inputs and converting check digit `'k'` to uppercase `'K'`.
  - `Rut.formatPartial()` and `CharSequence.formatPartialRut()` for real-time UI text field input masking without waiting for complete input.
  - `Rut.random()` for generating mathematically valid RUTs for unit testing, mocking, and QA database seeding.
  - Java interoperability annotations (`@JvmStatic`, `@JvmOverloads`) on `Rut.Companion`.
  - Extension functions for `CharSequence`, `String`, `Long`, and `Int`.
- **Quality & Developer Experience**:
  - Enabled Kotlin Explicit API Mode (`explicitApi()`) to ensure strict visibility, explicit return types, and complete KDocs on all public declarations.
  - Integrated Dokka 2.x documentation engine (`org.jetbrains.dokka:2.2.0`) for generating HTML API documentation via `./gradlew dokkaGeneratePublicationHtml`.
  - Comprehensive unit test suite in `commonTest` covering real Chilean test vectors, edge cases, length boundaries, and invalid characters.
- **Documentation Suite (in English)**:
  - `README.md` with badges, installation snippets (Gradle KTS / Groovy / Maven), interactive usage guide, and Dokka reference.
  - `CONTRIBUTING.md` defining workflow, Conventional Commits specification, and co-authorship guidelines.
  - `CODE_OF_CONDUCT.md` adhering to Contributor Covenant v2.1.
  - `AGENTS.md` containing architectural invariants and automation rules for AI agents and developer tooling.
  - `CLAUDE.md` linking directly to `AGENTS.md`.
- **Automation & CI**:
  - Added `.github/dependabot.yml` for automated weekly updates to Gradle dependencies and GitHub Actions workflows.

### Fixed
- Fixed executable permissions on `gradlew` by registering mode `100755` in the Git index (`git update-index --chmod=+x gradlew`), resolving the `gradlew is not executable` error on Linux and macOS GitHub Actions runners.
- Added explicit `run: chmod +x ./gradlew` steps in `gradle.yml` and `publish.yml` workflows as defense-in-depth.
- Upgraded CI workflows from Node 20 / deprecated actions to `actions/setup-java@v4` (JDK 21) and `gradle/actions/setup-gradle@v4`, eliminating Node 20 deprecation warnings.

### Changed
- Configured publishing coordinates to `io.github.ezermackenzie:rut-validator-kmp` with full POM metadata.
- Cleaned up obsolete Fibonacci multiplatform template files across `commonMain`, `androidMain`, `iosMain`, `jvmMain`, and `linuxX64Main`.
