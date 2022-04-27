package net.smilfinken.meter.collector.model

import net.smilfinken.meter.collector.util.Dater.Companion.nowDate
import java.util.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.Temporal
import javax.persistence.TemporalType.TIMESTAMP

@Entity(name = "dataReport")
class DataReport(
    @Id @GeneratedValue(strategy = IDENTITY) val id: Long,
    @Column(nullable = false, updatable = false) @Temporal(TIMESTAMP) val timestamp: Date,
    @Column(nullable = false, updatable = false) @Temporal(TIMESTAMP) val receivedTimestamp: Date
) {
    constructor() : this(0, nowDate(), nowDate())
}
