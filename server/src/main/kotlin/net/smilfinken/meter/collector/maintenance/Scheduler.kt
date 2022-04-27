package net.smilfinken.meter.collector.maintenance

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class Scheduler(
    @Autowired
    private val dataAggregator: DataAggregator
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(DataAggregator::class.java)!!
    }

    @Scheduled(cron = "\${application.schedule.cron}")
    private fun aggregateData() {
        LOGGER.trace("=> aggregateData()")

        dataAggregator.aggregateHourlyData()
        dataAggregator.aggregateDailyData()
        dataAggregator.aggregateWeeklyData()
    }
}