package net.smilfinken.meter.collector.api

import com.google.gson.Gson
import net.smilfinken.meter.collector.exceptions.DataParseException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URL

@Component
class FibaroClient {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(FibaroClient::class.java)

        private const val HOME_CENTER_API_BASE_URL = "http://fibaro.smilfinken.net/api"
        private const val HOME_CENTER_INDOOR_TEMPERATURE_URL =
            "$HOME_CENTER_API_BASE_URL/temperature/now/now/summary-graph/devices/temperature/296"
        private const val HOME_CENTER_OUTDOOR_TEMPERATURE_URL =
            "$HOME_CENTER_API_BASE_URL/temperature/now/now/summary-graph/devices/temperature/248"
    }

    internal fun getCurrentIndoorTemperature(): Float =
        requestResponseAndReturnFirstValue(HOME_CENTER_INDOOR_TEMPERATURE_URL)

    internal fun getCurrentOutdoorTemperature(): Float =
        requestResponseAndReturnFirstValue(HOME_CENTER_OUTDOOR_TEMPERATURE_URL)

    private fun requestResponseAndReturnFirstValue(url: String): Float {
        try {
            val apiResponse = URL(url).readText()
            LOGGER.trace("response from Fibaro Home Center =\n$apiResponse")
            return Gson().fromJson(apiResponse, Array<Array<Float>>::class.java).first()[1]
        } catch (throwable: Throwable) {
            LOGGER.error("failed to get temperature data from Fibaro home center at $url", throwable)
            throw DataParseException()
        }
    }
}