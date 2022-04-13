package net.smilfinken.meter.collector.persistence

import net.smilfinken.meter.collector.model.DataItem
import net.smilfinken.meter.collector.model.DataReport
import org.springframework.data.repository.CrudRepository

interface DataItemRepository : CrudRepository<DataItem, Long> {
    fun findByReport(report: DataReport): List<DataItem>
    fun findByReportAndObis(dataReport: DataReport, obis: String): DataItem
}