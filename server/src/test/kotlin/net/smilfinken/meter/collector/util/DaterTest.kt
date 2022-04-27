package net.smilfinken.meter.collector.util

import net.smilfinken.meter.collector.util.Dater.Companion.LOCAL_TIME_ZONE
import net.smilfinken.meter.collector.util.Dater.Companion.firstHourOfDay
import net.smilfinken.meter.collector.util.Dater.Companion.firstMinuteOfHour
import net.smilfinken.meter.collector.util.Dater.Companion.getDay
import net.smilfinken.meter.collector.util.Dater.Companion.getHour
import net.smilfinken.meter.collector.util.Dater.Companion.isSameHour
import net.smilfinken.meter.collector.util.Dater.Companion.minDate
import net.smilfinken.meter.collector.util.Dater.Companion.nowDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.Date

// pretty doubtful value, but TEST ALL THE THINGS!
internal class DaterTest {
    @Test
    fun nowDateReturnsCorrectValue() {
        // given
        val expectedResult = ZonedDateTime.of(LocalDateTime.now(), LOCAL_TIME_ZONE)

        // when
        val actualResult = nowDate().toInstant()

        // then
        assertEquals(
            expectedResult.truncatedTo(ChronoUnit.SECONDS).toInstant(),
            actualResult.minusMillis(actualResult.get(ChronoField.MILLI_OF_SECOND).toLong())
        )
    }

    @Test
    fun minDateReturnsCorrectValue() {
        // given
        val expectedResult = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, LOCAL_TIME_ZONE)

        // then
        assertEquals(expectedResult.toInstant(), minDate().toInstant())
    }

    @Test
    fun firstMinuteOfCurrentHourReturnsCorrectValue() {
        // given
        val now = LocalDateTime.now()
        val expectedResult =
            ZonedDateTime.of(now.year, now.monthValue, now.dayOfMonth, now.hour, 0, 0, 0, LOCAL_TIME_ZONE)

        // then
        assertEquals(expectedResult.toInstant(), firstMinuteOfHour().toInstant())
    }

    @Test
    fun firstHourOfCurrentDayReturnsCorrectValue() {
        // given
        val now = LocalDateTime.now()
        val expectedResult = ZonedDateTime.of(now.year, now.monthValue, now.dayOfMonth, 0, 0, 0, 0, LOCAL_TIME_ZONE)

        // then
        assertEquals(expectedResult.toInstant(), firstHourOfDay().toInstant())
    }

    @Test
    fun isSameHourReturnsTrueWithinSameHour() {
        // given
        val date1 = Date.from(ZonedDateTime.of(2022, 1, 1, 23, 1, 0, 0, LOCAL_TIME_ZONE).toInstant())
        val date2 = Date.from(ZonedDateTime.of(2022, 1, 1, 23, 59, 0, 0, LOCAL_TIME_ZONE).toInstant())

        // when
        val actualResult = isSameHour(date1, date2)

        // then
        assertTrue(actualResult)
    }

    @Test
    fun isSameHourReturnsFalseNextDay() {
        // given
        val date1 = Date.from(ZonedDateTime.of(2022, 1, 1, 23, 59, 0, 0, LOCAL_TIME_ZONE).toInstant())
        val date2 = Date.from(ZonedDateTime.of(2022, 1, 2, 0, 1, 0, 0, LOCAL_TIME_ZONE).toInstant())

        // when
        val actualResult = isSameHour(date1, date2)

        // then
        assertFalse(actualResult)
    }

    @Test
    fun isSameHourReturnsFalseNextMonth() {
        // given
        val date1 = Date.from(ZonedDateTime.of(2022, 1, 31, 23, 59, 0, 0, LOCAL_TIME_ZONE).toInstant())
        val date2 = Date.from(ZonedDateTime.of(2022, 2, 1, 0, 1, 0, 0, LOCAL_TIME_ZONE).toInstant())

        // when
        val actualResult = isSameHour(date1, date2)

        // then
        assertFalse(actualResult)
    }

    @Test
    fun isSameHourReturnsFalseNextYear() {
        // given
        val date1 = Date.from(ZonedDateTime.of(2022, 12, 31, 23, 59, 0, 0, LOCAL_TIME_ZONE).toInstant())
        val date2 = Date.from(ZonedDateTime.of(2023, 1, 1, 0, 1, 0, 0, LOCAL_TIME_ZONE).toInstant())

        // when
        val actualResult = isSameHour(date1, date2)

        // then
        assertFalse(actualResult)
    }

    @Test
    fun getHourReturnsCorrectValue() {
        // given
        val expectedResult = 13
        val date = Date.from(ZonedDateTime.of(2022, 1, 1, expectedResult, 0, 0, 0, LOCAL_TIME_ZONE).toInstant())

        // when
        val actualResult = getHour(date)

        // then
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun getDayReturnsCorrectValue() {
        // given
        val expectedResult = 13
        val date = Date.from(ZonedDateTime.of(2022, 1, expectedResult, 1, 0, 0, 0, LOCAL_TIME_ZONE).toInstant())

        // when
        val actualResult = getDay(date)

        // then
        assertEquals(expectedResult, actualResult)
    }
}