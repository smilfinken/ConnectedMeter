package net.smilfinken.meter.collector.model

import java.util.Date
import kotlin.time.DurationUnit.HOURS
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration

class EnergySum(val fromTimestamp: Date, val toTimestamp: Date, val obis: String, average: Double) {
    val sum: Double

    init {
        val hours: Double = (toTimestamp.time - fromTimestamp.time).toDuration(MILLISECONDS).toDouble(HOURS)
        sum = average * hours
    }
}