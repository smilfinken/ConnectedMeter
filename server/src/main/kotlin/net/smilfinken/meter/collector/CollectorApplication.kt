package net.smilfinken.meter.collector

import net.smilfinken.meter.collector.maintenance.DataAggregator
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.Banner.Mode.OFF
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class CollectorApplication {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(CollectorApplication::class.java)
    }

    @Autowired
    private lateinit var dataAggregator: DataAggregator

    @EventListener
    fun runDataAggregation(event: ApplicationReadyEvent) {
        LOGGER.trace("=> runDataAggregation()")

        dataAggregator.aggregateHourlyData()
        dataAggregator.aggregateDailyData()
        dataAggregator.aggregateWeeklyData()
    }
}

fun main(args: Array<String>) {
    runApplication<CollectorApplication>(*args) {
        setBannerMode(OFF)
    }
}