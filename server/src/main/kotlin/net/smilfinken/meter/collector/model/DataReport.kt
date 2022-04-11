package net.smilfinken.meter.collector.model

import java.sql.Timestamp

data class DataReport(private val timestamp: Timestamp) {
    private val dataItems: MutableSet<DataItem> = mutableSetOf()

    fun getTimestamp(): Timestamp {
        return timestamp
    }

    fun addItem(dataItem: DataItem) {
        dataItems.add(dataItem)
    }

    fun getCount(): Int = dataItems.size
}
