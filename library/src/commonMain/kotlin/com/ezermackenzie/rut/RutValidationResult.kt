package com.ezermackenzie.rut

/**
 * Represents the outcome of a Chilean RUT validation check.
 */
public sealed interface RutValidationResult {

    /**
     * Indicates whether the validation succeeded.
     */
    public val isValid: Boolean
        get() = this is Valid

    /**
     * Successful validation result containing a valid [Rut] instance.
     *
     * @property rut The validated [Rut] instance.
     */
    public data class Valid(
        public val rut: Rut
    ) : RutValidationResult

    /**
     * Sealed interface representing any validation failure.
     */
    public sealed interface Invalid : RutValidationResult {
        /**
         * Human-readable diagnostic description of the failure reason.
         */
        public val message: String

        /**
         * Indicates that the provided input was null, empty, or contained only whitespace characters.
         */
        public data object EmptyInput : Invalid {
            override val message: String = "RUT input cannot be null, empty, or whitespace only."
        }

        /**
         * Indicates that the RUT length (digits plus check digit) is out of the valid range (2 to 10 characters).
         *
         * @property actualLength The number of alphanumeric characters found in the input.
         */
        public data class InvalidLength(
            public val actualLength: Int
        ) : Invalid {
            override val message: String =
                "RUT alphanumeric length must be between 2 and 10 characters, but found $actualLength."
        }

        /**
         * Indicates that an unexpected or illegal character was encountered during parsing.
         *
         * @property char The illegal character.
         * @property index The zero-based index where the character was found in the input.
         */
        public data class InvalidCharacter(
            public val char: Char,
            public val index: Int
        ) : Invalid {
            override val message: String =
                "Illegal character '$char' at index $index. Only digits, '.', '-', spaces, and 'K'/'k' are allowed."
        }

        /**
         * Indicates that the calculated check digit (Dígito Verificador) does not match the provided one.
         *
         * @property expected The mathematically correct verification character calculated using Modulo 11.
         * @property actual The verification character provided in the input.
         */
        public data class InvalidCheckDigit(
            public val expected: Char,
            public val actual: Char
        ) : Invalid {
            override val message: String =
                "Check digit mismatch: expected '$expected' but received '$actual'."
        }

        /**
         * Indicates that the numeric body of the RUT is invalid (e.g., non-positive or out of range).
         *
         * @property reason Explanation of the numeric invalidity.
         */
        public data class InvalidNumber(
            public val reason: String
        ) : Invalid {
            override val message: String = "Invalid RUT numeric body: $reason."
        }
    }
}
