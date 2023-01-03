package net.smilfinken.meter.collector.model

import net.smilfinken.meter.collector.util.Dater
import java.util.Date
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import javax.persistence.Temporal
import javax.persistence.TemporalType.TIMESTAMP

@MappedSuperclass
open class PeriodicData(
    @Id @GeneratedValue(strategy = IDENTITY) val id: Long,
    @Column(nullable = false, updatable = false) @Temporal(TIMESTAMP) val timestamp: Date,
    @Column(nullable = false, updatable = false) val output: Float,
    @Column(nullable = false, updatable = false) val intake: Float,
    @Column(nullable = false, updatable = false) val produced: Float,
    @Column(nullable = false, updatable = false) val outdoorTemp: Float,
    @Column(nullable = false, updatable = false) val indoorTemp: Float
) {
    private val name = this.javaClass.simpleName

    constructor() : this(0, Dater.nowDate(), 0F, 0F, 0F, 0F, 0F)

    override fun toString(): String {
        return "$name @ $timestamp: $output, $intake, $produced, $outdoorTemp, $indoorTemp"
    }
}