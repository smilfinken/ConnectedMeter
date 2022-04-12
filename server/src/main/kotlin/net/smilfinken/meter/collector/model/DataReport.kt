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

    override fun toString(): String {
        return if (getCount() == 0) {
            "<html><head><title>current status</title></head><body><h2>status unavailable</h2></body></html>"
        } else {
            "<html><head><title>current status</title></head><body><h2>%s</h2></body></html>"
                .format(dataItems
                    .map { it.toString() }
                    .reduce { acc, s -> acc.plus("<br/>%s".format(s)) })
        }
    }
}
