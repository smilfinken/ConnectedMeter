package net.smilfinken.meter.collector.controllers

import net.smilfinken.meter.collector.api.FroniusClient
import net.smilfinken.meter.collector.model.EnergyChartDataItem
import net.smilfinken.meter.collector.model.EnergySum
import net.smilfinken.meter.collector.model.Mapper
import net.smilfinken.meter.collector.model.ProductionSum
import net.smilfinken.meter.collector.persistence.DailyDataRepository
import net.smilfinken.meter.collector.persistence.DataReportRepository
import net.smilfinken.meter.collector.persistence.HourlyDataRepository
import net.smilfinken.meter.collector.persistence.MonthlyDataRepository
import net.smilfinken.meter.collector.util.Dater.Companion.firstHourOfDay
import net.smilfinken.meter.collector.util.Dater.Companion.nowDate
import org.apache.commons.lang3.time.DateUtils.addDays
import org.apache.commons.lang3.time.DateUtils.addHours
import org.apache.commons.lang3.time.DateUtils.addMonths
import org.apache.commons.lang3.time.DateUtils.truncate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.temporal.ChronoUnit.DAYS
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.HOUR
import java.util.Date

@RestController
@RequestMapping("/meter/api")
class ApiController(
    @Autowired private val commonFunctions: CommonFunctions,
    @Autowired private val hourlyDataRepository: HourlyDataRepository,
    @Autowired private val dailyDataRepository: DailyDataRepository,
    @Autowired private val monthlyDataRepository: MonthlyDataRepository,
    @Autowired private val dataReportRepository: DataReportRepository,
    @Autowired private val froniusClient: FroniusClient
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ApiController::class.java)!!
    }

    @Value("\${application.chart.daily.daysToShow}")
    private val daysToShow = 30

    @GetMapping(path = ["/hourlyData", "/hourlyData/{hoursAgo}"], produces = ["application/json"])
    fun hourlyData(@PathVariable(required = false) hoursAgo: Int? = null): List<EnergyChartDataItem> {
        LOGGER.trace("=> hourlyData($hoursAgo)")

        return if (hoursAgo == null) {
            hourlyDataRepository.findTop24ByOrderByTimestampDesc().reversed()
        } else {
            try {
                listOf(
                    hourlyDataRepository.findByTimestamp(
                        truncate(Date.from(addHours(nowDate(), -hoursAgo).toInstant()), HOUR)
                    )!!
                )
            } catch (throwable: Throwable) {
                emptyList()
            }
        }.map {
            EnergyChartDataItem.fromPeriodicData(it)
        }
    }

    @GetMapping(path = ["/dailyData", "/dailyData/{daysAgo}"], produces = ["application/json"])
    fun dailyData(@PathVariable(required = false) daysAgo: Int? = null): List<EnergyChartDataItem> {
        LOGGER.trace("=> dailyData($daysAgo)")

        return if (daysAgo == null) {
            dailyDataRepository.findTop30ByOrderByTimestampDesc().reversed()
        } else {
            try {
                listOf(
                    dailyDataRepository.findByTimestamp(
                        truncate(Date.from(addDays(nowDate(), -daysAgo).toInstant()), HOUR)
                    )!!
                )
            } catch (throwable: Throwable) {
                emptyList()
            }
        }.map { EnergyChartDataItem.fromPeriodicData(it) }
    }

    @GetMapping(path = ["/monthlyData", "/monthlyData/{monthsAgo}"], produces = ["application/json"])
    fun monthlyData(@PathVariable(required = false) monthsAgo: Int? = null): List<EnergyChartDataItem> {
        LOGGER.trace("=> monthlyData($monthsAgo)")

        return if (monthsAgo == null) {
            monthlyDataRepository.findTop12ByOrderByTimestampDesc().reversed()
        } else {
            try {
                listOf(
                    monthlyDataRepository.findByTimestamp(
                        truncate(Date.from(addMonths(nowDate(), -monthsAgo).toInstant()), DAY_OF_MONTH)
                    )!!
                )
            } catch (throwable: Throwable) {
                emptyList()
            }
        }.map {
            EnergyChartDataItem.fromPeriodicData(it)
        }
    }

    @GetMapping(path = ["/dailyBalance", "/dailyBalance/{daysAgo}"])
    fun dailyBalance(@PathVariable(required = false) daysAgo: Int? = null): List<EnergySum> {
        LOGGER.trace("=> dailyBalance($daysAgo)")

        val result = commonFunctions.getEnergyBalanceHistory(daysAgo)
        LOGGER.trace(
            result.joinToString(
                separator = ", ",
                transform = {
                    val interval = "${it.fromTimestamp.toInstant()} -- ${it.toTimestamp.toInstant()}"
                    "total for $interval: ${Mapper.fromOBIS(it.obis)} = ${it.sum} Wh"
                }
            )
        )
        return result
    }

    @GetMapping(path = ["dailyProduction", "/dailyProduction/{daysAgo}"])
    fun dailyProduction(@PathVariable(required = false) daysAgo: Int? = null): List<ProductionSum> {
        LOGGER.trace("=> dailyProduction($daysAgo)")

        return if (daysAgo == 0 || daysAgo == null) {
            LOGGER.trace("fetching production data for today")
            froniusClient.getDayEnergy()?.map { value ->
                ProductionSum(firstHourOfDay(), nowDate(), value.toDouble())
            } ?: emptyList()
        } else {
            val now = nowDate()
            froniusClient.getDayEnergy(daysAgo)?.mapIndexed { index, value ->
                val startTimestamp = firstHourOfDay(Date.from(now.toInstant().minus(index.toLong(), DAYS)))
                val endTimestamp = firstHourOfDay(Date.from(now.toInstant().minus(index.toLong() - 1, DAYS)))
                LOGGER.trace("got $value as result for the interval $startTimestamp -- $endTimestamp")
                ProductionSum(startTimestamp, endTimestamp, value.toDouble())
            } ?: emptyList()
        }
    }
}