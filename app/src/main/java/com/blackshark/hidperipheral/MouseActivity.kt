package com.blackshark.hidperipheral

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blackshark.hidperipheral.databinding.ActivityMouseBinding

class MouseActivity : AppCompatActivity() {
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        Toast.makeText(this@MouseActivity, "onWindowFocusChanged $hasFocus", Toast.LENGTH_SHORT).show()
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            val ma = findViewById<TextView>(R.id.move_area)
            ma?.requestPointerCapture()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMouseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mouseUtils = MouseUtils()
        binding.mousePad.setOnTouchListener { _, event -> mouseUtils.mouseMove(event) }
        binding.btnLeft.setOnTouchListener { _, event -> mouseUtils.mouseLeft(event) }
        binding.btnRight.setOnTouchListener { _, event -> mouseUtils.mouseRight(event) }
        binding.middlePad.setOnTouchListener { _, event -> mouseUtils.mouseMiddle(event) }
        binding.mousePad.setOnGenericMotionListener { _, motionEvent ->
            binding.moveArea.text = "$motionEvent"
            false
        }
        binding.mousePad.focusable = View.FOCUSABLE
        binding.mousePad.setOnFocusChangeListener { view, b ->
            Toast.makeText(this@MouseActivity, "setOnFocusChangeListener", Toast.LENGTH_SHORT).show()
            view.requestFocus()
        }
        binding.mousePad.setOnCapturedPointerListener { _, motionEvent ->
            binding.moveArea.text = "$motionEvent"

            if (motionEvent.action == MotionEvent.ACTION_SCROLL) {
                val wheel = motionEvent.getAxisValue(MotionEvent.AXIS_VSCROLL)
                binding.moveArea.text = "${binding.moveArea.text} wheel:$wheel  ${motionEvent.getAxisValue(MotionEvent.AXIS_VSCROLL)}"
                when {
                    0 < wheel -> {
                        binding.moveArea.text = "${binding.moveArea.text} up"
                        //binding.moveArea.text = keyCode2usageStr(KeyEvent.KEYCODE_VOLUME_UP)
                        HidConsts.inputReportQueue.offer(HidReport(HidReport.DeviceType.Consumer, byteArrayOf(0x20)))
                        HidConsts.inputReportQueue.offer(HidReport(HidReport.DeviceType.Consumer, byteArrayOf(0x00)))
                    }

                    wheel < 0 -> {
                        binding.moveArea.text = "${binding.moveArea.text} dn"
                        //binding.moveArea.text = keyCode2usageStr(KeyEvent.KEYCODE_VOLUME_DOWN)
                        HidConsts.inputReportQueue.offer(HidReport(HidReport.DeviceType.Consumer, byteArrayOf(0x40)))
                        HidConsts.inputReportQueue.offer(HidReport(HidReport.DeviceType.Consumer, byteArrayOf(0x00)))
                    }
                }
            }
            true
        }
    }
}