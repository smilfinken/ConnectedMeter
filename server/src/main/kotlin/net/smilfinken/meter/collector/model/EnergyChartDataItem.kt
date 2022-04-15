package net.smilfinken.meter.collector.model

import java.time.LocalDateTime

data class EnergyChartDataItem(
    val timestamp: LocalDateTime,
    val usage: Float,
    val production: Float,
    val balance: Float
)