package net.smilfinken.meter.collector.controllers

import net.smilfinken.meter.collector.Constants.Companion.TEST_MESSAGE
import net.smilfinken.meter.collector.persistence.DataItemRepository
import net.smilfinken.meter.collector.persistence.DataReportRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus.OK
import javax.transaction.Transactional

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class PostDataControllerTest(
    @Autowired private val restTemplate: TestRestTemplate,
    @Autowired private val dataReportRepository: DataReportRepository,
    @Autowired private val dataItemRepository: DataItemRepository
) {
    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
        dataItemRepository.deleteAll()
        dataReportRepository.deleteAll()
    }

    @Test
    fun submit() {
        // when
        val entity = restTemplate.postForEntity("/meter/collector/submit", TEST_MESSAGE, String::class.java, Unit)

        // then
        assertEquals(OK, entity.statusCode)
        assertEquals(1, dataReportRepository.findAll().count())
        assertEquals(26, dataItemRepository.findByReport(dataReportRepository.findAll().first()).count())
    }
}