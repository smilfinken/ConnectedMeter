package net.smilfinken.meter.collector.util

import java.sql.Timestamp

class Parser {
    companion object {
        private val IDSTRING_MATCHER = """/([a-zA-Z]{3}\d)[^a-zA-Z0-9]*(\d+.+)""".toRegex()
        private val TIMESTRING_MATCHER = """(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})""".toRegex()
        private val CRC_MATCHER = """!([A-Fa-f0-9]{4})""".toRegex()

        fun parseIdString(value: String): String {
            val matches = (IDSTRING_MATCHER.findAll(value)).first().groupValues
            assert(matches.size == 3)

            return matches.last()
        }

        fun parseDateString(value: String): Timestamp {
            val matches = TIMESTRING_MATCHER.findAll(value).first().groupValues
            assert(matches.size == 7)

            val dateString = "20%s".format(matches.take(4).takeLast(3).joinToString("-"))
            val timeString = matches.takeLast(3).joinToString(":")

            return Timestamp.valueOf("%s %s".format(dateString, timeString))
        }

        fun parseCrcString(value: String): String {
            val matches = CRC_MATCHER.findAll(value).first().groupValues
            assert(matches.size == 2)

            return matches.last()
        }
    }
}