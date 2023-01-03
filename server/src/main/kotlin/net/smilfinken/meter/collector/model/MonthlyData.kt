package net.smilfinken.meter.collector.model

import java.util.Date
import javax.persistence.Entity

@Entity
class MonthlyData(
    id: Long,
    timestamp: Date,
    output: Float,
    intake: Float,
    produced: Float,
    outdoorTemp: Float,
    indoorTemp: Float
) : PeriodicData(id, timestamp, output, intake, produced, outdoorTemp, indoorTemp)
