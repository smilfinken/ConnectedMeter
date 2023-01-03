package net.smilfinken.meter.collector.persistence

import net.smilfinken.meter.collector.model.DailyData
import org.springframework.data.repository.CrudRepository
import java.util.Date

interface DailyDataRepository : CrudRepository<DailyData, Long> {
    fun findByTimestamp(timestamp: Date): DailyData?
    fun findTop30ByOrderByTimestampDesc(): List<DailyData>
}