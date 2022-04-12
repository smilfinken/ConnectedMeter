package net.smilfinken.meter.collector.persistance

import net.smilfinken.meter.collector.model.DataReport
import java.sql.Timestamp
import java.time.LocalDateTime

class CurrentStatus {
    companion object {
        private var latestData = DataReport(Timestamp.valueOf(LocalDateTime.now()))

        internal fun setCurrentStatus(data: DataReport) {
            latestData = data
        }

        internal fun getCurrentStatus(): DataReport {
            return latestData
        }
    }
}