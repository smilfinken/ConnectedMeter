package net.smilfinken.meter.collector.api.fronius.realtime

import net.smilfinken.meter.collector.api.fronius.EnergyData

internal data class Data(
    val DAY_ENERGY: EnergyData,
    val PAC: EnergyData,
    val TOTAL_ENERGY: EnergyData,
    val YEAR_ENERGY: EnergyData
)

internal data class Body(
    val Data: Data
)

internal data class ResponseData(
    val Body: Body,
    val Head: Any
)