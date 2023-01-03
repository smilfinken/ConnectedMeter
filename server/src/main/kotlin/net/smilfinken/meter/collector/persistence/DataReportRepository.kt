package net.smilfinken.meter.collector.persistence

import net.smilfinken.meter.collector.model.DataReport
import net.smilfinken.meter.collector.model.EnergyChartDataItem
import net.smilfinken.meter.collector.model.EnergySum
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.Date

interface DataReportRepository : CrudRepository<DataReport, Long> {
    fun findAllByOrderByTimestampAsc(): List<DataReport>
    fun findAllByReceivedTimestampBetween(start: Date, end: Date): List<DataReport>

    @Query(value = "SELECT new net.smilfinken.meter.collector.model.EnergySum(MIN(r.receivedTimestamp), MAX(r.receivedTimestamp), i.obis, AVG(i.value) * 1000) FROM dataReport r INNER JOIN dataItem i ON i.report = r.id WHERE (r.receivedTimestamp BETWEEN ?1 AND ?2) AND (i.obis = '1-0:1.7.0' OR i.obis = '1-0:2.7.0') GROUP BY i.obis")
    fun sumEnergyBalanceByDate(fromTimestamp: Date, toTimestamp: Date): List<EnergySum>

    @Query(value = "SELECT new net.smilfinken.meter.collector.model.EnergyChartDataItem(MIN(r.receivedTimestamp), AVG(i.value) * 1000) FROM dataReport r INNER JOIN dataItem i ON i.report = r.id WHERE (r.receivedTimestamp BETWEEN ?1 AND ?2) AND (i.obis = '1-0:1.7.0' OR i.obis = '1-0:2.7.0') GROUP BY i.obis")
    fun sumEnergyBalanceByDay(fromTimestamp: Date, toTimestamp: Date): List<EnergyChartDataItem>
}