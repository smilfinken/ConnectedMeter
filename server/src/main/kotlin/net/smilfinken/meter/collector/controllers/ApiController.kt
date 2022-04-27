package net.smilfinken.meter.collector.controllers

import net.smilfinken.meter.collector.api.FroniusClient
import net.smilfinken.meter.collector.model.EnergyChartDataItem
import net.smilfinken.meter.collector.model.EnergySum
import net.smilfinken.meter.collector.model.Mapper
import net.smilfinken.meter.collector.model.ProductionSum
import net.smilfinken.meter.collector.persistence.DataReportRepository
import net.smilfinken.meter.collector.persistence.HourlyDataRepository
import net.smilfinken.meter.collector.util.Dater.Companion.LOCAL_TIME_ZONE
import net.smilfinken.meter.collector.util.Dater.Companion.firstHourOfDay
import net.smilfinken.meter.collector.util.Dater.Companion.nowDate
import org.apache.commons.lang3.time.DateUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.DAYS
import java.util.Calendar.HOUR
import java.util.Date

@RestController
@RequestMapping("/meter/api")
class ApiController(
    @Autowired private val hourlyDataRepository: HourlyDataRepository,
    @Autowired private val dataReportRepository: DataReportRepository,
    @Autowired private val froniusClient: FroniusClient
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ApiController::class.java)!!
    }

    @GetMapping(path = ["/hourlyData", "/hourlyData/{hoursAgo}"], produces = ["application/json"])
    fun hourlyData(@PathVariable(required = false) hoursAgo: Int? = null): List<EnergyChartDataItem> {
        LOGGER.trace("=> hourlyData($hoursAgo)")

        return if (hoursAgo == null) {
            hourlyDataRepository.findTop24ByOrderByTimestampDesc().reversed()
        } else {
            val timestamp = Date.from(ZonedDateTime.now(LOCAL_TIME_ZONE).minusHours(hoursAgo.toLong()).toInstant())
            val result = hourlyDataRepository.findByTimestamp(DateUtils.truncate(timestamp, HOUR))
            if (result == null) {
                emptyList()
            } else {
                listOf(result)
            }
        }.map {
            EnergyChartDataItem.fromHourlyData(it)
        }
    }

    @GetMapping(path = ["/dailyData", "/dailyData/{daysAgo}"], produces = ["application/json"])
    fun dailyData(@PathVariable(required = false) daysAgo: Int? = null): List<EnergyChartDataItem> {
        LOGGER.trace("=> dailyData($daysAgo)")

        return emptyList()
    }

    @GetMapping(path = ["/monthlyData", "/monthlyData/{monthsAgo}"], produces = ["application/json"])
    fun monthlyData(@PathVariable(required = false) monthsAgo: Int? = null): List<EnergyChartDataItem> {
        LOGGER.trace("=> monthlyData($monthsAgo)")

        return emptyList()
    }

    @GetMapping(path = ["/dailyBalance", "/dailyBalance/{daysAgo}"])
    fun sum(@PathVariable(required = false) daysAgo: Int? = null): List<EnergySum> {
        LOGGER.trace("=> dailyBalance($daysAgo)")

        val now = nowDate()
        val timeStamps: Pair<Date, Date> =
            if (daysAgo == 0 || daysAgo == null) {
                Pair(firstHourOfDay(), now)
            } else {
                Pair(
                    firstHourOfDay(Date.from(now.toInstant().minus(daysAgo.toLong() + 1, DAYS))),
                    firstHourOfDay(Date.from(now.toInstant().minus(daysAgo.toLong(), DAYS)))
                )
            }
        val result = dataReportRepository.sumEnergyBalanceByDate(timeStamps.first, timeStamps.second)
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