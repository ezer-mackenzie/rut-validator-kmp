package com.ezermackenzie.rut

/**
 * Exception thrown when parsing a Chilean RUT string representation fails.
 *
 * @property error The detailed [RutValidationResult.Invalid] cause describing why parsing failed.
 */
public class RutParseException(
    public val error: RutValidationResult.Invalid,
    cause: Throwable? = null
) : IllegalArgumentException(error.message, cause)
