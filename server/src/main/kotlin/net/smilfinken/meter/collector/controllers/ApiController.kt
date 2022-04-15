package net.smilfinken.meter.collector.controllers

import net.smilfinken.meter.collector.model.EnergyChartDataItem
import net.smilfinken.meter.collector.persistence.DataItemRepository
import net.smilfinken.meter.collector.persistence.DataReportRepository
import net.smilfinken.meter.collector.persistence.PowerOutputRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/meter/api")
class ApiController(
    @Autowired private val dataReportRepository: DataReportRepository,
    @Autowired private val dataItemRepository: DataItemRepository,
    @Autowired private val powerOutputRepository: PowerOutputRepository
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ApiController::class.java)!!

        private const val VALUE_COUNT = 500
        private const val ENERGY_INCOMING_KEY = "1-0:1.7.0"
        private const val ENERGY_OUTGOING_KEY = "1-0:2.7.0"
    }

    @GetMapping("/data")
    fun data(): List<EnergyChartDataItem> {
        LOGGER.trace("data()")

        val result = emptyList<EnergyChartDataItem>().toMutableList()

        dataReportRepository
            .findAllByOrderByTimestampAsc()
            .takeLast(VALUE_COUNT)
            .forEach { dataReport ->
                val production = powerOutputRepository.findByReport(dataReport).output
                val incoming = dataItemRepository.findByReportAndObis(dataReport, ENERGY_INCOMING_KEY).value * 1000
                val outgoing = dataItemRepository.findByReportAndObis(dataReport, ENERGY_OUTGOING_KEY).value * 1000
                val usage = production + incoming - outgoing
                result.add(
                    EnergyChartDataItem(
                        dataReport.receivedTimestamp.toLocalDateTime(),
                        usage,
                        production,
                        outgoing - incoming
                    )
                )

                LOGGER.debug("report timestamp   ${dataReport.timestamp}")
                LOGGER.debug("received timestamp ${dataReport.receivedTimestamp}")

                val message = "" +
                        "power output = $production," +
                        " total usage = $usage, " +
                        " energy input = $incoming," +
                        " energy output = $outgoing"
                LOGGER.trace(message)
            }

        return result
    }
}