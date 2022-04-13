package net.smilfinken.meter.collector.model

import java.time.LocalDateTime

data class EnergyChartDataItem(val timestamp: LocalDateTime, val production: Float, val consumption: Float)