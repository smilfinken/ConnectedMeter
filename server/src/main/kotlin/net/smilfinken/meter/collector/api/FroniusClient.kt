package net.smilfinken.meter.collector.api

import com.google.gson.Gson
import net.smilfinken.meter.collector.model.DataCache
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URL
import java.time.Duration.ofSeconds
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import net.smilfinken.meter.collector.api.fronius.archive.ResponseData as ArchiveResponseData
import net.smilfinken.meter.collector.api.fronius.realtime.ResponseData as RealtimeResponseData

@Component
class FroniusClient(
    @Autowired private val dataCache: DataCache
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(FroniusClient::class.java)

        private const val DAY_IN_SECONDS = 60 * 60 * 24
    }

    @Value("\${application.api.inverter.baseUri}")
    private val baseUri: String = ""

    @Value("\${application.api.inverter.realtimeDataPath}")
    private val realtimePath: String = ""

    @Value("\${application.api.inverter.archiveDataPath}")
    private val archivePath: String = ""

    @Value("\${application.api.inverter.startDateQueryParameter}")
    private val startParameter: String = ""

    @Value("\${application.api.inverter.endDateQueryParameter}")
    private val endParameter: String = ""

    @Value("\${application.api.inverter.datePattern}")
    private val datePattern = ""

    internal fun getCurrentPAC(): Float? {
        LOGGER.trace("=> getCurrentPAC()")

        var result: Float? = null

        try {
            result = getRealtimeData()!!.Body.Data.PAC.Values["1"]
        } catch (throwable: Throwable) {
            LOGGER.error("failed to get current output from inverter")
        }

        return result
    }

    internal fun getDayEnergy(daysAgo: Int = 0): List<Float>? {
        LOGGER.trace("=> getDayEnergy($daysAgo)")

        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(datePattern)
        val result = emptyList<Float>().toMutableList()
        val now = LocalDate.now()

        try {
            result.add(getRealtimeData()!!.Body.Data.DAY_ENERGY.Values["1"]!!)

            (1..daysAgo)
                .map { index -> dataCache.getFromCache(now.minusDays(index.toLong()).format(dateTimeFormatter)) }
                .takeWhile { data -> data != null }
                .filterNotNull()
                .forEach { data -> result.prepend(data) }

            val archivedDaysToFetch = daysAgo.toLong() - (result.size - 1)
            if (archivedDaysToFetch > 0) {
                getArchiveData(now.minusDays(archivedDaysToFetch), now.minusDays(1))!!
                    .Body.Data.Inverter.Data.EnergyReal_WAC_Sum_Produced.Values
                    .toSortedMap { a: String, b: String -> b.toInt() - a.toInt() }
                    .forEach { data ->
                        result.prepend(
                            dataCache.putInCache(
                                now
                                    .minusDays(archivedDaysToFetch - ofSeconds(data.key.toLong()).toDays())
                                    .format(dateTimeFormatter),
                                data.value
                            )
                        )
                    }
            }
        } catch (throwable: Throwable) {
            LOGGER.error("failed to get day energy data from inverter", throwable)
        }

        return result.reversed()
    }

    private fun getRealtimeData(): RealtimeResponseData? {
        var result: RealtimeResponseData? = null

        val inverterRealtimeDataUrl = "$baseUri/$realtimePath"
        try {
            val apiResponse = URL(inverterRealtimeDataUrl).readText()
            LOGGER.trace("response from Fronius inverter for $inverterRealtimeDataUrl =\n$apiResponse")
            result = Gson().fromJson(apiResponse, RealtimeResponseData::class.java)
        } catch (throwable: Throwable) {
            LOGGER.error("failed to get realtime data from inverter at $inverterRealtimeDataUrl", throwable)
        }

        return result
    }

    private fun getArchiveData(startDate: LocalDate, endDate: LocalDate): ArchiveResponseData? {
        var result: ArchiveResponseData? = null

        val dateTimeFormatter = DateTimeFormatter.ofPattern(datePattern)
        val startString = startDate.format(dateTimeFormatter)
        val endString = endDate.format(dateTimeFormatter)
        val inverterArchiveDataUrl = "$baseUri/$archivePath&$endParameter=$endString&$startParameter=$startString"
        try {
            val apiResponse = URL(inverterArchiveDataUrl).readText().replace("""inverter/1""", "Inverter")
            LOGGER.trace("response from Fronius inverter for $inverterArchiveDataUrl =\n$apiResponse")
            result = Gson().fromJson(apiResponse, ArchiveResponseData::class.java)
        } catch (throwable: Throwable) {
            LOGGER.error("failed to get archive data from inverter at $inverterArchiveDataUrl", throwable)
        }

        return result
    }
}

private fun <Float> MutableList<Float>.prepend(value: Float) {
    this.add(0, value)
}
