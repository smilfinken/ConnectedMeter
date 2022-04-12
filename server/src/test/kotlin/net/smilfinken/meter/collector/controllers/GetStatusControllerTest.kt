package net.smilfinken.meter.collector.controllers

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class GetStatusControllerTest(@Autowired val restTemplate: TestRestTemplate) {
    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun currentStatus() {
        val entity = restTemplate.getForEntity("/meter/status", String::class.java)
        Assertions.assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
    }
}