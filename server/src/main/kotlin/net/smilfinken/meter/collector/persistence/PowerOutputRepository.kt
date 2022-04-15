package net.smilfinken.meter.collector.persistence

import net.smilfinken.meter.collector.model.DataReport
import net.smilfinken.meter.collector.model.PowerOutput
import org.springframework.data.repository.CrudRepository

interface PowerOutputRepository : CrudRepository<PowerOutput, Long> {
    fun findByReport(report: DataReport): PowerOutput
}