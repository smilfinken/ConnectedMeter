package net.smilfinken.meter.collector.model

class Mapper {
    companion object {
        internal fun fromOBIS(obis: String): String {
            when (obis) {
                "1-0:1.8.0" -> return "Mätarställning aktiv energi (uttag)"
                "1-0:2.8.0" -> return "Mätarställning aktiv energi (inmatning)"
                "1-0:3.8.0" -> return "Mätarställning reaktiv energi (uttag)"
                "1-0:4.8.0" -> return "Mätarställning reaktiv energi (inmatning)"
                "1-0:1.7.0" -> return "Momentan aktiv effekt trefas (uttag)"
                "1-0:2.7.0" -> return "Momentan aktiv effekt trefas (inmatning)"
                "1-0:3.7.0" -> return "Momentan reaktiv effekt trefas (uttag)"
                "1-0:4.7.0" -> return "Momentan reaktiv effekt trefas (inmatning)"
                "1-0:21.7.0" -> return "Aktiv energi L1 (uttag)"
                "1-0:22.7.0" -> return "Aktiv energi L1 (inmatning)"
                "1-0:23.7.0" -> return "Reaktiv energi L1 (uttag)"
                "1-0:24.7.0" -> return "Reaktiv energi L1 (inmatning)"
                "1-0:31.7.0" -> return "Momentan ström L1"
                "1-0:32.7.0" -> return "Momentan spänning L1"
                "1-0:41.7.0" -> return "Aktiv energi L2 (uttag)"
                "1-0:42.7.0" -> return "Aktiv energi L2 (inmatning)"
                "1-0:43.7.0" -> return "Reaktiv energi L2 (uttag)"
                "1-0:44.7.0" -> return "Reaktiv energi L2 (inmatning)"
                "1-0:51.7.0" -> return "Momentan ström L3"
                "1-0:52.7.0" -> return "Momentan spänning L2"
                "1-0:61.7.0" -> return "Aktiv energi L3 (uttag)"
                "1-0:62.7.0" -> return "Aktiv energi L3 (inmatning)"
                "1-0:63.7.0" -> return "Reaktiv energi L3 (uttag)"
                "1-0:64.7.0" -> return "Reaktiv energi L3 (inmatning)"
                "1-0:71.7.0" -> return "Momentan ström L3"
                "1-0:72.7.0" -> return "Momentan spänning L3"
                else -> return "Okänt mätvärde"
            }
        }
    }
}