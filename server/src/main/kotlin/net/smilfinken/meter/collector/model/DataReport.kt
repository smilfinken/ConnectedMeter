package net.smilfinken.meter.collector.model

import java.sql.Timestamp
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id

@Entity
class DataReport(
    @Id @GeneratedValue(strategy = IDENTITY) val id: Long,
    @Column(nullable = false, updatable = false) val timestamp: Timestamp,
    @Column(nullable = false, updatable = false) val receivedTimestamp: Timestamp
) {
    constructor() : this(0, Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now()))
}
