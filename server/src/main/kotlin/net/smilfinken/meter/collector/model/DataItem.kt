package net.smilfinken.meter.collector.model

data class DataItem(private val data: String) {
    companion object {
        val MATCHER = """([^()]*)\(([^()*]*)\*?([^()]*)\)""".toRegex()
    }

    private var obis: String = ""
    private var value: String = ""
    private var unit: String = ""

    init {
        val match = MATCHER.findAll(data).first()
        if (match.groupValues.size == 4) {
            obis = match.groupValues[1]
            value = match.groupValues[2]
            unit = match.groupValues[3]
        }
    }

    override fun toString(): String {
        return "%s: %s %s".format(Mapper.fromOBIS(obis), value, unit)
    }
}