package net.smilfinken.meter.collector.controllers

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus.OK

@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class DashboardControllerTest(@Autowired private val restTemplate: TestRestTemplate) {
    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun dashboardReturnsHttpOk() {
        // when
        val entity = restTemplate.getForEntity("/meter/dashboard", String::class.java)

        // then
        assertEquals(OK, entity.statusCode)
    }
}