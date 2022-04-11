package net.smilfinken.meter.collector

import org.springframework.boot.Banner.Mode.OFF
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CollectorApplication

fun main(args: Array<String>) {
    runApplication<CollectorApplication>(*args) {
        setBannerMode(OFF)
    }
}
