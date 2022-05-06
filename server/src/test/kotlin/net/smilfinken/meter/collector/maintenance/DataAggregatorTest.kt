package net.smilfinken.meter.collector.maintenance

import net.smilfinken.meter.collector.TestConfiguration
import net.smilfinken.meter.collector.model.DataItem
import net.smilfinken.meter.collector.model.DataReport
import net.smilfinken.meter.collector.model.HourlyData
import net.smilfinken.meter.collector.model.PowerOutput
import net.smilfinken.meter.collector.model.Temperature
import net.smilfinken.meter.collector.persistence.DataItemRepository
import net.smilfinken.meter.collector.persistence.DataReportRepository
import net.smilfinken.meter.collector.persistence.HourlyDataRepository
import net.smilfinken.meter.collector.persistence.PowerOutputRepository
import net.smilfinken.meter.collector.persistence.TemperatureRepository
import net.smilfinken.meter.collector.util.Dater.Companion.LOCAL_TIME_ZONE
import net.smilfinken.meter.collector.util.Dater.Companion.firstMinuteOfHour
import net.smilfinken.meter.collector.util.Dater.Companion.nowDate
import org.apache.commons.lang3.time.DateUtils
import org.apache.commons.lang3.time.DateUtils.addHours
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.SpringBootTest
import java.time.ZonedDateTime
import java.util.Calendar.HOUR
import java.util.Date

@SpringBootTest(classes = [TestConfiguration::class])
internal class DataAggregatorTest {
    @Mock
    lateinit var mockedDataReportRepository: DataReportRepository

    @Mock
    lateinit var mockedDataItemRepository: DataItemRepository

    @Mock
    lateinit var mockedPowerOutputRepository: PowerOutputRepository

    @Mock
    lateinit var mockedTemperatureRepository: TemperatureRepository

    @Mock
    lateinit var mockedHourlyDataRepository: HourlyDataRepository

    @InjectMocks
    lateinit var dataAggregator: DataAggregator

    @AfterEach
    fun tearDown() {
        reset(mockedDataReportRepository)
        reset(mockedDataItemRepository)
        reset(mockedPowerOutputRepository)
        reset(mockedTemperatureRepository)
        reset(mockedHourlyDataRepository)
    }

    @Captor
    lateinit var actualHourlyData: ArgumentCaptor<HourlyData>

    @Test
    fun aggregateHourlyDataWithNoDataShouldNotCallAndNotStore() {
        // given
        whenever(mockedHourlyDataRepository.findTopByOrderByTimestampDesc()).thenReturn(null)

        // when
        dataAggregator.aggregateHourlyData()

        // then
        verify(mockedHourlyDataRepository, times(1)).findTopByOrderByTimestampDesc()
        verify(mockedDataReportRepository, times(1)).findAllByReceivedTimestampBetween(any(), any())
        verify(mockedDataItemRepository, times(0)).findByReportAndObis(any(), any())
        verify(mockedPowerOutputRepository, times(0)).findByReport(any())
        verify(mockedTemperatureRepository, times(0)).findByReportAndSource(any(), any())
        verify(mockedHourlyDataRepository, times(0)).save(any())
    }

    @Test
    fun aggregateHourlyDataShouldUseCorrectDatesForRequest() {
        // given
        val startDate = nowDate()
        val latestHourlyData = HourlyData(0, startDate, 0F, 0F, 0F, 0F, 0F)
        val endDate = firstMinuteOfHour()

        whenever(mockedHourlyDataRepository.findTopByOrderByTimestampDesc()).thenReturn(latestHourlyData)

        // when
        dataAggregator.aggregateHourlyData()

        // then
        verify(mockedDataReportRepository, times(1)).findAllByReceivedTimestampBetween(
            eq(addHours(startDate, 1)),
            eq(endDate)
        )
    }

    @Test
    fun aggregateHourlyDataWithSingleUnprocessedDataShouldStore() {
        // given
        val now = ZonedDateTime.now(LOCAL_TIME_ZONE)

        val latestHourlyData =
            HourlyData(0, Date.from(now.minusDays(3).toInstant()), 0F, 0F, 0F, 0F, 0F)

        val reportTimestamp = Date.from(now.minusHours(1).toInstant())
        val newDataReport = DataReport(0, reportTimestamp, reportTimestamp)

        val mockedDataValue = 7F
        val mockedPowerValue = 42F

        val expectedTimestamp = DateUtils.truncate(reportTimestamp, HOUR)
        val expectedTemperatureValue = 66.6F
        val expectedHourlyData = HourlyData(
            0,
            expectedTimestamp,
            mockedDataValue,
            mockedDataValue,
            mockedPowerValue,
            expectedTemperatureValue,
            expectedTemperatureValue
        )

        val mockedDataItem = DataItem(0, newDataReport, "", mockedDataValue, "")
        val mockedPowerOutput = PowerOutput(0, newDataReport, mockedPowerValue)
        val mockedTemperature = Temperature(0, newDataReport, expectedTemperatureValue, "")

        whenever(mockedHourlyDataRepository.findTopByOrderByTimestampDesc()).thenReturn(latestHourlyData)
        whenever(
            mockedDataReportRepository
                .findAllByReceivedTimestampBetween(eq(addHours(latestHourlyData.timestamp, 1)), eq(firstMinuteOfHour()))
        ).thenReturn(listOf(newDataReport))
        whenever(mockedDataItemRepository.findByReportAndObis(any(), anyString())).thenReturn(mockedDataItem)
        whenever(mockedPowerOutputRepository.findByReport(any())).thenReturn(mockedPowerOutput)
        whenever(mockedTemperatureRepository.findByReportAndSource(any(), anyString())).thenReturn(mockedTemperature)

        // when
        dataAggregator.aggregateHourlyData()

        // then
        val expectedDataValue = mockedDataValue * 1000
        val expectedPowerValue = mockedPowerValue

        verify(mockedHourlyDataRepository, times(1)).save(actualHourlyData.capture())
        assertEquals(expectedTimestamp, actualHourlyData.value.timestamp)
        assertEquals(expectedDataValue, actualHourlyData.value.output)
        assertEquals(expectedDataValue, actualHourlyData.value.intake)
        assertEquals(expectedPowerValue, actualHourlyData.value.produced)
        assertEquals(expectedTemperatureValue, actualHourlyData.value.indoorTemp)
        assertEquals(expectedTemperatureValue, actualHourlyData.value.outdoorTemp)
    }

    @Test
    fun aggregateHourlyDataWithMultipleItemsShouldStoreCorrectAmount() {
        // given
        val now = ZonedDateTime.now(LOCAL_TIME_ZONE)

        val latestHourlyData =
            HourlyData(0, Date.from(now.minusDays(3).toInstant()), 0F, 0F, 0F, 0F, 0F)

        val reportTimestamps = listOf(
            now.minusHours(8),
            now.minusHours(3),
            now.minusHours(3),
            now.minusHours(2),
            now.minusHours(2),
            now.minusHours(2),
            now.minusHours(1),
        )
        val newDataReports = reportTimestamps.map {
            val timestamp = Date.from(it.toInstant())
            DataReport(0, timestamp, timestamp)
        }
        val expectedHourlyDataCount = newDataReports.distinctBy { dataReport -> dataReport.receivedTimestamp }.size

        val mockedDataItem = DataItem(0, DataReport(), "", 0F, "")
        val mockedPowerOutput = PowerOutput(0, DataReport(), 0F)
        val mockedTemperature = Temperature(0, DataReport(), 0F, "")

        whenever(mockedHourlyDataRepository.findTopByOrderByTimestampDesc()).thenReturn(latestHourlyData)
        whenever(
            mockedDataReportRepository
                .findAllByReceivedTimestampBetween(eq(addHours(latestHourlyData.timestamp, 1)), eq(firstMinuteOfHour()))
        ).thenReturn(newDataReports)
        whenever(mockedDataItemRepository.findByReportAndObis(any(), anyString())).thenReturn(mockedDataItem)
        whenever(mockedPowerOutputRepository.findByReport(any())).thenReturn(mockedPowerOutput)
        whenever(mockedTemperatureRepository.findByReportAndSource(any(), anyString())).thenReturn(mockedTemperature)

        // when
        dataAggregator.aggregateHourlyData()

        // then
        verify(mockedHourlyDataRepository, times(expectedHourlyDataCount)).save(any())
    }

    @Test
    fun aggregateHourlyDataReturnsCorrectAverages() {
        // given
        val now = ZonedDateTime.now(LOCAL_TIME_ZONE)

        val latestHourlyData =
            HourlyData(0, Date.from(now.minusDays(3).toInstant()), 0F, 0F, 0F, 0F, 0F)

        val reportTimestamps = listOf(
            now.minusHours(8),
            now.minusHours(8),
            now.minusHours(8),
            now.minusHours(8),
            now.minusHours(3),
            now.minusHours(3),
            now.minusHours(3),
            now.minusHours(2),
            now.minusHours(2),
            now.minusHours(1),
        )
        val newDataReports = reportTimestamps.map {
            val timestamp = Date.from(it.toInstant())
            DataReport(0, timestamp, timestamp)
        }

        val mockedDataValue = 5F
        val mockedPowerValue = 8F
        val mockedTemperatureValue = 10F
        val mockedDataItem = DataItem(0, DataReport(), "", mockedDataValue, "")
        val mockedPowerOutput = PowerOutput(0, DataReport(), mockedPowerValue)
        val mockedTemperature = Temperature(0, DataReport(), mockedTemperatureValue, "")

        whenever(mockedHourlyDataRepository.findTopByOrderByTimestampDesc()).thenReturn(latestHourlyData)
        whenever(
            mockedDataReportRepository
                .findAllByReceivedTimestampBetween(eq(addHours(latestHourlyData.timestamp, 1)), eq(firstMinuteOfHour()))
        ).thenReturn(newDataReports)
        whenever(mockedDataItemRepository.findByReportAndObis(any(), anyString())).thenReturn(mockedDataItem)
        whenever(mockedPowerOutputRepository.findByReport(any())).thenReturn(mockedPowerOutput)
        whenever(mockedTemperatureRepository.findByReportAndSource(any(), anyString())).thenReturn(mockedTemperature)

        // when
        dataAggregator.aggregateHourlyData()

        // then
        val expectedHourlyDataCount = newDataReports.distinctBy { dataReport -> dataReport.receivedTimestamp }.size
        verify(mockedHourlyDataRepository, times(expectedHourlyDataCount)).save(actualHourlyData.capture())
        assertEquals(expectedHourlyDataCount, actualHourlyData.allValues.size)

        val expectedDataValue = mockedDataValue * 1000
        val expectedPowerValue = mockedPowerValue
        val expectedTemperatureValue = mockedTemperatureValue
        actualHourlyData.allValues.forEach { actualResult ->
            assertEquals(expectedDataValue, actualResult.intake)
            assertEquals(expectedDataValue, actualResult.output)
            assertEquals(expectedPowerValue, actualResult.produced)
            assertEquals(expectedTemperatureValue, actualResult.outdoorTemp)
            assertEquals(expectedTemperatureValue, actualResult.indoorTemp)
        }
    }
}
