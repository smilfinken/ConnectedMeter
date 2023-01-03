package net.smilfinken.meter.collector.controllers

import com.google.gson.Gson
import net.smilfinken.meter.collector.model.DailyData
import net.smilfinken.meter.collector.model.EnergyChartDataItem
import net.smilfinken.meter.collector.model.HourlyData
import net.smilfinken.meter.collector.model.MonthlyData
import net.smilfinken.meter.collector.persistence.DailyDataRepository
import net.smilfinken.meter.collector.persistence.HourlyDataRepository
import net.smilfinken.meter.collector.persistence.MonthlyDataRepository
import net.smilfinken.meter.collector.util.Dater.Companion.nowDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus.OK

@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class ApiControllerTest(
    @Autowired private val restTemplate: TestRestTemplate,
    @Autowired private val hourlyDataRepository: HourlyDataRepository,
    @Autowired private val dailyDataRepository: DailyDataRepository,
    @Autowired private val monthlyDataRepository: MonthlyDataRepository
) {
    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun hourlyDataReturnsHttpOk() {
        // when
        val entity = restTemplate.getForEntity("/meter/api/hourlyData", String::class.java)

        // then
        assertEquals(OK, entity.statusCode)
    }

    @Test
    fun hourlyDataReturnsData() {
        // given
        val expectedTimestamp = nowDate()
        val expectedProduction = 9000F
        val expectedIndoorTemperature = 17F
        val expectedOutdoorTemperature = 66.6F

        val hourlyData = HourlyData(
            0,
            expectedTimestamp,
            0F,
            0F,
            expectedProduction,
            expectedOutdoorTemperature,
            expectedIndoorTemperature
        )
        hourlyDataRepository.save(hourlyData)

        // when
        val entity = restTemplate.getForEntity("/meter/api/hourlyData", String::class.java)

        // then
        val energyChartDataItem = Gson().fromJson(entity.body, Array<EnergyChartDataItem>::class.java).firstOrNull()
        assertNotNull(energyChartDataItem)
        assertEquals(expectedTimestamp, energyChartDataItem?.timestamp)
        assertEquals(expectedIndoorTemperature, energyChartDataItem?.indoorTemperature)
        assertEquals(expectedOutdoorTemperature, energyChartDataItem?.outdoorTemperature)
        assertEquals(expectedProduction, energyChartDataItem?.production)
    }

    @Test
    fun dailyDataReturnsHttpOk() {
        // when
        val entity = restTemplate.getForEntity("/meter/api/dailyData", String::class.java)

        // then
        assertEquals(OK, entity.statusCode)
    }

    @Test
    fun dailyDataReturnsData() {
        // given
        val expectedTimestamp = nowDate()
        val expectedProduction = 9000F
        val expectedIndoorTemperature = 17F
        val expectedOutdoorTemperature = 66.6F

        val dailyData = DailyData(
            0,
            expectedTimestamp,
            0F,
            0F,
            expectedProduction,
            expectedOutdoorTemperature,
            expectedIndoorTemperature
        )
        dailyDataRepository.save(dailyData)

        // when
        val entity = restTemplate.getForEntity("/meter/api/dailyData", String::class.java)

        // then
        val energyChartDataItem = Gson().fromJson(entity.body, Array<EnergyChartDataItem>::class.java).firstOrNull()
        assertNotNull(energyChartDataItem)
        assertEquals(expectedTimestamp, energyChartDataItem?.timestamp)
        assertEquals(expectedIndoorTemperature, energyChartDataItem?.indoorTemperature)
        assertEquals(expectedOutdoorTemperature, energyChartDataItem?.outdoorTemperature)
        assertEquals(expectedProduction, energyChartDataItem?.production)
    }

    @Test
    fun monthlyDataReturnsHttpOk() {
        // when
        val entity = restTemplate.getForEntity("/meter/api/monthlyData", String::class.java)

        // then
        assertEquals(OK, entity.statusCode)
    }

    @Test
    fun monthlyDataReturnsData() {
        // given
        val expectedTimestamp = nowDate()
        val expectedProduction = 9000F
        val expectedIndoorTemperature = 17F
        val expectedOutdoorTemperature = 66.6F

        val monthlyData = MonthlyData(
            0,
            expectedTimestamp,
            0F,
            0F,
            expectedProduction,
            expectedOutdoorTemperature,
            expectedIndoorTemperature
        )
        monthlyDataRepository.save(monthlyData)

        // when
        val entity = restTemplate.getForEntity("/meter/api/monthlyData", String::class.java)

        // then
        val energyChartDataItem = Gson().fromJson(entity.body, Array<EnergyChartDataItem>::class.java).firstOrNull()
        assertNotNull(energyChartDataItem)
        assertEquals(expectedTimestamp, energyChartDataItem?.timestamp)
        assertEquals(expectedIndoorTemperature, energyChartDataItem?.indoorTemperature)
        assertEquals(expectedOutdoorTemperature, energyChartDataItem?.outdoorTemperature)
        assertEquals(expectedProduction, energyChartDataItem?.production)
    }
}