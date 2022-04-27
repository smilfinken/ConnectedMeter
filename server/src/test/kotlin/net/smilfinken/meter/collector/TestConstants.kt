package net.smilfinken.meter.collector

import java.sql.Timestamp
import java.text.SimpleDateFormat

class TestConstants {
    companion object {
        internal val VALID_DATE: Timestamp = Timestamp.valueOf("2021-02-17 18:40:19.0")
        internal const val VALID_ID = "253833635_A"
        internal const val VALID_CRC = "7945"

        // private const val TEST_MESSAGE_ID = """/ELL5\x5c$VALID_ID"""
        internal const val TEST_MESSAGE_ID = "/ELL5\\$VALID_ID"
        internal val TEST_MESSAGE_DATE = "0-0:1.0.0(${SimpleDateFormat("YYMMddHHmmss").format(VALID_DATE)}W)"

        val TEST_MESSAGE_CONTENT = "" +
                TEST_MESSAGE_ID + "\r\n\r\n" +
                TEST_MESSAGE_DATE + "\r\n" +
                "1-0:1.8.0(00006678.394*kWh)" + "\r\n" +
                "1-0:2.8.0(00000000.000*kWh)" + "\r\n" +
                "1-0:3.8.0(00000021.988*kvarh)" + "\r\n" +
                "1-0:4.8.0(00001020.971*kvarh)" + "\r\n" +
                "1-0:1.7.0(0001.727*kW)" + "\r\n" +
                "1-0:2.7.0(0000.000*kW)" + "\r\n" +
                "1-0:3.7.0(0000.000*kvar)" + "\r\n" +
                "1-0:4.7.0(0000.309*kvar)" + "\r\n" +
                "1-0:21.7.0(0001.023*kW)" + "\r\n" +
                "1-0:41.7.0(0000.350*kW)" + "\r\n" +
                "1-0:61.7.0(0000.353*kW)" + "\r\n" +
                "1-0:22.7.0(0000.000*kW)" + "\r\n" +
                "1-0:42.7.0(0000.000*kW)" + "\r\n" +
                "1-0:62.7.0(0000.000*kW)" + "\r\n" +
                "1-0:23.7.0(0000.000*kvar)" + "\r\n" +
                "1-0:43.7.0(0000.000*kvar)" + "\r\n" +
                "1-0:63.7.0(0000.000*kvar)" + "\r\n" +
                "1-0:24.7.0(0000.009*kvar)" + "\r\n" +
                "1-0:44.7.0(0000.161*kvar)" + "\r\n" +
                "1-0:64.7.0(0000.138*kvar)" + "\r\n" +
                "1-0:32.7.0(240.3*V)" + "\r\n" +
                "1-0:52.7.0(240.1*V)" + "\r\n" +
                "1-0:72.7.0(241.3*V)" + "\r\n" +
                "1-0:31.7.0(004.2*A)" + "\r\n" +
                "1-0:51.7.0(001.6*A)" + "\r\n" +
                "1-0:71.7.0(001.7*A)" + "\r\n" +
                "!"

        val TEST_MESSAGE = TEST_MESSAGE_CONTENT + VALID_CRC
    }
}