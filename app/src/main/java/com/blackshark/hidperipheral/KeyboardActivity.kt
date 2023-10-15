package com.blackshark.hidperipheral

import android.annotation.SuppressLint
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

    override fun onDestroy() {
        HidConsts.cleanKbd() //페이지를 나가기 직전에 특정 키가 눌리면 무한히 눌린 상태가 되는 문제가 있어 나가기 전에 클리어
        super.onDestroy()
    }
}