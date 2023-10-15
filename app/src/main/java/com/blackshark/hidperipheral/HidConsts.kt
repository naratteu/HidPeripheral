package com.blackshark.hidperipheral

import android.text.TextUtils
import androidx.core.math.MathUtils
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

object HidConsts {
    val inputReportQueue = ConcurrentLinkedQueue<HidReport>()
    private var ModifierByte: Byte = 0x00
    private var KeyByte: Byte = 0x00
    fun cleanKbd() = sendKeyReport(byteArrayOf(0, 0))

    private fun sendMouseReport(reportData: ByteArray) = inputReportQueue.offer(HidReport(HidReport.DeviceType.Mouse, reportData))

    private val MouseReport = HidReport(HidReport.DeviceType.Mouse, byteArrayOf(0, 0, 0, 0))
    fun mouseMove(dx: Int, dy: Int, wheel: Int, leftButton: Boolean, rightButton: Boolean, middleButton: Boolean) {
        val dx = MathUtils.clamp(dx, -127, 127)
        val dy = MathUtils.clamp(dy, -127, 127)
        val wheel = MathUtils.clamp(wheel, -127, 127)
        MouseReport.ReportData[0] = if (leftButton) MouseReport.ReportData[0] or 1 else MouseReport.ReportData[0] and 1.inv()
        MouseReport.ReportData[0] = if (rightButton) MouseReport.ReportData[0] or 2 else MouseReport.ReportData[0] and 2.inv()
        MouseReport.ReportData[0] = if (middleButton) MouseReport.ReportData[0] or 4 else MouseReport.ReportData[0] and 4.inv()
        MouseReport.ReportData[1] = dx.toByte()
        MouseReport.ReportData[2] = dy.toByte()
        MouseReport.ReportData[3] = wheel.toByte()
        inputReportQueue.offer(MouseReport)
    }

    fun leftBtnDown() {
        MouseReport.ReportData[0] = MouseReport.ReportData[0] or 1
        sendMouseReport(MouseReport.ReportData)
    }

    fun leftBtnUp() {
        MouseReport.ReportData[0] = MouseReport.ReportData[0] and 1.inv()
        sendMouseReport(MouseReport.ReportData)
    }

    fun leftBtnClick() {
        leftBtnDown()
        UtilCls.DelayTask({ leftBtnUp() }, 20)
    }

    fun leftBtnClickAsync(delay: Int): TimerTask {
        return UtilCls.DelayTask({ leftBtnClick() }, delay)
    }

    fun rightBtnDown() {
        MouseReport.ReportData[0] = MouseReport.ReportData[0] or 2
        sendMouseReport(MouseReport.ReportData)
    }

    fun rightBtnUp() {
        MouseReport.ReportData[0] = MouseReport.ReportData[0] and 2.inv()
        sendMouseReport(MouseReport.ReportData)
    }

    fun midBtnDown() {
        MouseReport.ReportData[0] = MouseReport.ReportData[0] or 4
        sendMouseReport(MouseReport.ReportData)
    }

    fun midBtnUp() {
        MouseReport.ReportData[0] = MouseReport.ReportData[0] and 4.inv()
        sendMouseReport(MouseReport.ReportData)
    }


    fun kbdKeyDown(usageStr: String) {
        if (!TextUtils.isEmpty(usageStr)) {
            if (usageStr.startsWith("M")) {
                val UsageId = usageStr.substring(1).toInt().toByte()
                synchronized(HidConsts::class.java) {
                    ModifierByte = ModifierByte or UsageId
                    sendKeyReport(byteArrayOf(ModifierByte, KeyByte))
                }
            } else {
                synchronized(HidConsts::class.java) {
                    KeyByte = usageStr.toInt().toByte()
                    sendKeyReport(byteArrayOf(ModifierByte, KeyByte))
                }
            }
        }
    }

    fun kbdKeyUp(usageStr: String) {
        if (!TextUtils.isEmpty(usageStr)) {
            if (usageStr.startsWith("M")) {
                val UsageId = usageStr.substring(1).toInt().toByte()
                synchronized(HidConsts::class.java) {
                    ModifierByte = ModifierByte and UsageId.inv()
                    sendKeyReport(byteArrayOf(ModifierByte, KeyByte))
                }
            } else {
                synchronized(HidConsts::class.java) {
                    KeyByte = 0
                    sendKeyReport(byteArrayOf(ModifierByte, KeyByte))
                }
            }
        }
    }

    fun sendKeyReport(reportData: ByteArray) = inputReportQueue.offer(HidReport(HidReport.DeviceType.Keyboard, reportData))

    @JvmField
    val Descriptor = byteArrayOf(
        0x05.toByte(), 0x01.toByte(), // USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x02.toByte(), // USAGE (Mouse)
        0xa1.toByte(), 0x01.toByte(), // COLLECTION (Application)
            0x09.toByte(), 0x01.toByte(), // USAGE (Pointer)
            0xa1.toByte(), 0x00.toByte(), // COLLECTION (Physical)
                0x85.toByte(), 0x01.toByte(), // REPORT_ID (1)
                0x05.toByte(), 0x09.toByte(), // USAGE_PAGE (Button)
                0x19.toByte(), 0x01.toByte(), // USAGE_MINIMUM (Button 1)
                0x29.toByte(), 0x03.toByte(), // USAGE_MAXIMUM (Button 3)
                0x15.toByte(), 0x00.toByte(), // LOGICAL_MINIMUM (0)
                0x25.toByte(), 0x01.toByte(), // LOGICAL_MAXIMUM (1)
                0x95.toByte(), 0x03.toByte(), // REPORT_COUNT (3)
                    0x75.toByte(), 0x01.toByte(), // REPORT_SIZE (1)
                    0x81.toByte(), 0x02.toByte(), // INPUT (Data,Var,Abs)
                    0x95.toByte(), 0x01.toByte(), // REPORT_COUNT (1)
                    0x75.toByte(), 0x05.toByte(), // REPORT_SIZE (5)
                    0x81.toByte(), 0x03.toByte(), // INPUT (Cnst,Var,Abs)
                    0x05.toByte(), 0x01.toByte(), // USAGE_PAGE (Generic Desktop)
                    0x09.toByte(), 0x30.toByte(), // USAGE (X)
                    0x09.toByte(), 0x31.toByte(), // USAGE (Y)
                    0x09.toByte(), 0x38.toByte(), // USAGE (Wheel)
                    0x15.toByte(), 0x81.toByte(), // LOGICAL_MINIMUM (-127)
                    0x25.toByte(), 0x7f.toByte(), // LOGICAL_MAXIMUM (127)
                    0x75.toByte(), 0x08.toByte(), // REPORT_SIZE (8)
                    0x95.toByte(), 0x03.toByte(), // REPORT_COUNT (3)
                    0x81.toByte(), 0x06.toByte(), // INPUT (Data,Var,Rel)
            0xc0.toByte(),
        0xc0.toByte(),
        0x05.toByte(), 0x01.toByte(),
        0x09.toByte(), 0x06.toByte(),
        0xa1.toByte(), 0x01.toByte(),
        0x85.toByte(), 0x02.toByte(),
        0x05.toByte(), 0x07.toByte(),
        0x19.toByte(), 0xE0.toByte(),
        0x29.toByte(), 0xE7.toByte(),
        0x15.toByte(), 0x00.toByte(),
        0x25.toByte(), 0x01.toByte(),
        0x75.toByte(), 0x01.toByte(),
        0x95.toByte(), 0x08.toByte(),
        0x81.toByte(), 0x02.toByte(),
        0x95.toByte(), 0x01.toByte(),
        0x75.toByte(), 0x08.toByte(),
        0x15.toByte(), 0x00.toByte(),
        0x25.toByte(), 0x65.toByte(),
        0x19.toByte(), 0x00.toByte(),
        0x29.toByte(), 0x65.toByte(),
        0x81.toByte(), 0x00.toByte(),
        0x05.toByte(), 0x08.toByte(),
        0x95.toByte(), 0x05.toByte(),
        0x75.toByte(), 0x01.toByte(),
        0x19.toByte(), 0x01.toByte(),
        0x29.toByte(), 0x05.toByte(),
        0x91.toByte(), 0x02.toByte(),
        0x95.toByte(), 0x01.toByte(),
        0x75.toByte(), 0x03.toByte(),
        0x91.toByte(), 0x03.toByte(),
        0xc0.toByte(),

        //https://www.espruino.com/modules/ble_hid_controls.js
        0x05.toByte(), 0x0c.toByte(), // USAGE_PAGE (Consumer Devices)
        0x09.toByte(), 0x01.toByte(), // USAGE (Consumer Control)
        0xa1.toByte(), 0x01.toByte(), // COLLECTION (Application)
        0x15.toByte(), 0x00.toByte(), //   LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x01.toByte(), //   LOGICAL_MAXIMUM (1)
        0x85.toByte(), 0x03.toByte(), //   REPORT_ID (3)
        0x75.toByte(), 0x01.toByte(), //   REPORT_SIZE (1)    - each field occupies 1 bit
        0x95.toByte(), 0x05.toByte(), //   REPORT_COUNT (5)
        0x09.toByte(), 0xb5.toByte(), //   USAGE (Scan Next Track)
        0x09.toByte(), 0xb6.toByte(), //   USAGE (Scan Previous Track)
        0x09.toByte(), 0xb7.toByte(), //   USAGE (Stop)
        0x09.toByte(), 0xcd.toByte(), //   USAGE (Play/Pause)
        0x09.toByte(), 0xe2.toByte(), //   USAGE (Mute)
        0x81.toByte(), 0x06.toByte(), //   INPUT (Data,Var,Rel)  - relative inputs
        0x95.toByte(), 0x02.toByte(), //   REPORT_COUNT (2)
        0x09.toByte(), 0xe9.toByte(), //   USAGE (Volume Up)
        0x09.toByte(), 0xea.toByte(), //   USAGE (Volume Down)
        0x81.toByte(), 0x02.toByte(), //   INPUT (Data,Var,Abs)  - absolute inputs
        0x95.toByte(), 0x01.toByte(), //   REPORT_COUNT (1)
        0x81.toByte(), 0x01.toByte(), //   INPUT (Cnst,Ary,Abs)
        0xc0.toByte() // END_COLLECTION
    )
}