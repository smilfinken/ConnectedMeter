package net.smilfinken.meter.collector.controllers

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/")
class DefaultController {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(DashboardController::class.java)
    }

    @GetMapping(path = ["", "/meter"])
    fun default(): String {
        LOGGER.trace("default()")

        return "redirect:/meter/dashboard"
    }
}