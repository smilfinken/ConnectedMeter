package net.smilfinken.meter.collector.util

import org.apache.commons.lang3.time.DateUtils
import org.apache.commons.lang3.time.DateUtils.truncate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.HOUR
import java.util.Calendar.HOUR_OF_DAY
import java.util.Date

class Dater {
    companion object {
        internal val LOCAL_TIME_ZONE = ZoneId.of("Europe/Stockholm")

        fun nowDate(): Date =
            Date.from(ZonedDateTime.now(LOCAL_TIME_ZONE).toInstant())

        fun minDate(): Date =
            Date.from(ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, LOCAL_TIME_ZONE).toInstant())

        fun firstMinuteOfHour(
            date: Date = Date.from(
                ZonedDateTime.of(LocalDateTime.now(), LOCAL_TIME_ZONE).toInstant()
            )
        ): Date = truncate(date, HOUR)

        fun firstHourOfDay(
            date: Date = Date.from(
                ZonedDateTime.of(LocalDateTime.now(), LOCAL_TIME_ZONE).toInstant()
            )
        ): Date = truncate(date, DAY_OF_MONTH)

        fun isSameHour(a: Date, b: Date): Boolean {
            return DateUtils.truncate(a, HOUR) == DateUtils.truncate(b, HOUR)
        }

        fun getHour(date: Date): Int {
            val calendar = Calendar.getInstance()
            calendar.time = date
            return calendar.get(HOUR_OF_DAY)
        }

        fun getDay(date: Date): Int {
            val calendar = Calendar.getInstance()
            calendar.time = date
            return calendar.get(DAY_OF_MONTH)
        }
    }
}