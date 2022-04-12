package net.smilfinken.meter.collector.controllers

import net.smilfinken.meter.collector.persistence.DataItemRepository
import net.smilfinken.meter.collector.persistence.DataReportRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.time.format.DateTimeFormatter

@Controller
@RequestMapping("/meter/status")
class GetStatusController(
    @Autowired private val dataReportRepository: DataReportRepository,
    @Autowired private val dataItemRepository: DataItemRepository
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(GetStatusController::class.java)!!
        private val KEYS = listOf("1-0:1.7.0", "1-0:2.7.0")
    }

    @GetMapping("")
    fun currentStatus(): ResponseEntity<String> {
        LOGGER.trace("currentStatus()")

        val result = StringBuilder("<html>\n\t<head><title>energy stats</title></head>\n\t<body>\n")

        dataReportRepository
            .findAllByOrderByTimestampDesc()
            .take(30)
            .forEach { dataReport ->
                result.append(
                    "\t\t\t<h3>${
                        dataReport.timestamp.toLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss"))
                    }</h3><br/>\n"
                )

                dataItemRepository
                    .findByReport(dataReport)
                    .filter { it.obis in KEYS }
                    .forEach { dataItem -> result.append("\t\t\t$dataItem<br/>\n") }
            }

        result.append("\t</body>\n</html>\n")

        return ResponseEntity.ok(result.toString())
    }
}