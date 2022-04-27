package net.smilfinken.meter.collector.api

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class FibaroClientTest {
    @Autowired
    private lateinit var fibaroClient: FibaroClient

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun getCurrentIndoorTemperature() {
        // when
        fibaroClient.getCurrentIndoorTemperature()
    }

    @Test
    fun getCurrentOutdoorTemperature() {
        // when
        fibaroClient.getCurrentOutdoorTemperature()
    }
}