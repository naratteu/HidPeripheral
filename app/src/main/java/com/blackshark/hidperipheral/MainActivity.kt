package com.blackshark.hidperipheral

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothProfile.ServiceListener
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.blackshark.hidperipheral.databinding.ActivityMainBinding
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    val hashmap = ConcurrentHashMap<BluetoothDevice, Boolean>()
    val hashmap2 = ConcurrentHashMap<BluetoothDevice, Int>()

    val bluetoothPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { Start() }

    val discoverPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        Log.d(TAG, ": ${it.resultCode}")
    }

    fun Start() {
        if (Build.VERSION.SDK_INT >= 31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            connectPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
            return
        }
        val manager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager?
        val bluetoothAdapter = manager?.adapter
        if (bluetoothAdapter == null) {
            bluetoothPermission.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            return
        }
        bluetoothAdapter.name = "Peripheral MK"
        bluetoothAdapter.getProfileProxy(applicationContext, object : ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                Log.e(TAG, "hid onServiceConnected ${profile} ${proxy}")
                val mHidDevice = proxy as BluetoothHidDevice?
                mHidDevice?.registerApp(
                    BluetoothHidDeviceAppSdpSettings("BS-HID-Peripheral", "fac", "funny", BluetoothHidDevice.SUBCLASS1_COMBO, HidConsts.Descriptor),
                    null, null, Executors.newCachedThreadPool(),
                    object : BluetoothHidDevice.Callback() {
                        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
                            if (pluggedDevice != null) {
                                Log.e(TAG, "onAppStatusChanged: $registered")
                                hashmap[pluggedDevice] = registered
                                mHidDevice.connect(pluggedDevice)
                            }
                            runOnUiThread {
                                binding.tvConnectStatus.text = "${hashmap} \n ${hashmap2} \n ${bluetoothAdapter.bondedDevices} \n ${mHidDevice.connectedDevices}"
                            }
                        }

                        override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
                            Log.e(TAG, "onConnectionStateChanged:$state")
                            hashmap2[device] = state
                            runOnUiThread {
                                binding.tvConnectStatus.text = "${hashmap} \n ${hashmap2} \n ${bluetoothAdapter.bondedDevices} \n ${mHidDevice.connectedDevices}"
                            }
                            when (state) {
                                BluetoothProfile.STATE_CONNECTED -> {
                                    HidConsts.cleanKbd()
                                    Timer().scheduleAtFixedRate(object : TimerTask() {
                                        override fun run() {
                                            HidConsts.inputReportQueue.poll()?.run {
                                                mHidDevice.sendReport(device, ReportId, ReportData)
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

    val connectPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { Start() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStart.setOnClickListener {
            discoverPermission.launch(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE))
        }

        binding.btnMouse.setOnClickListener {
            startActivity(Intent(this, MouseActivity::class.java))
        }

        binding.btnKeyboard.setOnClickListener {
            startActivity(Intent(this, KeyboardActivity::class.java))
        }

        Start()
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
        KEYCODE_ESCAPE -> "${kHIDUsage.KeyboardEscape}"
        KEYCODE_F1 -> "${kHIDUsage.KeyboardF1}"
        KEYCODE_F2 -> "${kHIDUsage.KeyboardF2}"
        KEYCODE_F3 -> "${kHIDUsage.KeyboardF3}"
        KEYCODE_F4 -> "${kHIDUsage.KeyboardF4}"
        KEYCODE_F5 -> "${kHIDUsage.KeyboardF5}"
        KEYCODE_F6 -> "${kHIDUsage.KeyboardF6}"
        KEYCODE_F7 -> "${kHIDUsage.KeyboardF7}"
        KEYCODE_F8 -> "${kHIDUsage.KeyboardF8}"
        KEYCODE_F9 -> "${kHIDUsage.KeyboardF9}"
        KEYCODE_F10 -> "${kHIDUsage.KeyboardF10}"
        KEYCODE_F11 -> "${kHIDUsage.KeyboardF11}"
        KEYCODE_F12 -> "${kHIDUsage.KeyboardF12}"
        KEYCODE_FORWARD_DEL -> "${kHIDUsage.KeyboardDeleteForward}"
        KEYCODE_MOVE_END -> "${kHIDUsage.KeyboardEnd}"
        KEYCODE_GRAVE -> "${kHIDUsage.KeyboardGraveAccentAndTilde}" //"`  ~"
        KEYCODE_1 -> "${kHIDUsage.Keyboard1}"
        KEYCODE_2 -> "${kHIDUsage.Keyboard2}"
        KEYCODE_3 -> "${kHIDUsage.Keyboard3}"
        KEYCODE_4 -> "${kHIDUsage.Keyboard4}"
        KEYCODE_5 -> "${kHIDUsage.Keyboard5}"
        KEYCODE_6 -> "${kHIDUsage.Keyboard6}"
        KEYCODE_7 -> "${kHIDUsage.Keyboard7}"
        KEYCODE_8 -> "${kHIDUsage.Keyboard8}"
        KEYCODE_9 -> "${kHIDUsage.Keyboard9}"
        KEYCODE_0 -> "${kHIDUsage.Keyboard0}"
        KEYCODE_MINUS -> "${kHIDUsage.KeyboardHyphen}"
        KEYCODE_EQUALS -> "${kHIDUsage.KeyboardEqualSign}" //"=  +"
        KEYCODE_DEL -> "${kHIDUsage.KeyboardDeleteOrBackspace}" //"Backspace"
        KEYCODE_TAB -> "${kHIDUsage.KeyboardTab}"
        KEYCODE_Q -> "${kHIDUsage.KeyboardQ}"
        KEYCODE_W -> "${kHIDUsage.KeyboardW}"
        KEYCODE_E -> "${kHIDUsage.KeyboardE}"
        KEYCODE_R -> "${kHIDUsage.KeyboardR}"
        KEYCODE_T -> "${kHIDUsage.KeyboardT}"
        KEYCODE_Y -> "${kHIDUsage.KeyboardY}"
        KEYCODE_U -> "${kHIDUsage.KeyboardU}"
        KEYCODE_I -> "${kHIDUsage.KeyboardI}"
        KEYCODE_O -> "${kHIDUsage.KeyboardO}"
        KEYCODE_P -> "${kHIDUsage.KeyboardP}"
        KEYCODE_LEFT_BRACKET -> "${kHIDUsage.KeyboardOpenBracket}" //"[  {"
        KEYCODE_RIGHT_BRACKET -> "${kHIDUsage.KeyboardCloseBracket}" //"]  }"
        KEYCODE_BACKSLASH -> "${kHIDUsage.KeyboardBackslash}" // \ or |
        KEYCODE_CAPS_LOCK -> "${kHIDUsage.KeyboardCapsLock}"
        KEYCODE_A -> "${kHIDUsage.KeyboardA}"
        KEYCODE_S -> "${kHIDUsage.KeyboardS}"
        KEYCODE_D -> "${kHIDUsage.KeyboardD}"
        KEYCODE_F -> "${kHIDUsage.KeyboardF}"
        KEYCODE_G -> "${kHIDUsage.KeyboardG}"
        KEYCODE_H -> "${kHIDUsage.KeyboardH}"
        KEYCODE_J -> "${kHIDUsage.KeyboardJ}"
        KEYCODE_K -> "${kHIDUsage.KeyboardK}"
        KEYCODE_L -> "${kHIDUsage.KeyboardL}"
        KEYCODE_SEMICOLON -> "${kHIDUsage.KeyboardSemicolon}" //";  :"
        KEYCODE_APOSTROPHE -> "${kHIDUsage.KeyboardQuote}" // ' or "
        KEYCODE_ENTER -> "${kHIDUsage.KeyboardReturnOrEnter}"
        KEYCODE_SHIFT_LEFT -> "M2"
        KEYCODE_Z -> "${kHIDUsage.KeyboardZ}"
        KEYCODE_X -> "${kHIDUsage.KeyboardX}"
        KEYCODE_C -> "${kHIDUsage.KeyboardC}"
        KEYCODE_V -> "${kHIDUsage.KeyboardV}"
        KEYCODE_B -> "${kHIDUsage.KeyboardB}"
        KEYCODE_N -> "${kHIDUsage.KeyboardN}"
        KEYCODE_M -> "${kHIDUsage.KeyboardM}"
        KEYCODE_COMMA -> "${kHIDUsage.KeyboardComma}"
        KEYCODE_PERIOD -> "${kHIDUsage.KeyboardPeriod}"
        KEYCODE_SLASH -> "${kHIDUsage.KeyboardSlash}"
        KEYCODE_SHIFT_RIGHT -> "M32"
        KEYCODE_CTRL_LEFT -> "M1"
        KEYCODE_WINDOW -> "M8"
        KEYCODE_ALT_LEFT -> "M4"
        KEYCODE_SPACE -> "${kHIDUsage.KeyboardSpacebar}"
        KEYCODE_ALT_RIGHT -> "M4"
        KEYCODE_MENU -> "${kHIDUsage.KeyboardApplication}"
        KEYCODE_SYSRQ -> "${kHIDUsage.KeyboardPrintScreen}" //Prt
        KEYCODE_CTRL_RIGHT -> "M16"
        KEYCODE_INSERT -> "${kHIDUsage.KeyboardPageUp}"
        KEYCODE_DPAD_UP -> "${kHIDUsage.KeyboardUpArrow}"
        KEYCODE_PAGE_UP -> "${kHIDUsage.KeyboardInsert}"
        KEYCODE_PAGE_DOWN -> "${kHIDUsage.KeyboardPageDown}"
        KEYCODE_MOVE_HOME -> "${kHIDUsage.KeyboardHome}"
        KEYCODE_SCROLL_LOCK -> "${kHIDUsage.KeyboardScrollLock}"
        KEYCODE_BREAK -> "${kHIDUsage.KeyboardPause}"
        KEYCODE_NUM_LOCK -> "${kHIDUsage.KeypadNumLock}"
        KEYCODE_NUMPAD_DIVIDE -> "${kHIDUsage.KeypadSlash}"
        KEYCODE_NUMPAD_MULTIPLY -> "${kHIDUsage.KeypadAsterisk}"
        KEYCODE_NUMPAD_SUBTRACT -> "${kHIDUsage.KeypadHyphen}"
        KEYCODE_NUMPAD_ADD -> "${kHIDUsage.KeypadPlus}"
        KEYCODE_DPAD_LEFT -> "${kHIDUsage.KeyboardLeftArrow}"
        KEYCODE_DPAD_DOWN -> "${kHIDUsage.KeyboardDownArrow}"
        KEYCODE_DPAD_RIGHT -> "${kHIDUsage.KeyboardRightArrow}"
        KEYCODE_NUMPAD_1 -> "${kHIDUsage.Keypad1}"
        KEYCODE_NUMPAD_2 -> "${kHIDUsage.Keypad2}"
        KEYCODE_NUMPAD_3 -> "${kHIDUsage.Keypad3}"
        KEYCODE_NUMPAD_4 -> "${kHIDUsage.Keypad4}"
        KEYCODE_NUMPAD_5 -> "${kHIDUsage.Keypad5}"
        KEYCODE_NUMPAD_6 -> "${kHIDUsage.Keypad6}"
        KEYCODE_NUMPAD_7 -> "${kHIDUsage.Keypad7}"
        KEYCODE_NUMPAD_8 -> "${kHIDUsage.Keypad8}"
        KEYCODE_NUMPAD_9 -> "${kHIDUsage.Keypad9}"
        KEYCODE_NUMPAD_ENTER -> "${kHIDUsage.KeypadEnter}"
        KEYCODE_VOLUME_UP -> "${kHIDUsage.KeyboardVolumeUp}"
        KEYCODE_VOLUME_DOWN -> "${kHIDUsage.KeyboardVolumeDown}"
        KEYCODE_VOLUME_MUTE -> "${kHIDUsage.KeyboardMute}"
        else -> ""
    }
}