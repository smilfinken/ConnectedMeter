package net.smilfinken.meter.collector.controllers

import net.smilfinken.meter.collector.model.DataItem
import net.smilfinken.meter.collector.model.DataReport
import net.smilfinken.meter.collector.persistence.DataItemRepository
import net.smilfinken.meter.collector.persistence.DataReportRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import java.sql.Timestamp
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureTestDatabase
internal class GetStatusControllerTest(
    @Autowired private val restTemplate: TestRestTemplate,
    @Autowired private val dataReportRepository: DataReportRepository,
    @Autowired private val dataItemRepository: DataItemRepository
) {
    @BeforeEach
    fun setUp() {
        val dataReport = DataReport(0, Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now()))
        dataReportRepository.save(dataReport)

        listOf("1-0:1.7.0", "1-0:2.7.0").forEach {
            val dataItem = DataItem(0, dataReport, it, 4.2F, "Borks")
            dataItemRepository.save(dataItem)
        }
    }

    @AfterEach
    fun tearDown() {
        dataItemRepository.deleteAll()
        dataReportRepository.deleteAll()
    }

    @Test
    fun currentStatusReturnsHttpOk() {
        // when
        val entity = restTemplate.getForEntity("/meter/status", String::class.java)

        // then
        Assertions.assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
    }
}