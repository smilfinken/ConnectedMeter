package net.smilfinken.meter.collector.tools

import com.github.snksoft.crc.CRC
import com.github.snksoft.crc.CRC.Parameters
import net.smilfinken.meter.collector.TestConstants.Companion.TEST_MESSAGE_CONTENT
import net.smilfinken.meter.collector.TestConstants.Companion.TEST_MESSAGE_DATE
import net.smilfinken.meter.collector.TestConstants.Companion.TEST_MESSAGE_ID
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ExternalToolsTest {
    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    private fun calculateCrc(value: String) = CRC.calculateCRC(Parameters.CRC16, value.toByteArray())

    @Test
    fun crc16() {
        assertEquals("ea4a", "%04x".format(calculateCrc("bork")))

        assertEquals("3900", "%04x".format(calculateCrc("""\""")))
        assertEquals("3900", "%04x".format(calculateCrc("\\")))
        assertEquals("9784", "%04x".format(calculateCrc("\r\n")))

        assertEquals("5ccc", "%04x".format(calculateCrc(TEST_MESSAGE_ID)))
        assertEquals("c3d4", "%04x".format(calculateCrc(TEST_MESSAGE_DATE)))

        assertEquals("7945", "%04x".format(calculateCrc(TEST_MESSAGE_CONTENT)))
    }
}