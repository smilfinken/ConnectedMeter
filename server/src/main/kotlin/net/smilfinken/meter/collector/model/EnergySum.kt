package net.smilfinken.meter.collector.model

import java.util.Date

class EnergySum(val fromTimestamp: Date, val toTimestamp: Date, val obis: String, val value: Double)