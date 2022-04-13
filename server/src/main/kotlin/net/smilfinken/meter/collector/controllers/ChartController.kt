package net.smilfinken.meter.collector.controllers

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/meter")
class ChartController {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ChartController::class.java)
    }

    @GetMapping("/chart")
    fun chart(model: Model): String {
        LOGGER.trace("chart()")

        model["title"] = "Energy stats"
        return "chart"
    }
}