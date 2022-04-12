package net.smilfinken.meter.collector.controllers

import net.smilfinken.meter.collector.exceptions.CrcVerificationException
import net.smilfinken.meter.collector.model.DataItem
import net.smilfinken.meter.collector.model.DataReport
import net.smilfinken.meter.collector.util.Parser.Companion.parseDateString
import net.smilfinken.meter.collector.util.Parser.Companion.parseIdString
import net.smilfinken.meter.collector.util.Verifier.Companion.verifyData
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.transaction.Transactional

@Controller
@Transactional
@RequestMapping("/meter/collector")
class PostDataController {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(PostDataController::class.java)!!
        private val DATAITEM_PATTERN = """\d+-\d+:\d+\.\d+\.\d+\(.+\)""".toRegex()
    }

    @PersistenceContext
    private lateinit var entityManager: EntityManager

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

            var itemCount = 0
            val dataReport = DataReport(0, parseDateString(lines.removeAt(0)))
            entityManager.persist(dataReport)
            lines
                .filter { it.matches(DATAITEM_PATTERN) }
                .map { DataItem.parseData(it, dataReport) }
                .forEach {
                    itemCount++
                    entityManager.persist(it)
                }

            val message = "" +
                    "stored $itemCount data items" +
                    " for ID $id" +
                    " recorded at ${dataReport.timestamp}"
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