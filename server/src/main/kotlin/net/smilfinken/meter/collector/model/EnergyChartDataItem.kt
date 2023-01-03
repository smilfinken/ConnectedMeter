package net.smilfinken.meter.collector.model

import java.util.Date

data class EnergyChartDataItem(
    val timestamp: Date,
    val usage: Float,
    val production: Float,
    val balance: Float,
    val indoorTemperature: Float,
    val outdoorTemperature: Float
) {
    constructor(timestamp: Date, balance: Double) : this(timestamp, 0F, 0F, balance.toFloat(), 0F, 0F)

    companion object {
        internal fun fromPeriodicData(data: PeriodicData): EnergyChartDataItem {
            return EnergyChartDataItem(
                data.timestamp,
                data.produced + data.intake - data.output,
                data.produced,
                data.output - data.intake,
                data.indoorTemp,
                data.outdoorTemp
            )
        }
    }
}