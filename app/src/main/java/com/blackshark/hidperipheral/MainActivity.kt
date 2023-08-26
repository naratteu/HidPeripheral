package com.blackshark.hidperipheral

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothProfile.ServiceListener
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.blackshark.hidperipheral.databinding.ActivityMainBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

@OptIn(DelicateCoroutinesApi::class)
class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    val hashmap = ConcurrentHashMap<BluetoothDevice, Boolean>()
    val hashmap2 = ConcurrentHashMap<BluetoothDevice, Int>()

    var bluetoothPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            if (isEnableBluetooth()) {
                showToast(R.string.toast_bluetooth_on)
            } else {
                showToast(R.string.toast_bluetooth_off)
            }
        }
    }

    var discoverPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        Log.d(TAG, ": ${it.resultCode}")
        if (it.resultCode == 120) {
                val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                bluetoothAdapter.name = "Peripheral MK"
                bluetoothAdapter.getProfileProxy(applicationContext, object : ServiceListener {
                    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                        Log.e(TAG, "hid onServiceConnected ${profile} {proxy}")
                            val mHidDevice = proxy as BluetoothHidDevice
                            mHidDevice?.registerApp(
                                BluetoothHidDeviceAppSdpSettings("BS-HID-Peripheral", "fac", "funny", BluetoothHidDevice.SUBCLASS1_COMBO, HidConsts.Descriptor),
                                null, null, Executors.newCachedThreadPool(),
                                object : BluetoothHidDevice.Callback() {
                                    override fun onAppStatusChanged(pluggedDevice: BluetoothDevice, registered: Boolean) {
                                        Log.e(TAG, "onAppStatusChanged: $registered")
                                        hashmap[pluggedDevice] = registered
                                        mHidDevice.connect(pluggedDevice)
                                        GlobalScope.launch(Dispatchers.Main) {
                                            binding.tvConnectStatus.text = "${hashmap} : ${hashmap2}"
                                        }
                                    }
                                    override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
                                        Log.e(TAG, "onConnectionStateChanged:$state")
                                        hashmap2[device] = state
                                        GlobalScope.launch(Dispatchers.Main) {
                                            binding.tvConnectStatus.text = "${hashmap} : ${hashmap2}"
                                            HidConsts.cleanKbd()
                                        }
                                        when (state) {
                                            BluetoothProfile.STATE_CONNECTED -> {
                                                GlobalScope.launch(Dispatchers.Main) {
                                                    HidConsts.cleanKbd()
                                                }
                                                Timer().scheduleAtFixedRate(object : TimerTask() {
                                                    override fun run() {
                                                        HidConsts.inputReportQueue.poll()?.run {
                                                            mHidDevice?.sendReport(device, ReportId.toInt(), ReportData)
                                                        }
                                                    }
                                                }, 0, 5)
                                            }
                                        }
                                    }
                                }
                            )
                    }
                    override fun onServiceDisconnected(profile: Int) {
                        Log.e(TAG, "hid onServiceDisconnected ${profile}")
                    }
                }, BluetoothProfile.HID_DEVICE)
        }
    }
    var connectPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (!it) {
            showToast(R.string.toast_permission)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStart.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 31 && !hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                connectPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
                return@setOnClickListener
            }
            if (!isEnableBluetooth()) {
                bluetoothPermission.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                return@setOnClickListener
            }
            discoverPermission.launch(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE))
        }

        binding.btnMouse.setOnClickListener {
                startActivity(Intent(this, MouseActivity::class.java))
        }

        binding.btnKeyboard.setOnClickListener {
                startActivity(Intent(this, KeyboardActivity::class.java))
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        binding.tvConnectStatus.text = "$event"
        when (event?.action) {
            ACTION_DOWN -> HidConsts.kbdKeyDown(keyCode2usageStr(event.keyCode))
            ACTION_UP -> HidConsts.kbdKeyUp(keyCode2usageStr(event.keyCode))
            else -> return true
        }
        return false
    }

    private fun keyCode2usageStr(keyCode: Int) = when (keyCode) {
        KEYCODE_ESCAPE -> "41"
        KEYCODE_F1 -> "58"
        KEYCODE_F2 -> "59"
        KEYCODE_F3 -> "60"
        KEYCODE_F4 -> "61"
        KEYCODE_F5 -> "62"
        KEYCODE_F6 -> "63"
        KEYCODE_F7 -> "64"
        KEYCODE_F8 -> "65"
        KEYCODE_F9 -> "66"
        KEYCODE_F10 -> "67"
        KEYCODE_F11 -> "68"
        KEYCODE_F12 -> "69"
        KEYCODE_FORWARD_DEL -> "76"
        KEYCODE_MOVE_END -> "77"
        KEYCODE_GRAVE -> "53" //"`  ~"
        KEYCODE_1 -> "30"
        KEYCODE_2 -> "31"
        KEYCODE_3 -> "32"
        KEYCODE_4 -> "33"
        KEYCODE_5 -> "34"
        KEYCODE_6 -> "35"
        KEYCODE_7 -> "36"
        KEYCODE_8 -> "37"
        KEYCODE_9 -> "38"
        KEYCODE_0 -> "39"
        KEYCODE_MINUS -> "45"
        KEYCODE_EQUALS -> "46" //"=  +"
        KEYCODE_DEL -> "42" //"Backspace"
        KEYCODE_TAB -> "43"
        KEYCODE_Q -> "20"
        KEYCODE_W -> "26"
        KEYCODE_E -> "8"
        KEYCODE_R -> "21"
        KEYCODE_T -> "23"
        KEYCODE_Y -> "28"
        KEYCODE_U -> "24"
        KEYCODE_I -> "12"
        KEYCODE_O -> "18"
        KEYCODE_P -> "19"
        KEYCODE_LEFT_BRACKET -> "47" //"[  {"
        KEYCODE_RIGHT_BRACKET -> "48" //"]  }"
        KEYCODE_BACKSLASH -> "49" // \ or |
        KEYCODE_CAPS_LOCK -> "57"
        KEYCODE_A -> "4"
        KEYCODE_S -> "22"
        KEYCODE_D -> "7"
        KEYCODE_F -> "9"
        KEYCODE_G -> "10"
        KEYCODE_H -> "11"
        KEYCODE_J -> "13"
        KEYCODE_K -> "14"
        KEYCODE_L -> "15"
        KEYCODE_SEMICOLON -> "51" //";  :"
        KEYCODE_APOSTROPHE -> "52" // ' or "
        KEYCODE_ENTER -> "40"
        KEYCODE_SHIFT_LEFT -> "M2"
        KEYCODE_Z -> "29"
        KEYCODE_X -> "27"
        KEYCODE_C -> "6"
        KEYCODE_V -> "25"
        KEYCODE_B -> "5"
        KEYCODE_N -> "17"
        KEYCODE_M -> "16"
        KEYCODE_COMMA -> "54"
        KEYCODE_PERIOD -> "55"
        KEYCODE_SLASH -> "56"
        KEYCODE_SHIFT_RIGHT -> "M32"
        KEYCODE_CTRL_LEFT -> "M1"
        KEYCODE_WINDOW -> "M8"
        KEYCODE_ALT_LEFT -> "M4"
        KEYCODE_SPACE -> "44"
        KEYCODE_ALT_RIGHT -> "M4"
        KEYCODE_MENU -> "101"
        KEYCODE_SYSRQ -> "70" //Prt
        KEYCODE_CTRL_RIGHT -> "M16"
        KEYCODE_INSERT -> "75"
        KEYCODE_DPAD_UP -> "82"
        KEYCODE_PAGE_UP -> "73"
        KEYCODE_PAGE_DOWN -> "78"
        KEYCODE_MOVE_HOME -> "74"
        KEYCODE_SCROLL_LOCK -> "71"
        KEYCODE_BREAK -> "71"
        KEYCODE_NUM_LOCK -> "83"
        KEYCODE_NUMPAD_DIVIDE -> "84"
        KEYCODE_NUMPAD_MULTIPLY -> "85"
        KEYCODE_NUMPAD_SUBTRACT -> "86"
        KEYCODE_NUMPAD_ADD -> "87"
        KEYCODE_DPAD_LEFT -> "80"
        KEYCODE_DPAD_DOWN -> "81"
        KEYCODE_DPAD_RIGHT -> "79"
        KEYCODE_NUMPAD_1 -> "89"
        KEYCODE_NUMPAD_2 -> "90"
        KEYCODE_NUMPAD_3 -> "91"
        KEYCODE_NUMPAD_4 -> "93"
        KEYCODE_NUMPAD_5 -> "93"
        KEYCODE_NUMPAD_6 -> "94"
        KEYCODE_NUMPAD_7 -> "95"
        KEYCODE_NUMPAD_8 -> "96"
        KEYCODE_NUMPAD_9 -> "97"
        KEYCODE_NUMPAD_ENTER -> "88"
        else -> ""
    }
}