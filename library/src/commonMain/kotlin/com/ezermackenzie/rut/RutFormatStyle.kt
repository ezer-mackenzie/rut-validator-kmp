package com.ezermackenzie.rut

/**
 * Defines the formatting styles for Chilean RUT (Rol Único Tributario) representations.
 */
public enum class RutFormatStyle {
    /**
     * Standard formal presentation with thousand dots and hyphen separator.
     * Example: `"12.345.678-5"`
     */
    DOTS_AND_HYPHEN,

    /**
     * Canonical representation with hyphen separator and no dots.
     * Example: `"12345678-5"`
     */
    HYPHEN_ONLY,

    /**
     * Raw compact representation without dots or hyphens.
     * Example: `"123456785"`
     */
    UNFORMATTED
}
