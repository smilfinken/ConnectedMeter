package net.smilfinken.meter.collector.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.OneToOne

@Entity
class Temperature(
    @Id @GeneratedValue(strategy = IDENTITY) val id: Long,
    @OneToOne val report: DataReport,
    @Column(nullable = false, updatable = false) val value: Float,
    @Column(nullable = false, updatable = false) val source: String
) {
    constructor() : this(0, DataReport(), 0F, "")
}