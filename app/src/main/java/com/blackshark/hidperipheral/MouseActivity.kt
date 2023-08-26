package com.blackshark.hidperipheral

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blackshark.hidperipheral.databinding.ActivityMouseBinding

class MouseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMouseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mouseUtils = MouseUtils()
        binding.mousePad.setOnTouchListener { _, event -> mouseUtils.mouseMove(event) }
        binding.btnLeft.setOnTouchListener { _, event -> mouseUtils.mouseLeft(event) }
        binding.btnRight.setOnTouchListener { _, event -> mouseUtils.mouseRight(event) }
        binding.middlePad.setOnTouchListener { _, event -> mouseUtils.mouseMiddle(event) }
    }
}