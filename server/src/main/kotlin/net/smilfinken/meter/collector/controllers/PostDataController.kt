package net.smilfinken.meter.collector.controllers

import net.smilfinken.meter.collector.api.FibaroClient
import net.smilfinken.meter.collector.api.FroniusClient
import net.smilfinken.meter.collector.exceptions.CrcVerificationException
import net.smilfinken.meter.collector.exceptions.DataParseException
import net.smilfinken.meter.collector.model.DataItem
import net.smilfinken.meter.collector.model.DataReport
import net.smilfinken.meter.collector.model.PowerOutput
import net.smilfinken.meter.collector.model.Temperature
import net.smilfinken.meter.collector.util.Dater.Companion.nowDate
import net.smilfinken.meter.collector.util.Parser.Companion.parseDateString
import net.smilfinken.meter.collector.util.Parser.Companion.parseIdString
import net.smilfinken.meter.collector.util.Verifier.Companion.verifyData
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
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
        private val DATA_ITEM_PATTERN = """\d+-\d+:\d+\.\d+\.\d+\(.+\)""".toRegex()
    }

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var froniusClient: FroniusClient

    @Autowired
    private lateinit var fibaroClient: FibaroClient

    @Value("\${application.keys.temperature.indoor}")
    private val indoorTemperatureKey: String = ""

    @Value("\${application.keys.temperature.outdoor}")
    private val outdoorTemperatureKey: String = ""

    @PostMapping("/submit")
    fun submit(@RequestBody data: String): ResponseEntity<String> {
        LOGGER.trace("=> submit()")
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
            val dataReport =
                DataReport(
                    0,
                    parseDateString(lines.removeAt(0)),
                    nowDate()
                )
            entityManager.persist(dataReport)
            lines
                .filter { it.matches(DATA_ITEM_PATTERN) }
                .map { DataItem.parseData(it, dataReport) }
                .forEach {
                    itemCount++
                    entityManager.persist(it)
                }

            entityManager.persist(PowerOutput(0, dataReport, froniusClient.getCurrentPAC() ?: 0F))

            try {
                val temperature = fibaroClient.getCurrentIndoorTemperature()
                entityManager.persist(Temperature(0, dataReport, temperature, indoorTemperatureKey))
                LOGGER.debug("stored indoor temperature = $temperature")
            } catch (throwable: DataParseException) {
                LOGGER.warn("failed to fetch indoor temperature", throwable)
            }

            try {
                val temperature = fibaroClient.getCurrentOutdoorTemperature()
                entityManager.persist(Temperature(0, dataReport, temperature, outdoorTemperatureKey))
                LOGGER.debug("stored outdoor temperature = $temperature")
            } catch (throwable: DataParseException) {
                LOGGER.warn("failed to fetch outdoor temperature", throwable)
            }

            val message = "" +
                    "stored $itemCount data items" +
                    " for ID $id" +
                    " recorded at ${dataReport.timestamp}"
            LOGGER.info(message)

            return ResponseEntity.ok(id)
        } catch (throwable: CrcVerificationException) {
            LOGGER.error("failed to verify data", throwable)
            return ResponseEntity.internalServerError().body(throwable.localizedMessage)
        } catch (throwable: IllegalArgumentException) {
            LOGGER.error("failed to parse timestamp", throwable)
            return ResponseEntity.internalServerError().body(throwable.localizedMessage)
        } catch (throwable: Throwable) {
            LOGGER.error("failed to store submitted data", throwable)
            return ResponseEntity.internalServerError().body(throwable.localizedMessage)
        }
    }
}