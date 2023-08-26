package com.blackshark.hidperipheral

import android.os.Bundle
import android.view.MotionEvent
import android.view.View.OnTouchListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.blackshark.hidperipheral.databinding.ActivityKeyboardBinding

class KeyboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityKeyboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        HidConsts.cleanKbd()
        val onTouchListener = OnTouchListener { v, event ->
            when(event.action)
            {
                MotionEvent.ACTION_DOWN ->  HidConsts.kbdKeyDown(v.tag.toString())
                MotionEvent.ACTION_UP -> HidConsts.kbdKeyUp(v.tag.toString())
            }
            false
        }
        for (i in binding.keysButtons.children) {
            i.setOnTouchListener(onTouchListener)
        }
    }
}