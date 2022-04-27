package net.smilfinken.meter.collector.api.fronius.archive

import net.smilfinken.meter.collector.api.fronius.EnergyData

internal class InverterData(
    val EnergyReal_WAC_Sum_Produced: EnergyData
)

internal data class Inverter(
    val Data: InverterData,
    val DeviceType: Int,
    val NodeType: Int,
    val Start: String,
    val End: String
)

internal data class Data(
    val Inverter: Inverter
)

internal data class Body(
    val Data: Data
)

internal data class ResponseData(
    val Body: Body,
    val Head: Any
)
