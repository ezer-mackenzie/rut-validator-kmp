# rut-validator-kmp

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin%20Multiplatform-KMP-blue.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org/docs/multiplatform.html)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](CONTRIBUTING.md)

**rut-validator-kmp** is an ultra-fast, zero-dependency, pure Kotlin Multiplatform (KMP) library for validating, parsing, formatting, and generating Chilean **RUT / RUN** (*Rol Único Tributario / Rol Único Nacional*) numbers.

Built adhering to the strictest Kotlin library best practices—including **Explicit API mode**, exhaustive sealed result hierarchies, zero-allocation character scanners, and binary stability guarantees.

---

## Supported Targets

| Platform | Target Identifier | Minimum Version / Architecture |
| :--- | :--- | :--- |
| **JVM** | `jvm` | Java 11+ |
| **Android** | `android` | API 24+ (Android 7.0+) |
| **iOS** | `iosArm64`, `iosSimulatorArm64` | iOS 12+ (64-bit ARM & Simulator) |
| **Linux** | `linuxX64` | x86_64 Linux |

---

## Key Features

- **100% Pure Kotlin Multiplatform**: Zero third-party runtime dependencies. All core logic lives in `commonMain`.
- **Strict Mathematical Accuracy**: Full compliance with the Chilean Civil Registry and SII Modulo 11 specification, handling `'0'`, `'K'`, and `'1'..'9'`.
- **Zero-Allocation Hot Paths**: Character-by-character scanner avoiding regex allocations during high-throughput validation and real-time input masking.
- **Type-Safe Domain Model**: Immutable, comparable `Rut` value representation.
- **Exhaustive Error Diagnostics**: Sealed `RutValidationResult` hierarchy returning specific failure reasons (`EmptyInput`, `InvalidLength`, `InvalidCharacter`, `InvalidCheckDigit`, `InvalidNumber`).
- **Flexible Formatting**: Effortlessly convert between `DOTS_AND_HYPHEN` (`12.345.678-5`), `HYPHEN_ONLY` (`12345678-5`), and `UNFORMATTED` (`123456785`).
- **Ergonomic Extensions**: Intuitive Kotlin DSL extensions on `String`, `CharSequence`, `Long`, and `Int`.
- **Explicit API Mode**: 100% documented public surface with explicit visibility and return types.

---

## Installation

### Gradle (Kotlin DSL)
Add the dependency to your `commonMain` source set:

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.ezermackenzie:rut-validator-kmp:1.0.0")
        }
    }
}
```

### Gradle (Groovy)
```groovy
implementation 'io.github.ezermackenzie:rut-validator-kmp:1.0.0'
```

### Maven
```xml
<dependency>
    <groupId>io.github.ezermackenzie</groupId>
    <artifactId>rut-validator-kmp</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## Quick Start & Usage

### 1. Simple Boolean Validation

```kotlin
import com.ezermackenzie.rut.isValidRut

// Direct extension call
val isValid = "12.345.678-5".isValidRut() // true
val isInvalid = "12.345.678-9".isValidRut() // false

// Handles unformatted, dotted, and lowercase 'k'
"21305614k".isValidRut() // true
" 12.345.678-5 ".isValidRut() // true (leading/trailing whitespace trimmed)
```

### 2. Strongly Typed Parsing (`Rut`)

```kotlin
import com.ezermackenzie.rut.Rut
import com.ezermackenzie.rut.toRut
import com.ezermackenzie.rut.toRutOrNull

// Throws RutParseException if invalid
val rut: Rut = "12.345.678-5".toRut()
println(rut.number)      // 12345678
println(rut.checkDigit)  // '5'

// Safe parsing returning null on error
val safeRut: Rut? = "invalid-rut".toRutOrNull() // null
```

### 3. Detailed Diagnostic Validation (`RutValidationResult`)

For user-facing forms, inspect the exact failure reason:

```kotlin
import com.ezermackenzie.rut.Rut
import com.ezermackenzie.rut.RutValidationResult

when (val result = Rut.validate("12.345.678-9")) {
    is RutValidationResult.Valid -> {
        println("Valid RUT: ${result.rut.formatted}")
    }
    is RutValidationResult.Invalid.InvalidCheckDigit -> {
        println("Wrong DV! Expected ${result.expected}, got ${result.actual}")
    }
    is RutValidationResult.Invalid.InvalidCharacter -> {
        println("Illegal character '${result.char}' at index ${result.index}")
    }
    is RutValidationResult.Invalid.InvalidLength -> {
        println("RUT length is invalid: ${result.actualLength}")
    }
    is RutValidationResult.Invalid.EmptyInput -> {
        println("Please provide a RUT.")
    }
    is RutValidationResult.Invalid.InvalidNumber -> {
        println("Number error: ${result.reason}")
    }
}
```

### 4. Formatting Styles

```kotlin
import com.ezermackenzie.rut.Rut
import com.ezermackenzie.rut.RutFormatStyle
import com.ezermackenzie.rut.formatRut

val rut = Rut.parse("123456785")

println(rut.format(RutFormatStyle.DOTS_AND_HYPHEN)) // "12.345.678-5"
println(rut.formatted)                              // "12.345.678-5"

println(rut.format(RutFormatStyle.HYPHEN_ONLY))     // "12345678-5"
println(rut.canonical)                              // "12345678-5"

println(rut.format(RutFormatStyle.UNFORMATTED))     // "123456785"
println(rut.unformatted)                            // "123456785"

// Direct string extension
val canonical = "12.345.678-5".formatRut(RutFormatStyle.HYPHEN_ONLY) // "12345678-5"
```

### 5. Check Digit Calculation & Factory Methods

```kotlin
import com.ezermackenzie.rut.Rut
import com.ezermackenzie.rut.toRut

// Calculate Modulo 11 check digit for any positive Long
val checkDigit = Rut.calculateCheckDigit(12345678L) // '5'
val kDigit = Rut.calculateCheckDigit(6L)            // 'K'
val zeroDigit = Rut.calculateCheckDigit(14L)        // '0'

// Create Rut directly from number
val rutFromLong = 12345678L.toRut()
println(rutFromLong.formatted) // "12.345.678-5"

// Create Rut verifying given check digit
val customRut = Rut.of(12345678L, '5')
```

### 6. Natural Sorting & Comparison

`Rut` implements `Comparable<Rut>`, ordering instances numerically:

```kotlin
val r1 = Rut.parse("1-9")
val r2 = Rut.parse("14-0")
val r3 = Rut.parse("12.345.678-5")

val sorted = listOf(r3, r1, r2).sorted()
// Result: [1-9, 14-0, 12.345.678-5]
```

### 7. Kotlin Destructuring Declarations

Decompose any `Rut` instance into its numeric body and check digit:

```kotlin
val (number, checkDigit) = Rut.parse("12.345.678-5")
println(number)     // 12345678
println(checkDigit) // '5'
```

### 8. Cleaning & Normalization

Extract unformatted digits and normalize `'k'` to uppercase `'K'` from dirty inputs:

```kotlin
import com.ezermackenzie.rut.cleanRut

val clean = " 12.345.678-k ".cleanRut() // "12345678K"
val direct = Rut.clean("12.345.678-5")    // "123456785"
```

### 9. Real-Time UI Input Masking / Live Formatting

Effortlessly format input in real time as users type in mobile or web forms:

```kotlin
import com.ezermackenzie.rut.formatPartialRut

"12345".formatPartialRut()     // "1.234-5"
"123456785".formatPartialRut() // "12.345.678-5"
"21305614k".formatPartialRut() // "21.305.614-K"
```

### 10. Random Valid RUT Generator (Testing & QA)

Generate mathematically valid RUTs for unit tests, mock databases, and seeding:

```kotlin
// Random RUT in standard range (1,000,000 to 99,999,999)
val randomRut = Rut.random()
println(randomRut.formatted) // e.g. "18.492.103-7"

// Custom number range
val smallRut = Rut.random(range = 100L..999L)
```

---

## API Documentation (Dokka)

Full API documentation is generated using **Dokka**:

```bash
# Generate HTML API docs in build/dokka/html
./gradlew dokkaGeneratePublicationHtml
```

For more details on the API evolution, SemVer stability policy, and future milestones, read the [Roadmap & API Stability Blueprint](temp/ROADMAP.md).

---

## Contributing

We welcome contributions! Please review:
- [CHANGELOG.md](CHANGELOG.md) — Release notes and history of changes.
- [CONTRIBUTING.md](CONTRIBUTING.md) — Guidelines on workflow, Conventional Commits, and co-authorship.
- [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) — Contributor Covenant Code of Conduct.
- [AGENTS.md](AGENTS.md) — Repository guide for automated tools and AI coding agents.

---

## License

```
Copyright 2026 Eli-ezer Reuven Ramirez Ruiz

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```