package com.ezermackenzie.rut

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.random.Random

/**
 * Immutable domain model representing a validated Chilean RUT (Rol Único Tributario) / RUN (Rol Único Nacional).
 *
 * A RUT consists of a numeric body ([number]) and a verification check digit ([checkDigit]),
 * mathematically validated using the official Modulo 11 algorithm.
 *
 * Instances of this class are guaranteed to represent a valid, mathematically sound RUT.
 * To construct an instance, use [Rut.parse], [Rut.parseOrNull], [Rut.of], or [Rut.random].
 *
 * @property number The numeric body of the RUT (excluding the verification digit).
 * @property checkDigit The verification character (always normalized to uppercase `'0'`..`'9'` or `'K'`).
 */
public class Rut private constructor(
    public val number: Long,
    public val checkDigit: Char
) : Comparable<Rut> {

    /**
     * Standard formatted presentation with thousand separator dots and hyphen.
     * Example: `"12.345.678-5"`
     */
    public val formatted: String
        get() = format(RutFormatStyle.DOTS_AND_HYPHEN)

    /**
     * Canonical representation with hyphen separator and no dots.
     * Example: `"12345678-5"`
     */
    public val canonical: String
        get() = format(RutFormatStyle.HYPHEN_ONLY)

    /**
     * Raw unformatted representation without dots or hyphens.
     * Example: `"123456785"`
     */
    public val unformatted: String
        get() = format(RutFormatStyle.UNFORMATTED)

    /**
     * Formats this RUT according to the specified [style].
     *
     * @param style The desired formatting style. Defaults to [RutFormatStyle.DOTS_AND_HYPHEN].
     * @return The formatted RUT string representation.
     */
    public fun format(style: RutFormatStyle = RutFormatStyle.DOTS_AND_HYPHEN): String {
        return when (style) {
            RutFormatStyle.UNFORMATTED -> "$number$checkDigit"
            RutFormatStyle.HYPHEN_ONLY -> "$number-$checkDigit"
            RutFormatStyle.DOTS_AND_HYPHEN -> formatWithDotsAndHyphen()
        }
    }

    private fun formatWithDotsAndHyphen(): String {
        val numStr = number.toString()
        val len = numStr.length
        if (len <= 3) {
            return "$numStr-$checkDigit"
        }
        val firstGroupLen = if (len % 3 == 0) 3 else len % 3
        val dotCount = (len - 1) / 3
        val builder = StringBuilder(len + dotCount + 2)

        builder.append(numStr, 0, firstGroupLen)
        var index = firstGroupLen
        while (index < len) {
            builder.append('.')
            builder.append(numStr, index, index + 3)
            index += 3
        }
        builder.append('-').append(checkDigit)
        return builder.toString()
    }

    /**
     * Returns the numeric body of this RUT for destructuring declarations:
     * `val (number, checkDigit) = rut`
     */
    public operator fun component1(): Long = number

    /**
     * Returns the verification check digit of this RUT for destructuring declarations:
     * `val (number, checkDigit) = rut`
     */
    public operator fun component2(): Char = checkDigit

    /**
     * Compares this RUT with another RUT based on their numeric bodies.
     */
    override fun compareTo(other: Rut): Int = this.number.compareTo(other.number)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Rut) return false
        return number == other.number && checkDigit == other.checkDigit
    }

    override fun hashCode(): Int {
        var result = number.hashCode()
        result = 31 * result + checkDigit.hashCode()
        return result
    }

    /**
     * Returns the standard formatted representation ([formatted]) of this RUT.
     */
    override fun toString(): String = formatted

    public companion object {
        /**
         * Minimum valid numeric body for a Chilean RUT.
         */
        public const val MIN_NUMBER: Long = 1L

        /**
         * Maximum supported numeric body for a Chilean RUT (9 digits).
         */
        public const val MAX_NUMBER: Long = 999_999_999L

        /**
         * Calculates the verification check digit (Dígito Verificador) for the given positive [number]
         * using the official Modulo 11 algorithm.
         *
         * @param number The numeric body of the RUT (must be positive and `<= [MAX_NUMBER]`).
         * @return The calculated check digit as uppercase `'0'`..`'9'` or `'K'`.
         * @throws IllegalArgumentException If [number] is less than 1 or greater than [MAX_NUMBER].
         */
        @JvmStatic
        public fun calculateCheckDigit(number: Long): Char {
            require(number in MIN_NUMBER..MAX_NUMBER) {
                "RUT number must be between $MIN_NUMBER and $MAX_NUMBER, but was $number."
            }
            var sum = 0L
            var multiplier = 2L
            var temp = number
            while (temp > 0L) {
                sum += (temp % 10L) * multiplier
                temp /= 10L
                multiplier = if (multiplier == 7L) 2L else multiplier + 1L
            }
            val remainder = 11L - (sum % 11L)
            return when (remainder) {
                11L -> '0'
                10L -> 'K'
                else -> ('0'.code + remainder.toInt()).toChar()
            }
        }

        /**
         * Creates a [Rut] instance from a numeric [number], automatically calculating its check digit.
         *
         * @param number The numeric body of the RUT.
         * @return A valid [Rut] instance.
         * @throws IllegalArgumentException If [number] is not within valid boundaries.
         */
        @JvmStatic
        public fun of(number: Long): Rut {
            val cd = calculateCheckDigit(number)
            return Rut(number, cd)
        }

        /**
         * Creates a [Rut] instance from a [number] and [checkDigit], validating that they match.
         *
         * @param number The numeric body of the RUT.
         * @param checkDigit The verification character (case-insensitive).
         * @return A valid [Rut] instance.
         * @throws RutParseException If the provided check digit does not match the calculated one.
         */
        @JvmStatic
        public fun of(number: Long, checkDigit: Char): Rut {
            if (number !in MIN_NUMBER..MAX_NUMBER) {
                throw RutParseException(
                    RutValidationResult.Invalid.InvalidNumber(
                        "Number must be between $MIN_NUMBER and $MAX_NUMBER, but was $number"
                    )
                )
            }
            val normalizedCd = checkDigit.uppercaseChar()
            val expectedCd = calculateCheckDigit(number)
            if (normalizedCd != expectedCd) {
                throw RutParseException(
                    RutValidationResult.Invalid.InvalidCheckDigit(expectedCd, normalizedCd)
                )
            }
            return Rut(number, normalizedCd)
        }

        /**
         * Validates a RUT string representation and returns a detailed [RutValidationResult].
         *
         * Accepts input formatted with dots, hyphens, spaces, or raw compact digits,
         * with case-insensitive check digit (`'k'` or `'K'`).
         *
         * @param input The character sequence to validate.
         * @return [RutValidationResult.Valid] containing the [Rut] on success, or an instance of
         * [RutValidationResult.Invalid] detailing the failure reason.
         */
        @JvmStatic
        public fun validate(input: CharSequence?): RutValidationResult {
            if (input == null) {
                return RutValidationResult.Invalid.EmptyInput
            }

            // Find trimmed bounds
            var start = 0
            var end = input.length - 1
            while (start <= end && input[start].isWhitespace()) {
                start++
            }
            while (end >= start && input[end].isWhitespace()) {
                end--
            }

            if (start > end) {
                return RutValidationResult.Invalid.EmptyInput
            }

            // Scan and validate characters
            var bodyNumber = 0L
            var bodyDigitsCount = 0
            var hyphenFound = false
            var checkDigitChar: Char? = null

            for (i in start..end) {
                val c = input[i]
                when {
                    c.isDigit() -> {
                        if (checkDigitChar != null) {
                            // Digits cannot appear after the check digit was already encountered
                            return RutValidationResult.Invalid.InvalidCharacter(c, i)
                        }
                        // Check if this digit is before or after hyphen
                        if (hyphenFound) {
                            checkDigitChar = c
                        } else {
                            val digitVal = c.code - '0'.code
                            if (bodyDigitsCount >= 9) {
                                return RutValidationResult.Invalid.InvalidLength(bodyDigitsCount + 2)
                            }
                            bodyNumber = bodyNumber * 10L + digitVal
                            bodyDigitsCount++
                        }
                    }
                    c == 'k' || c == 'K' -> {
                        if (checkDigitChar != null) {
                            return RutValidationResult.Invalid.InvalidCharacter(c, i)
                        }
                        // 'K' can only be the check digit
                        checkDigitChar = 'K'
                    }
                    c == '-' -> {
                        if (hyphenFound || i == start || i == end || checkDigitChar != null) {
                            return RutValidationResult.Invalid.InvalidCharacter(c, i)
                        }
                        hyphenFound = true
                    }
                    c == '.' -> {
                        // Dots are allowed as thousand separators, cannot be first, last, adjacent to hyphen, or consecutive
                        if (i == start || i == end || hyphenFound || (i > start && input[i - 1] == '.') || (i + 1 <= end && input[i + 1] == '-')) {
                            return RutValidationResult.Invalid.InvalidCharacter(c, i)
                        }
                    }
                    c.isWhitespace() -> {
                        // Spaces within the body are invalid
                        return RutValidationResult.Invalid.InvalidCharacter(c, i)
                    }
                    else -> {
                        return RutValidationResult.Invalid.InvalidCharacter(c, i)
                    }
                }
            }

            if (hyphenFound && checkDigitChar == null) {
                return RutValidationResult.Invalid.InvalidLength(bodyDigitsCount)
            }

            // If no explicit hyphen was used, the last digit encountered in body is the check digit
            val finalCheckDigit: Char
            val finalNumber: Long

            if (checkDigitChar != null) {
                if (bodyDigitsCount < 1) {
                    return RutValidationResult.Invalid.InvalidLength(1)
                }
                finalCheckDigit = checkDigitChar
                finalNumber = bodyNumber
            } else {
                // Input was unformatted like "123456785" without hyphen
                if (bodyDigitsCount < 2) {
                    return RutValidationResult.Invalid.InvalidLength(bodyDigitsCount)
                }
                val lastDigit = (bodyNumber % 10L).toInt()
                finalCheckDigit = ('0'.code + lastDigit).toChar()
                finalNumber = bodyNumber / 10L
            }

            if (finalNumber < MIN_NUMBER || finalNumber > MAX_NUMBER) {
                return RutValidationResult.Invalid.InvalidNumber(
                    "Numeric value $finalNumber is outside valid range ($MIN_NUMBER..$MAX_NUMBER)"
                )
            }

            val expectedCd = calculateCheckDigit(finalNumber)
            if (finalCheckDigit != expectedCd) {
                return RutValidationResult.Invalid.InvalidCheckDigit(expectedCd, finalCheckDigit)
            }

            return RutValidationResult.Valid(Rut(finalNumber, finalCheckDigit))
        }

        /**
         * Checks whether the given [input] string represents a mathematically valid Chilean RUT.
         *
         * @param input The character sequence to test.
         * @return `true` if the input is valid, `false` otherwise.
         */
        @JvmStatic
        public fun isValid(input: CharSequence?): Boolean = validate(input) is RutValidationResult.Valid

        /**
         * Parses the given [input] into a valid [Rut] instance.
         *
         * @param input The character sequence to parse.
         * @return A valid [Rut] instance.
         * @throws RutParseException If the input is invalid.
         */
        @JvmStatic
        public fun parse(input: CharSequence): Rut {
            return when (val result = validate(input)) {
                is RutValidationResult.Valid -> result.rut
                is RutValidationResult.Invalid -> throw RutParseException(result)
            }
        }

        /**
         * Safely parses the given [input] into a [Rut] instance, returning `null` if invalid.
         *
         * @param input The character sequence to parse.
         * @return A valid [Rut] instance, or `null` if the input is null or invalid.
         */
        @JvmStatic
        public fun parseOrNull(input: CharSequence?): Rut? {
            return when (val result = validate(input)) {
                is RutValidationResult.Valid -> result.rut
                is RutValidationResult.Invalid -> null
            }
        }

        /**
         * Cleans a raw or formatted RUT input by extracting alphanumeric characters
         * and normalizing any check digit `'k'` to uppercase `'K'`.
         *
         * Returns an empty string if [input] is null, blank, or contains no alphanumeric characters.
         *
         * Example: `" 12.345.678-k "` becomes `"12345678K"`.
         *
         * @param input The character sequence to clean.
         * @return Clean unformatted string representation containing only digits and `'K'`.
         */
        @JvmStatic
        public fun clean(input: CharSequence?): String {
            if (input == null) return ""
            val builder = StringBuilder(input.length)
            for (i in 0 until input.length) {
                val c = input[i]
                when {
                    c.isDigit() -> builder.append(c)
                    c == 'k' || c == 'K' -> builder.append('K')
                }
            }
            return builder.toString()
        }

        /**
         * Formats a raw or partial string as the user types into an interactive input field.
         *
         * Automatically inserts thousand dots and a hyphen separator before the check digit.
         * If fewer than 2 characters are present, no hyphen is added.
         *
         * Examples:
         * - `"1"` -> `"1"`
         * - `"12"` -> `"1-2"`
         * - `"12345"` -> `"1.234-5"`
         * - `"123456785"` -> `"12.345.678-5"`
         *
         * @param input The raw or typed characters.
         * @return Real-time formatted string representation.
         */
        @JvmStatic
        public fun formatPartial(input: CharSequence?): String {
            if (input == null) return ""
            val rawCleaned = clean(input)
            val cleaned = if (rawCleaned.length > 10) rawCleaned.substring(0, 10) else rawCleaned
            val len = cleaned.length
            if (len <= 1) return cleaned

            val bodyLen = len - 1
            val checkDigit = cleaned[len - 1]
            val firstGroupLen = if (bodyLen % 3 == 0) 3 else bodyLen % 3
            val dotCount = (bodyLen - 1) / 3
            val builder = StringBuilder(bodyLen + dotCount + 2)

            builder.append(cleaned, 0, firstGroupLen)
            var index = firstGroupLen
            while (index < bodyLen) {
                builder.append('.')
                builder.append(cleaned, index, index + 3)
                index += 3
            }
            builder.append('-').append(checkDigit)
            return builder.toString()
        }

        /**
         * Generates a mathematically valid [Rut] within the specified [range] using [random].
         *
         * @param range The range of valid numbers (defaults to standard RUT range 1_000_000L..99_999_999L).
         * @param random The random instance to draw from.
         * @return A valid [Rut] instance.
         */
        @JvmStatic
        @JvmOverloads
        public fun random(
            range: LongRange = 1_000_000L..99_999_999L,
            random: Random = Random.Default
        ): Rut {
            require(!range.isEmpty() && range.first >= MIN_NUMBER && range.last <= MAX_NUMBER) {
                "Range $range must be non-empty and within $MIN_NUMBER..$MAX_NUMBER"
            }
            val num = random.nextLong(range.first, range.last + 1L)
            return of(num)
        }
    }
}
