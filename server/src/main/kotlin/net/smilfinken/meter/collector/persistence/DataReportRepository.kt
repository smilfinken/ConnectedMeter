package net.smilfinken.meter.collector.persistence

import net.smilfinken.meter.collector.model.DataReport
import org.springframework.data.repository.CrudRepository

interface DataReportRepository : CrudRepository<DataReport, Long> {
    fun findAllByOrderByTimestampAsc(): List<DataReport>
}