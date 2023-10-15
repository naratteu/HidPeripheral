package com.blackshark.hidperipheral

class HidReport(var ReportId: Int, var ReportData: ByteArray) {
    object DeviceType {
        val Mouse = 0x01
        val Keyboard = 0x02
        val Consumer = 0x03
    }
}