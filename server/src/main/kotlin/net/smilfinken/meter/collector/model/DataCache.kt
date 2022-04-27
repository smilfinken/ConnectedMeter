package net.smilfinken.meter.collector.model

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DataCache {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(DataCache::class.java)

        private val cache = HashMap<String, Any>()
    }

    fun getFromCache(key: String): Float? {
        try {
            val value = cache[key]
            return if (value is Float) {
                LOGGER.trace("found $key = $value")
                return value
            } else {
                LOGGER.trace("no matching value for key $key")
                null
            }
        } catch (throwable: Throwable) {
            LOGGER.error("Value for $key is not available in cache", throwable)
            return null
        }
    }

    fun putInCache(key: String, value: Float): Float {
        LOGGER.trace("adding $key = $value")
        cache[key] = value
        return value
    }
}