package com.ezermackenzie.rut

/**
 * Parses this character sequence into a validated [Rut].
 *
 * @receiver A string or character sequence representing a Chilean RUT.
 * @return A valid [Rut] instance.
 * @throws RutParseException If the input is invalid or improperly formatted.
 */
public fun CharSequence.toRut(): Rut = Rut.parse(this)

/**
 * Safely parses this character sequence into a [Rut], or returns `null` if invalid.
 *
 * @receiver A nullable character sequence representing a Chilean RUT.
 * @return A valid [Rut] instance, or `null` if the input is null, empty, or invalid.
 */
public fun CharSequence?.toRutOrNull(): Rut? = Rut.parseOrNull(this)

/**
 * Checks whether this character sequence is a mathematically valid Chilean RUT.
 *
 * @receiver A nullable character sequence to validate.
 * @return `true` if valid, `false` otherwise.
 */
public fun CharSequence?.isValidRut(): Boolean = Rut.isValid(this)

/**
 * Formats this character sequence representing a Chilean RUT into the requested [style].
 *
 * @receiver A valid Chilean RUT character sequence.
 * @param style The desired formatting style. Defaults to [RutFormatStyle.DOTS_AND_HYPHEN].
 * @return The formatted RUT string representation.
 * @throws RutParseException If the input is not a valid Chilean RUT.
 */
public fun CharSequence.formatRut(style: RutFormatStyle = RutFormatStyle.DOTS_AND_HYPHEN): String =
    Rut.parse(this).format(style)

/**
 * Cleans this character sequence by removing all formatting characters and normalizing
 * check digit `'k'` to uppercase `'K'`.
 *
 * @receiver A nullable character sequence to clean.
 * @return Clean unformatted string representation containing only digits and `'K'`.
 */
public fun CharSequence?.cleanRut(): String = Rut.clean(this)

/**
 * Formats this character sequence in real time as the user types into an interactive field.
 *
 * Automatically places thousand dots and hyphen separator before the check digit.
 *
 * @receiver A nullable character sequence to format partially.
 * @return Formatted partial string representation.
 */
public fun CharSequence?.formatPartialRut(): String = Rut.formatPartial(this)

/**
 * Creates a [Rut] instance from this numeric [Long], automatically calculating its check digit.
 *
 * @receiver The numeric body of the RUT.
 * @return A valid [Rut] instance.
 * @throws IllegalArgumentException If this number is outside valid RUT boundaries.
 */
public fun Long.toRut(): Rut = Rut.of(this)

/**
 * Creates a [Rut] instance from this numeric [Int], automatically calculating its check digit.
 *
 * @receiver The numeric body of the RUT.
 * @return A valid [Rut] instance.
 * @throws IllegalArgumentException If this number is outside valid RUT boundaries.
 */
public fun Int.toRut(): Rut = Rut.of(this.toLong())
