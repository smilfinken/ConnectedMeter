package net.smilfinken.meter.collector.controllers

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/meter")
class DashboardController(@Autowired private val context: ApplicationContext) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(DashboardController::class.java)
    }

    @Value("\${application.info.name}")
    private val applicationName: String = ""

    @Value("\${application.info.version}")
    private val applicationVersion: String = ""

    @GetMapping("/dashboard")
    fun dashboard(model: Model): String {
        LOGGER.trace("=> dashboard()")

        model["title"] = "Energy stats"
        model["name"] = applicationName
        model["version"] = applicationVersion

        return "dashboard"
    }
}