package net.smilfinken.meter.collector.exceptions

class CrcVerificationException(private val expectedCrc: String, private val calculatedCrc: String) :
    Exception("Calculated checksum $calculatedCrc does not match expected checksum $expectedCrc") {
    fun getExpectedCrc() = expectedCrc
    fun getCalculatedCrc() = calculatedCrc
}