package net.smilfinken.meter.collector.controllers

import net.smilfinken.meter.collector.persistance.CurrentStatus
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/meter/status")
class GetStatusController {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(GetStatusController::class.java)!!
    }

    @GetMapping("")
    fun currentStatus(): ResponseEntity<String> {
        LOGGER.trace("currentStatus()")
        return ResponseEntity.ok(CurrentStatus.getCurrentStatus().toString())
    }
}