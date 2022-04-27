package net.smilfinken.meter.collector.maintenance

import net.smilfinken.meter.collector.model.HourlyData
import net.smilfinken.meter.collector.persistence.DataItemRepository
import net.smilfinken.meter.collector.persistence.DataReportRepository
import net.smilfinken.meter.collector.persistence.HourlyDataRepository
import net.smilfinken.meter.collector.persistence.PowerOutputRepository
import net.smilfinken.meter.collector.persistence.TemperatureRepository
import net.smilfinken.meter.collector.util.Dater.Companion.firstMinuteOfHour
import net.smilfinken.meter.collector.util.Dater.Companion.isSameHour
import net.smilfinken.meter.collector.util.Dater.Companion.minDate
import org.apache.commons.lang3.time.DateUtils.addHours
import org.apache.commons.lang3.time.DateUtils.truncate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Calendar.HOUR
import java.util.Date

@Component
class DataAggregator(
    @Autowired private val dataReportRepository: DataReportRepository,
    @Autowired private val dataItemRepository: DataItemRepository,
    @Autowired private val powerOutputRepository: PowerOutputRepository,
    @Autowired private val temperatureRepository: TemperatureRepository,
    @Autowired private val hourlyDataRepository: HourlyDataRepository
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(DataAggregator::class.java)!!
    }

    @Value("\${application.keys.energy.incoming}")
    private val energyIncomingKey: String = ""

    @Value("\${application.keys.energy.outgoing}")
    private val energyOutgoingKey: String = ""

    @Value("\${application.keys.temperature.indoor}")
    private val indoorTemperatureKey: String = ""

    @Value("\${application.keys.temperature.outdoor}")
    private val outdoorTemperatureKey: String = ""

    private class Accumulator(val timestamp: Date) {
        private var count: Int = 0
        private var output: Float = 0F
        private var intake: Float = 0F
        private var production: Float = 0F
        private var indoorTemperature: Float = 0F
        private var outdoorTemperature: Float = 0F

        init {
            LOGGER.trace("starting new average for timestamp $timestamp")
        }

        fun getCount() = count

        fun addItem(
            output: Float,
            intake: Float,
            production: Float,
            outdoorTemperature: Float,
            indoorTemperature: Float
        ) {
            count++
            this.output += output
            this.intake += intake
            this.production += production
            this.outdoorTemperature += outdoorTemperature
            this.indoorTemperature += indoorTemperature
        }

        fun toHourlyData() = HourlyData(
            0,
            timestamp,
            output / count,
            intake / count,
            production / count,
            outdoorTemperature / count,
            indoorTemperature / count
        )
    }

    internal fun aggregateHourlyData() {
        LOGGER.trace("=> aggregateHourlyData()")

        val cutoffTime = firstMinuteOfHour()
        val lastEntryTime = addHours(hourlyDataRepository.findTopByOrderByTimestampDesc()?.timestamp ?: minDate(), 1)

        val reports = dataReportRepository.findAllByReceivedTimestampBetween(lastEntryTime, cutoffTime)
        LOGGER.trace("found ${reports.size} entries between $lastEntryTime and $cutoffTime")
        if (reports.isEmpty()) {
            return
        }

        var accumulator = Accumulator(truncate(reports.first().receivedTimestamp, HOUR))
        reports
            .forEach { report ->
                LOGGER.trace("processing report from ${report.receivedTimestamp}")

                if (!isSameHour(report.receivedTimestamp, accumulator.timestamp)) {
                    if (accumulator.getCount() > 0) {
                        saveHourlyData(accumulator)
                    }
                    accumulator = Accumulator(truncate(report.receivedTimestamp, HOUR))
                }
                accumulator.addItem(
                    dataItemRepository.findByReportAndObis(report, energyOutgoingKey).value * 1000,
                    dataItemRepository.findByReportAndObis(report, energyIncomingKey).value * 1000,
                    powerOutputRepository.findByReport(report).output,
                    temperatureRepository.findByReportAndSource(report, outdoorTemperatureKey).value,
                    temperatureRepository.findByReportAndSource(report, indoorTemperatureKey).value
                )
            }
        saveHourlyData(accumulator)
    }

    private fun saveHourlyData(accumulator: Accumulator) {
        LOGGER.debug("saving ${accumulator.getCount()} items of data for timestamp ${accumulator.timestamp}")
        hourlyDataRepository.save(accumulator.toHourlyData())
    }

    fun aggregateDailyData() {
        LOGGER.trace("=> aggregateDailyData()")
    }

    fun aggregateWeeklyData() {
        LOGGER.trace("=> aggregateWeeklyData()")
    }
}