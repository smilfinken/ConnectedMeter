package net.smilfinken.meter.collector.controllers

import net.smilfinken.meter.collector.exceptions.CrcVerificationException
import net.smilfinken.meter.collector.model.DataItem
import net.smilfinken.meter.collector.model.DataReport
import net.smilfinken.meter.collector.persistance.CurrentStatus
import net.smilfinken.meter.collector.util.Parser.Companion.parseDateString
import net.smilfinken.meter.collector.util.Parser.Companion.parseIdString
import net.smilfinken.meter.collector.util.Verifier.Companion.verifyData
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/meter/collector")
class PostDataController {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(PostDataController::class.java)!!
        private val DATAITEM_PATTERN = """\d+-\d+:\d+\.\d+\.\d+\(.+\)""".toRegex()
    }

    @PostMapping("/submit")
    fun submit(@RequestBody data: String): ResponseEntity<String> {
        LOGGER.trace("submit()")
        LOGGER.trace("received message:\n$data")

        try {
            verifyData(data)

            val lines = data
                .replace("\r\n", "\n")
                .split("\n")
                .filter { it.isNotBlank() }
                .toMutableList()
            val id = parseIdString(lines.removeAt(0))

            val dataReport = DataReport(parseDateString(lines.removeAt(0)))
            lines
                .filter { it.matches(DATAITEM_PATTERN) }
                .map { DataItem(it) }
                .forEach { dataReport.addItem(it) }
            CurrentStatus.setCurrentStatus(dataReport)

            val message = "" +
                    "stored ${dataReport.getCount()} data items" +
                    " for ID $id" +
                    " recorded at ${dataReport.getTimestamp()}"
            LOGGER.info(message)

            return ResponseEntity.ok(id)
        } catch (throwable: CrcVerificationException) {
            LOGGER.error("Failed to verify data", throwable)
            return ResponseEntity.internalServerError().body(throwable.localizedMessage)
        } catch (throwable: IllegalArgumentException) {
            LOGGER.error("Failed to parse timestamp", throwable)
            return ResponseEntity.internalServerError().body(throwable.localizedMessage)
        } catch (throwable: Throwable) {
            LOGGER.error("Failed to store submitted data", throwable)
            return ResponseEntity.internalServerError().body(throwable.localizedMessage)
        }
    }
}