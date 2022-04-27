package net.smilfinken.meter.collector.api.fronius

internal data class EnergyData(
    val Unit: String,
    val Values: Map<String, Float>,
    val _comment: String
)
