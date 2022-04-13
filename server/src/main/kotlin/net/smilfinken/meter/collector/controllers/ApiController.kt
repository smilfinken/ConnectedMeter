package net.smilfinken.meter.collector.controllers

import net.smilfinken.meter.collector.model.EnergyChartDataItem
import net.smilfinken.meter.collector.persistence.DataItemRepository
import net.smilfinken.meter.collector.persistence.DataReportRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/meter/api")
class ApiController(
    @Autowired private val dataReportRepository: DataReportRepository,
    @Autowired private val dataItemRepository: DataItemRepository
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ApiController::class.java)!!
        private val KEYS = listOf("1-0:1.7.0", "1-0:2.7.0")
    }

    @GetMapping("/data")
    fun data(): List<EnergyChartDataItem> {
        val result = emptyList<EnergyChartDataItem>().toMutableList()

        dataReportRepository
            .findAllByOrderByTimestampDesc()
            .take(30)
            .forEach { dataReport ->
                LOGGER.debug("report timestamp ${dataReport.timestamp}")
                val input = dataItemRepository.findByReportAndObis(dataReport, "1-0:1.7.0").value
                LOGGER.trace("energy input $input")
                val output = dataItemRepository.findByReportAndObis(dataReport, "1-0:1.8.0").value
                LOGGER.trace("energy output $output")
                result.add(EnergyChartDataItem(dataReport.timestamp.toLocalDateTime(), 0F, output - input))
            }

        LOGGER.debug("returning ${result.size} data items")

        return result
    }
}