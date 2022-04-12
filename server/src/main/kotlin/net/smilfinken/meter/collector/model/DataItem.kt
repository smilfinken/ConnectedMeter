package net.smilfinken.meter.collector.model

import net.smilfinken.meter.collector.exceptions.DataParseException
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
class DataItem(
    @Id @GeneratedValue(strategy = IDENTITY) val id: Long,
    @ManyToOne val report: DataReport,
    @Column(nullable = false, updatable = false) val obis: String,
    @Column(nullable = false, updatable = false) val value: Float,
    @Column(nullable = false, updatable = false) val unit: String
) {
    constructor() : this(0, DataReport(), "", 0F, "")

    companion object {
        val MATCHER = """([^()]*)\(([^()*]*)\*?([^()]*)\)""".toRegex()

        internal fun parseData(data: String, report: DataReport): DataItem {
            val match = MATCHER.findAll(data).first()
            if (match.groupValues.size == 4) {
                val obis = match.groupValues[1]
                val value = match.groupValues[2].toFloat()
                val unit = match.groupValues[3]
                return DataItem(0, report, obis, value, unit)
            }
            throw DataParseException()
        }
    }

    override fun toString(): String {
        return "%s: %s %s".format(Mapper.fromOBIS(obis), value, unit)
    }
}