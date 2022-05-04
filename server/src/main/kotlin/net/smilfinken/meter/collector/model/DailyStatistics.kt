package net.smilfinken.meter.collector.model

data class DailyStatistics(val date: String, val production: Float, val intake: Float, val output: Float) {
    val balance = output - intake
    val result = if (output >= intake) {
        "win"
    } else {
        "fail"
    }

    override fun toString(): String = "$date: $output - $intake = $production"
}