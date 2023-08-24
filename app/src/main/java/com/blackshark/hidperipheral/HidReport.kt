package com.blackshark.hidperipheral

import com.blackshark.hidperipheral.HidReport.DeviceType

class HidReport(var deviceType: DeviceType, var ReportId: Byte, var ReportData: ByteArray) {
    enum class DeviceType {
        None, Mouse, Keyboard
    }
}