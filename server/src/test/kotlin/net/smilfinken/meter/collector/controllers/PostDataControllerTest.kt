package net.smilfinken.meter.collector.controllers

import net.smilfinken.meter.collector.Constants.Companion.TEST_MESSAGE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class PostDataControllerTest(@Autowired val restTemplate: TestRestTemplate) {
    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun submit() {
        val entity = restTemplate.postForEntity("/meter/collector/submit", TEST_MESSAGE, String::class.java, Unit)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
    }
}