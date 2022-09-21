# HidPeripheral
> Simulation Bluetooth HID Device(Mouse/Keyboard) for Android，Support 「Android、iOS、Windows...」 

### Register HID Device

```java
BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
bluetoothAdapter.getProfileProxy(context, mProfileServiceListener, BluetoothProfile.HID_DEVICE);

// SUBCLASS1_COMBO = MOUSE+KEYBOARD
BluetoothHidDeviceAppSdpSettings sdp = new BluetoothHidDeviceAppSdpSettings(HidConsts.NAME, HidConsts.DESCRIPTION, HidConsts.PROVIDER, BluetoothHidDevice.SUBCLASS1_COMBO, HidConsts.Descriptor);
mHidDevice.registerApp(sdp, null, null, Executors.newCachedThreadPool(), mCallback);
```

### iOS Device(Need to open the AssistiveTouch)
**How to open AssistiveTouch?**
System Setting -> Accessibility -> Touch -> AssistiveTouch

### Screenshots
![Mouse](screentshots/mouse.jpg)
![Keyboard](screentshots/keyboard.jpg)