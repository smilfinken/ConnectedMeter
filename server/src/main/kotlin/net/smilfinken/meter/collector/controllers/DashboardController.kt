package net.smilfinken.meter.collector.controllers

import net.smilfinken.meter.collector.api.FroniusClient
import net.smilfinken.meter.collector.model.DailyStatistics
import net.smilfinken.meter.collector.model.EnergySum
import net.smilfinken.meter.collector.util.Dater.Companion.LOCAL_TIME_ZONE
import net.smilfinken.meter.collector.util.Dater.Companion.firstHourOfDay
import org.apache.commons.lang3.time.DateUtils.addDays
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.time.format.DateTimeFormatter

@Controller
@RequestMapping("/meter")
class DashboardController(
    @Autowired private val commonFunctions: CommonFunctions,
    @Autowired private val froniusClient: FroniusClient
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(DashboardController::class.java)
    }

    @Value("\${application.statistics.daily.daysToShow}")
    private val daysToShow = 7

    @Value("\${application.info.name}")
    private val applicationName = ""

    @Value("\${application.info.version}")
    private val applicationVersion = ""

    @Value("\${application.keys.energy.incoming}")
    private val energyIncomingKey = ""

    @Value("\${application.keys.energy.outgoing}")
    private val energyOutgoingKey = ""

    @Value("\${application.format.datePattern}")
    private val datePattern = ""

    @GetMapping("/dashboard")
    fun dashboard(model: Model): String {
        LOGGER.trace("=> dashboard()")

        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(datePattern)

        model["title"] = "Energy stats"
        model["name"] = applicationName
        model["version"] = applicationVersion

        try {
            val production = froniusClient.getDayEnergy(daysToShow - 1)
            model["dailyStatistics"] = (0..daysToShow - 1).map { index ->
                val energyBalanceHistory = commonFunctions.getEnergyBalanceHistory(index)
                val data = DailyStatistics(
                    when (index) {
                        0 -> "Today (so far)"
                        1 -> "Yesterday"
                        else ->
                            addDays(firstHourOfDay(), -index)
                                .toInstant()
                                .atZone(LOCAL_TIME_ZONE)
                                .format(dateTimeFormatter)
                    },
                    production?.get(index) ?: 0F,
                    getIntakeValue(energyBalanceHistory),
                    getOutputValue(energyBalanceHistory)
                )
                LOGGER.trace("$index: $data")
                data
            }
        } catch (throwable: Throwable) {
            LOGGER.error("failed to retrieve energy data from inverter", throwable)
        }

        return "dashboard"
    }

    private fun getIntakeValue(balance: List<EnergySum>): Float {
        return (balance.firstOrNull { value -> value.obis == energyIncomingKey }?.sum ?: 0.0).toFloat()
    }

    private fun getOutputValue(balance: List<EnergySum>): Float {
        return (balance.firstOrNull { value -> value.obis == energyOutgoingKey }?.sum ?: 0.0).toFloat()
    }
}