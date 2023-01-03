package net.smilfinken.meter.collector.persistence

import net.smilfinken.meter.collector.model.DataReport
import net.smilfinken.meter.collector.model.Temperature
import org.springframework.data.repository.CrudRepository

interface TemperatureRepository : CrudRepository<Temperature, Long> {
    fun findByReport(report: DataReport): List<Temperature>
    fun findByReportAndSource(report: DataReport, source: String): Temperature?
}