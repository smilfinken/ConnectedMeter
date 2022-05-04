package net.smilfinken.meter.collector.controllers

import net.smilfinken.meter.collector.model.EnergySum
import net.smilfinken.meter.collector.persistence.DataReportRepository
import net.smilfinken.meter.collector.util.Dater
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.temporal.ChronoUnit.DAYS
import java.util.Date

@Component
class CommonFunctions(@Autowired private val dataReportRepository: DataReportRepository) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(CommonFunctions::class.java)
    }

    internal fun getEnergyBalanceHistory(daysAgo: Int?): List<EnergySum> {
        LOGGER.trace("getEnergyBalanceHistory($daysAgo)")

        val now = Dater.nowDate()
        val timeStamps: Pair<Date, Date> =
            if (daysAgo == 0 || daysAgo == null) {
                Pair(Dater.firstHourOfDay(), now)
            } else {
                Pair(
                    Dater.firstHourOfDay(Date.from(now.toInstant().minus(daysAgo.toLong(), DAYS))),
                    Dater.firstHourOfDay(Date.from(now.toInstant().minus(daysAgo.toLong() - 1, DAYS)))
                )
            }
        LOGGER.trace(timeStamps.toString())
        return dataReportRepository.sumEnergyBalanceByDate(timeStamps.first, timeStamps.second)
    }
}