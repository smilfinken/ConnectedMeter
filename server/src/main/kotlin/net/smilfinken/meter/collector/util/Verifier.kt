package net.smilfinken.meter.collector.util

import com.github.snksoft.crc.CRC
import com.github.snksoft.crc.CRC.Parameters
import net.smilfinken.meter.collector.exceptions.CrcVerificationException
import org.slf4j.LoggerFactory

class Verifier {
    companion object {
        val LOGGER = LoggerFactory.getLogger(Verifier::class.java)!!

        fun verifyData(data: String) {
            LOGGER.trace("Verifying checksum")

            val index = data.indexOf("!")
            val crcData = data.take(index + 1)
            val expectedCrc = Parser.parseCrcString(data.takeLast(index))
            val calculatedCrc = "%04x".format(CRC.calculateCRC(Parameters.CRC16, crcData.toByteArray()))

            LOGGER.debug("expected crc:   $expectedCrc")
            LOGGER.debug("calculated crc: $calculatedCrc")

            if (expectedCrc != calculatedCrc) {
                throw CrcVerificationException(
                    expectedCrc,
                    calculatedCrc
                )
            }
        }
    }
}