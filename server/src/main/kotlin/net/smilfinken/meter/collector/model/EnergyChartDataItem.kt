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
    companion object {
        internal fun fromHourlyData(hourlyData: HourlyData): EnergyChartDataItem {
            return EnergyChartDataItem(
                hourlyData.timestamp,
                hourlyData.produced + hourlyData.intake - hourlyData.output,
                hourlyData.produced,
                hourlyData.output - hourlyData.intake,
                hourlyData.indoorTemp,
                hourlyData.outdoorTemp
            )
        }
    }
}