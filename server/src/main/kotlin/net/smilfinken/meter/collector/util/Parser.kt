package net.smilfinken.meter.collector.util

import java.sql.Timestamp

internal class Parser {
    companion object {
        private val ID_STRING_MATCHER = """/([a-zA-Z]{3}\d)[^a-zA-Z\d]*(\d+.+)""".toRegex()
        private val TIME_STRING_MATCHER = """(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})""".toRegex()
        private val CRC_MATCHER = """!([A-Fa-f\d]{4})""".toRegex()

        fun parseIdString(value: String): String {
            val matches = (ID_STRING_MATCHER.findAll(value)).first().groupValues
            assert(matches.size == 3)

            return matches.last()
        }

        fun parseDateString(value: String): Timestamp {
            val matches = TIME_STRING_MATCHER.findAll(value).first().groupValues
            assert(matches.size == 7)

            val dateString = "20${matches.take(4).takeLast(3).joinToString("-")}"
            val timeString = matches.takeLast(3).joinToString(":")

            return Timestamp.valueOf("$dateString $timeString")
        }

        fun parseCrcString(value: String): String {
            val matches = CRC_MATCHER.findAll(value).first().groupValues
            assert(matches.size == 2)

            return matches.last()
        }
    }
}