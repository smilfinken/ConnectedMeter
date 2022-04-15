package net.smilfinken.meter.collector.api

import com.google.gson.Gson
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URL

class Status(
    val Code: Int,
    val Reason: String,
    val UserMessage: String
)

private data class RequestArguments(
    val DeviceClass: String,
    val Scope: String
)

private data class Head(
    val RequestArguments: RequestArguments,
    val Status: Status
)

private data class EnergyData(
    val Unit: String,
    val Values: Map<String, Float>
)

private data class Data(
    val DAY_ENERGY: EnergyData,
    val PAC: EnergyData,
    val TOTAL_ENERGY: EnergyData,
    val YEAR_ENERGY: EnergyData
)

private data class Body(
    val Data: Data
)

private data class RealtimeData(
    val Body: Body,
    val Head: Head
)

@Component
class FroniusClient {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(FroniusClient::class.java)

        private const val INVERTER_API_BASE_URL = "http://fronius.smilfinken.net/solar_api/v1"
        private const val INVERTER_REALTIME_DATA_URL =
            "$INVERTER_API_BASE_URL/GetInverterRealtimeData.cgi?Scope=System"
    }

    internal fun GetCurrentPAC(): Float {
        var result = 0F

        try {
            val apiResponse = URL(INVERTER_REALTIME_DATA_URL).readText()
            LOGGER.trace(apiResponse)
            val realtimeData = Gson().fromJson(apiResponse, RealtimeData::class.java)
            result = realtimeData.Body.Data.PAC.Values["1"]?.toFloat() ?: result
            LOGGER.debug("PAC = ${realtimeData.Body.Data.PAC.Values["1"]}")
        } catch (throwable: Throwable) {
            LOGGER.error("Failed to get energy data from inverter at $INVERTER_REALTIME_DATA_URL", throwable)
        }

        return result
    }
}