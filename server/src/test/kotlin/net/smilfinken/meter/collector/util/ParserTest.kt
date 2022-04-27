package net.smilfinken.meter.collector.util

import net.smilfinken.meter.collector.TestConstants.Companion.TEST_MESSAGE
import net.smilfinken.meter.collector.TestConstants.Companion.VALID_CRC
import net.smilfinken.meter.collector.TestConstants.Companion.VALID_DATE
import net.smilfinken.meter.collector.TestConstants.Companion.VALID_ID
import net.smilfinken.meter.collector.util.Parser.Companion.parseCrcString
import net.smilfinken.meter.collector.util.Parser.Companion.parseDateString
import net.smilfinken.meter.collector.util.Parser.Companion.parseIdString
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ParserTest {
    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun parseIdString() {
        // given
        val validInput = TEST_MESSAGE.split("\n").first()
        val expectedResult = VALID_ID

        // when
        val actualResult = parseIdString(validInput)

        // then
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun parseDateString() {
        // given
        val validInput = TEST_MESSAGE.split("\n").get(2)
        val expectedResult = VALID_DATE

        // when
        val actualResult = parseDateString(validInput)

        // then
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun parseCrcString() {
        // given
        val validInput = TEST_MESSAGE.split("\n").last()
        val expectedResult = VALID_CRC

        // when
        val actualResult = parseCrcString(validInput)

        // then
        assertEquals(expectedResult, actualResult)
    }
}