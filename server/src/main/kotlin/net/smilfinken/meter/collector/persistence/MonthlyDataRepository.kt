package net.smilfinken.meter.collector.persistence

import net.smilfinken.meter.collector.model.MonthlyData
import org.springframework.data.repository.CrudRepository
import java.util.Date

interface MonthlyDataRepository : CrudRepository<MonthlyData, Long> {
    fun findByTimestamp(timestamp: Date): MonthlyData?
    fun findTop12ByOrderByTimestampDesc(): List<MonthlyData>
}