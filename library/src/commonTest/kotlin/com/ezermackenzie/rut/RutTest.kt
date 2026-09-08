package com.ezermackenzie.rut

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RutTest {

    @Test
    fun testCalculateCheckDigitKnownValues() {
        assertEquals('9', Rut.calculateCheckDigit(1L))
        assertEquals('K', Rut.calculateCheckDigit(6L))
        assertEquals('0', Rut.calculateCheckDigit(14L))
        assertEquals('5', Rut.calculateCheckDigit(12345678L))
        assertEquals('1', Rut.calculateCheckDigit(11111111L))
        assertEquals('2', Rut.calculateCheckDigit(22222222L))
        assertEquals('K', Rut.calculateCheckDigit(21305614L))
        assertEquals('0', Rut.calculateCheckDigit(21305605L))
        assertEquals('5', Rut.calculateCheckDigit(97004000L))
        assertEquals('5', Rut.calculateCheckDigit(76086428L))
    }

    @Test
    fun testCalculateCheckDigitBoundaries() {
        assertFailsWith<IllegalArgumentException> {
            Rut.calculateCheckDigit(0L)
        }
        assertFailsWith<IllegalArgumentException> {
            Rut.calculateCheckDigit(-1L)
        }
        assertFailsWith<IllegalArgumentException> {
            Rut.calculateCheckDigit(1_000_000_000L)
        }
    }

    @Test
    fun testValidRutParsingDiverseFormats() {
        val testVectors = listOf(
            "12.345.678-5",
            "12345678-5",
            "123456785",
            " 12.345.678-5 ",
            "1-9",
            "19",
            "6-K",
            "6-k",
            "6K",
            "6k",
            "14-0",
            "140",
            "21.305.614-k",
            "21.305.614-K",
            "21305614K",
            "21.305.605-0",
            "97.004.000-5",
            "76.086.428-5"
        )

        for (vector in testVectors) {
            assertTrue(Rut.isValid(vector), "Expected '$vector' to be valid")
            val rut = Rut.parse(vector)
            assertNotNull(rut, "Expected parsing to succeed for '$vector'")
        }
    }

    @Test
    fun testNormalizationOfCheckDigit() {
        val rutLower = Rut.parse("21.305.614-k")
        val rutUpper = Rut.parse("21.305.614-K")

        assertEquals('K', rutLower.checkDigit)
        assertEquals('K', rutUpper.checkDigit)
        assertEquals(rutLower, rutUpper)
        assertEquals(rutLower.hashCode(), rutUpper.hashCode())
    }

    @Test
    fun testFormattingStyles() {
        val rut = Rut.parse("12.345.678-5")

        assertEquals("12.345.678-5", rut.format(RutFormatStyle.DOTS_AND_HYPHEN))
        assertEquals("12.345.678-5", rut.formatted)

        assertEquals("12345678-5", rut.format(RutFormatStyle.HYPHEN_ONLY))
        assertEquals("12345678-5", rut.canonical)

        assertEquals("123456785", rut.format(RutFormatStyle.UNFORMATTED))
        assertEquals("123456785", rut.unformatted)

        assertEquals("12.345.678-5", rut.toString())
    }

    @Test
    fun testFormattingShortNumbers() {
        val rutSingleDigit = Rut.parse("1-9")
        assertEquals("1-9", rutSingleDigit.formatted)
        assertEquals("1-9", rutSingleDigit.canonical)
        assertEquals("19", rutSingleDigit.unformatted)

        val rutFourDigits = Rut.parse("1234-3")
        assertEquals("1.234-3", rutFourDigits.formatted)
        assertEquals("1234-3", rutFourDigits.canonical)
        assertEquals("12343", rutFourDigits.unformatted)
    }

    @Test
    fun testInvalidCheckDigitFails() {
        val result = Rut.validate("12.345.678-9")
        assertFalse(result.isValid)
        assertIs<RutValidationResult.Invalid.InvalidCheckDigit>(result)

        assertEquals('5', result.expected)
        assertEquals('9', result.actual)

        val exception = assertFailsWith<RutParseException> {
            Rut.parse("12.345.678-9")
        }
        assertIs<RutValidationResult.Invalid.InvalidCheckDigit>(exception.error)
    }

    @Test
    fun testInvalidCharactersFail() {
        val invalidChars = listOf(
            "12.34a.678-5",
            "12.345.678-X",
            "12.345.678#5",
            "12 345 678-5",
            "12345678-",
            "12..345.678-5",
            "12345678.-5",
            "12-345-678-5",
            "-123456785"
        )

        for (input in invalidChars) {
            assertFalse(Rut.isValid(input), "Expected '$input' to be invalid")
            val result = Rut.validate(input)
            assertTrue(result is RutValidationResult.Invalid, "Expected Invalid result for '$input'")
        }
    }

    @Test
    fun testEmptyAndWhitespaceInputFails() {
        val emptyInputs = listOf(null, "", "   ", "\t\n")

        for (input in emptyInputs) {
            assertFalse(Rut.isValid(input), "Expected '$input' to be invalid")
            val result = Rut.validate(input)
            assertIs<RutValidationResult.Invalid.EmptyInput>(result)
            assertNull(Rut.parseOrNull(input))
        }
    }

    @Test
    fun testInvalidLengthFails() {
        val shortInputs = listOf("1")
        for (input in shortInputs) {
            val result = Rut.validate(input)
            assertIs<RutValidationResult.Invalid.InvalidLength>(result)
        }
    }

    @Test
    fun testFactoryOfMethods() {
        val rutFromNumber = Rut.of(12345678L)
        assertEquals(12345678L, rutFromNumber.number)
        assertEquals('5', rutFromNumber.checkDigit)

        val rutWithDv = Rut.of(12345678L, '5')
        assertEquals(rutFromNumber, rutWithDv)

        val rutWithLowerDv = Rut.of(6L, 'k')
        assertEquals('K', rutWithLowerDv.checkDigit)

        assertFailsWith<RutParseException> {
            Rut.of(12345678L, '9')
        }
    }

    @Test
    fun testComparableAndSorting() {
        val r1 = Rut.parse("1-9")
        val r2 = Rut.parse("14-0")
        val r3 = Rut.parse("12.345.678-5")

        val list = listOf(r3, r1, r2)
        val sorted = list.sorted()

        assertEquals(listOf(r1, r2, r3), sorted)
        assertTrue(r1 < r2)
        assertTrue(r2 < r3)
    }

    @Test
    fun testExtensionFunctions() {
        val rutStr = "12.345.678-5"

        assertTrue(rutStr.isValidRut())
        assertEquals(12345678L, rutStr.toRut().number)
        assertEquals('5', rutStr.toRut().checkDigit)
        assertEquals("12345678-5", rutStr.formatRut(RutFormatStyle.HYPHEN_ONLY))

        val nullStr: String? = null
        assertFalse(nullStr.isValidRut())
        assertNull(nullStr.toRutOrNull())

        val invalidStr = "invalid"
        assertFalse(invalidStr.isValidRut())
        assertNull(invalidStr.toRutOrNull())

        val fromLong = 12345678L.toRut()
        assertEquals("12.345.678-5", fromLong.formatted)

        val fromInt = 12345678.toRut()
        assertEquals("12.345.678-5", fromInt.formatted)
    }

    @Test
    fun testDestructuringDeclarations() {
        val rut = Rut.parse("12.345.678-5")
        val (number, checkDigit) = rut

        assertEquals(12345678L, number)
        assertEquals('5', checkDigit)
    }

    @Test
    fun testCleanRut() {
        assertEquals("123456785", Rut.clean(" 12.345.678-5 "))
        assertEquals("21305614K", Rut.clean("21.305.614-k"))
        assertEquals("21305614K", "21.305.614-k".cleanRut())
        assertEquals("", (null as String?).cleanRut())
        assertEquals("", "".cleanRut())
    }

    @Test
    fun testFormatPartialRutLiveTyping() {
        assertEquals("", Rut.formatPartial(null))
        assertEquals("", Rut.formatPartial(""))
        assertEquals("1", Rut.formatPartial("1"))
        assertEquals("1-2", Rut.formatPartial("12"))
        assertEquals("12-3", Rut.formatPartial("123"))
        assertEquals("123-4", Rut.formatPartial("1234"))
        assertEquals("1.234-5", Rut.formatPartial("12345"))
        assertEquals("12.345-6", Rut.formatPartial("123456"))
        assertEquals("123.456-7", Rut.formatPartial("1234567"))
        assertEquals("1.234.567-8", Rut.formatPartial("12345678"))
        assertEquals("12.345.678-5", Rut.formatPartial("123456785"))
        assertEquals("12.345.678-5", "123456785".formatPartialRut())
        assertEquals("21.305.614-K", "21305614k".formatPartialRut())
        assertEquals("123.456.789-0", Rut.formatPartial("12345678909999"))
    }

    @Test
    fun testRandomRutGeneration() {
        for (i in 1..50) {
            val randomRut = Rut.random()
            assertTrue(randomRut.number in 1_000_000L..99_999_999L)
            assertTrue(Rut.isValid(randomRut.formatted))
            assertEquals(randomRut, Rut.parse(randomRut.formatted))
        }

        val customRangeRut = Rut.random(range = 100L..999L)
        assertTrue(customRangeRut.number in 100L..999L)
        assertTrue(Rut.isValid(customRangeRut.formatted))
    }
}
