package net.smilfinken.meter.collector.persistence

import net.smilfinken.meter.collector.model.HourlyData
import org.springframework.data.repository.CrudRepository
import java.util.Date

interface HourlyDataRepository : CrudRepository<HourlyData, Long> {
    fun findByTimestamp(timestamp: Date): HourlyData?
    fun findByTimestampBetween(start: Date, end: Date): HourlyData?
    fun findTopByOrderByTimestampDesc(): HourlyData?
    fun findTop24ByOrderByTimestampDesc(): List<HourlyData>
}