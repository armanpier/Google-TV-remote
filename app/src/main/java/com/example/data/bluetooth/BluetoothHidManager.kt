package com.example.data.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppQosSettings
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executors

enum class BluetoothHidState {
    NOT_SUPPORTED,
    NEED_PERMISSION,
    INITIALIZING,
    READY,
    CONNECTED,
    DISCONNECTED
}

/**
 * Android Bluetooth HID Device Manager.
 * Emulates a real Bluetooth Mouse and Keyboard peripheral so the Sony Bravia
 * and Google TV recognizes the phone as a physical Bluetooth trackpad & keyboard.
 * When connected, Google TV OS natively displays the on-screen mouse cursor!
 *
 * Designed with defensive permission checks so it NEVER crashes if permissions are absent.
 */
class BluetoothHidManager(private val context: Context) {

    private val tag = "BluetoothHidManager"

    private val bluetoothManager: BluetoothManager? = try {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    } catch (_: Throwable) {
        null
    }

    val bluetoothAdapter: BluetoothAdapter? = try {
        bluetoothManager?.adapter
    } catch (_: Throwable) {
        null
    }

    private var hidDevice: BluetoothHidDevice? = null
    private var connectedDevice: BluetoothDevice? = null

    private val _connectionState = MutableStateFlow(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && bluetoothAdapter != null) {
            if (hasBluetoothPermission()) BluetoothHidState.INITIALIZING else BluetoothHidState.NEED_PERMISSION
        } else {
            BluetoothHidState.NOT_SUPPORTED
        }
    )
    val connectionState: StateFlow<BluetoothHidState> = _connectionState.asStateFlow()

    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()

    companion object {
        const val REPORT_ID_MOUSE = 0
    }

    private var mouseButtons: Byte = 0

    // Standard USB/Bluetooth HID Standalone Optical Mouse Descriptor
    // Logitech / Microsoft / Apple industry-standard 3-button boot & report optical mouse
    private val hidReportDescriptor = byteArrayOf(
        0x05.toByte(), 0x01.toByte(),        // USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x02.toByte(),        // USAGE (Mouse)
        0xa1.toByte(), 0x01.toByte(),        // COLLECTION (Application)
        0x09.toByte(), 0x01.toByte(),        //   USAGE (Pointer)
        0xa1.toByte(), 0x00.toByte(),        //   COLLECTION (Physical)
        0x05.toByte(), 0x09.toByte(),        //     USAGE_PAGE (Button)
        0x19.toByte(), 0x01.toByte(),        //     USAGE_MINIMUM (Button 1)
        0x29.toByte(), 0x03.toByte(),        //     USAGE_MAXIMUM (Button 3)
        0x15.toByte(), 0x00.toByte(),        //     LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x01.toByte(),        //     LOGICAL_MAXIMUM (1)
        0x95.toByte(), 0x03.toByte(),        //     REPORT_COUNT (3)
        0x75.toByte(), 0x01.toByte(),        //     REPORT_SIZE (1)
        0x81.toByte(), 0x02.toByte(),        //     INPUT (Data,Var,Abs) - Buttons 1, 2, 3
        0x95.toByte(), 0x01.toByte(),        //     REPORT_COUNT (1)
        0x75.toByte(), 0x05.toByte(),        //     REPORT_SIZE (5)
        0x81.toByte(), 0x03.toByte(),        //     INPUT (Cnst,Var,Abs) - 5-bit padding
        0x05.toByte(), 0x01.toByte(),        //     USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x30.toByte(),        //     USAGE (X)
        0x09.toByte(), 0x31.toByte(),        //     USAGE (Y)
        0x09.toByte(), 0x38.toByte(),        //     USAGE (Wheel)
        0x15.toByte(), 0x81.toByte(),        //     LOGICAL_MINIMUM (-127)
        0x25.toByte(), 0x7f.toByte(),        //     LOGICAL_MAXIMUM (127)
        0x75.toByte(), 0x08.toByte(),        //     REPORT_SIZE (8)
        0x95.toByte(), 0x03.toByte(),        //     REPORT_COUNT (3)
        0x81.toByte(), 0x06.toByte(),        //     INPUT (Data,Var,Rel) - X, Y, Wheel relative
        0xc0.toByte(),                      //   END_COLLECTION
        0xc0.toByte()                       // END_COLLECTION
    )

    private val serviceListener = object : BluetoothProfile.ServiceListener {
        @SuppressLint("MissingPermission")
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            try {
                if (profile == BluetoothProfile.HID_DEVICE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    hidDevice = proxy as? BluetoothHidDevice
                    registerHidApp()
                }
            } catch (e: Throwable) {
                Log.w(tag, "Error in onServiceConnected: ${e.message}")
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            try {
                if (profile == BluetoothProfile.HID_DEVICE) {
                    hidDevice = null
                    _connectionState.value = BluetoothHidState.DISCONNECTED
                }
            } catch (e: Throwable) {
                Log.w(tag, "Error in onServiceDisconnected: ${e.message}")
            }
        }
    }

    private val hidCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        object : BluetoothHidDevice.Callback() {
            @SuppressLint("MissingPermission")
            override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
                try {
                    Log.d(tag, "HID App registered: $registered")
                    if (registered) {
                        _connectionState.value = BluetoothHidState.READY
                    }
                } catch (_: Throwable) {}
            }

            @SuppressLint("MissingPermission")
            override fun onGetReport(device: BluetoothDevice?, type: Byte, id: Byte, bufferSize: Int) {
                if (device == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
                val reportData = byteArrayOf(mouseButtons, 0, 0, 0)
                try {
                    hidDevice?.replyReport(device, type, id, reportData)
                } catch (e: Throwable) {
                    Log.w(tag, "replyReport error: ${e.message}")
                }
            }

            @SuppressLint("MissingPermission")
            override fun onSetReport(device: BluetoothDevice?, type: Byte, id: Byte, data: ByteArray?) {
                if (device == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
                try {
                    hidDevice?.reportError(device, BluetoothHidDevice.ERROR_RSP_SUCCESS)
                } catch (_: Throwable) {}
            }

            @SuppressLint("MissingPermission")
            override fun onSetProtocol(device: BluetoothDevice?, protocol: Byte) {
                if (device == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
                try {
                    hidDevice?.reportError(device, BluetoothHidDevice.ERROR_RSP_SUCCESS)
                } catch (_: Throwable) {}
            }

            @SuppressLint("MissingPermission")
            override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
                try {
                    Log.d(tag, "HID connection state changed: $state for ${device?.name}")
                    when (state) {
                        BluetoothProfile.STATE_CONNECTED -> {
                            connectedDevice = device
                            _connectionState.value = BluetoothHidState.CONNECTED
                            _connectedDeviceName.value = try { device?.name } catch (_: Throwable) { "Bluetooth TV" }
                            try {
                                java.util.concurrent.Executors.newSingleThreadScheduledExecutor().schedule({
                                    sendWakeupMovement()
                                }, 350, java.util.concurrent.TimeUnit.MILLISECONDS)
                            } catch (_: Throwable) {}
                        }
                        BluetoothProfile.STATE_DISCONNECTED -> {
                            if (connectedDevice == device) {
                                connectedDevice = null
                                _connectionState.value = BluetoothHidState.READY
                                _connectedDeviceName.value = null
                            }
                        }
                        BluetoothProfile.STATE_CONNECTING -> {
                            _connectionState.value = BluetoothHidState.INITIALIZING
                        }
                    }
                } catch (_: Throwable) {}
            }
        }
    } else null

    init {
        initHidProfile()
    }

    fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            } catch (_: Throwable) {
                false
            }
        } else {
            true
        }
    }

    fun initHidProfile() {
        if (!hasBluetoothPermission()) {
            _connectionState.value = BluetoothHidState.NEED_PERMISSION
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && bluetoothAdapter != null) {
            try {
                bluetoothAdapter.getProfileProxy(context, serviceListener, BluetoothProfile.HID_DEVICE)
            } catch (e: Throwable) {
                Log.w(tag, "Failed to get HID profile proxy: ${e.message}")
                _connectionState.value = BluetoothHidState.NOT_SUPPORTED
            }
        } else {
            _connectionState.value = BluetoothHidState.NOT_SUPPORTED
        }
    }

    @SuppressLint("MissingPermission")
    private fun registerHidApp() {
        if (!hasBluetoothPermission()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val device = hidDevice ?: return
            val sdp = BluetoothHidDeviceAppSdpSettings(
                "Bluetooth Mouse",
                "Bluetooth Optical Mouse for Sony Bravia & Google TV",
                "Sony",
                BluetoothHidDevice.SUBCLASS1_MOUSE,
                hidReportDescriptor
            )
            val qos = BluetoothHidDeviceAppQosSettings(
                BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
                800, 9, 0, 11250, BluetoothHidDeviceAppQosSettings.MAX
            )
            try {
                device.registerApp(sdp, null, qos, Executors.newSingleThreadExecutor(), hidCallback)
            } catch (e: Throwable) {
                Log.w(tag, "Failed to register HID App: ${e.message}")
            }
        }
    }

    /**
     * Send real relative mouse movement to the TV (dx, dy from -127 to +127).
     * The Google TV OS renders its native mouse cursor arrow when this is received!
     */
    @SuppressLint("MissingPermission")
    fun sendMouseMove(dx: Int, dy: Int, wheel: Int = 0): Boolean {
        if (!hasBluetoothPermission()) return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        val device = hidDevice ?: return false
        val host = connectedDevice ?: getPairedTvDevices().firstOrNull() ?: return false

        val clampedDx = dx.coerceIn(-127, 127).toByte()
        val clampedDy = dy.coerceIn(-127, 127).toByte()
        val clampedWheel = wheel.coerceIn(-127, 127).toByte()

        val report = byteArrayOf(mouseButtons, clampedDx, clampedDy, clampedWheel)
        return try {
            device.sendReport(host, REPORT_ID_MOUSE, report)
        } catch (e: Throwable) {
            Log.w(tag, "sendReport mouse move failed: ${e.message}")
            false
        }
    }

    /**
     * Wakes up and forces the on-screen mouse cursor arrow to appear on Google TV
     */
    fun sendWakeupMovement(): Boolean {
        val r1 = sendMouseMove(4, 4)
        try { Thread.sleep(20) } catch (_: Throwable) {}
        val r2 = sendMouseMove(-4, -4)
        return r1 || r2
    }

    /**
     * Send mouse button press and release (0: left, 1: right, 2: middle)
     */
    @SuppressLint("MissingPermission")
    fun sendMouseClick(isRightClick: Boolean = false): Boolean {
        if (!hasBluetoothPermission()) return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        val device = hidDevice ?: return false
        val host = connectedDevice ?: getPairedTvDevices().firstOrNull() ?: return false

        val btnMask: Byte = if (isRightClick) 0x02 else 0x01
        val pressReport = byteArrayOf(btnMask, 0, 0, 0)
        val releaseReport = byteArrayOf(0, 0, 0, 0)

        return try {
            device.sendReport(host, REPORT_ID_MOUSE, pressReport)
            try { Thread.sleep(30) } catch (_: Throwable) {}
            device.sendReport(host, REPORT_ID_MOUSE, releaseReport)
            true
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * In pure Optical Mouse HID mode, keyboard and search input are handled
     * directly via Sony Bravia's native IP API for 100% reliability.
     */
    fun sendKeyboardKey(keyCode: Byte, modifier: Byte = 0): Boolean {
        return false
    }

    fun sendText(text: String): Boolean {
        return false
    }

    @SuppressLint("MissingPermission")
    fun getPairedTvDevices(): List<BluetoothDevice> {
        if (!hasBluetoothPermission()) return emptyList()

        return try {
            val paired = bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
            paired.filter { device ->
                val name = try { device.name ?: "" } catch (_: Throwable) { "" }
                name.contains("TV", ignoreCase = true) ||
                name.contains("BRAVIA", ignoreCase = true) ||
                name.contains("Sony", ignoreCase = true) ||
                name.contains("Google", ignoreCase = true) ||
                name.contains("Chromecast", ignoreCase = true)
            }.ifEmpty { paired }
        } catch (e: Throwable) {
            Log.w(tag, "Error accessing bonded devices: ${e.message}")
            emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice): Boolean {
        if (!hasBluetoothPermission()) return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        val hid = hidDevice ?: return false
        return try {
            // If already connected to another device, disconnect first
            if (connectedDevice != null && connectedDevice != device) {
                try { hid.disconnect(connectedDevice) } catch (_: Throwable) {}
                connectedDevice = null
            }
            // If not yet paired, initiate bonding first
            if (device.bondState == BluetoothDevice.BOND_NONE) {
                try { device.createBond() } catch (_: Throwable) {}
            }
            hid.connect(device)
            true
        } catch (e: Throwable) {
            Log.w(tag, "Error connecting to device: ${e.message}")
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToAddress(macAddress: String): Boolean {
        if (!hasBluetoothPermission() || macAddress.isBlank()) return false
        return try {
            val device = bluetoothAdapter?.getRemoteDevice(macAddress) ?: return false
            connectToDevice(device)
        } catch (e: Throwable) {
            Log.w(tag, "connectToAddress error: ${e.message}")
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect(): Boolean {
        if (!hasBluetoothPermission()) return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        val hid = hidDevice ?: return false
        val host = connectedDevice ?: return false
        return try {
            hid.disconnect(host)
            connectedDevice = null
            _connectionState.value = BluetoothHidState.READY
            _connectedDeviceName.value = null
            true
        } catch (e: Throwable) {
            Log.w(tag, "Error disconnecting: ${e.message}")
            false
        }
    }

    fun getConnectedDeviceAddress(): String? {
        return try { connectedDevice?.address } catch (_: Throwable) { null }
    }

    @SuppressLint("MissingPermission")
    fun makeDiscoverable(context: Context, durationSeconds: Int = 180) {
        try {
            val intent = android.content.Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, durationSeconds)
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Throwable) {
            Log.w(tag, "makeDiscoverable error: ${e.message}")
        }
    }

    private fun charToHidCode(c: Char): Pair<Byte, Boolean> {
        return when (c) {
            in 'a'..'z' -> Pair((0x04 + (c - 'a')).toByte(), false)
            in 'A'..'Z' -> Pair((0x04 + (c - 'A')).toByte(), true)
            in '1'..'9' -> Pair((0x1E + (c - '1')).toByte(), false)
            '0' -> Pair(0x27.toByte(), false)
            ' ' -> Pair(0x2C.toByte(), false)
            '\n' -> Pair(0x28.toByte(), false) // Enter
            '\b' -> Pair(0x2A.toByte(), false) // Backspace
            '-' -> Pair(0x2D.toByte(), false)
            '=' -> Pair(0x2E.toByte(), false)
            '.' -> Pair(0x37.toByte(), false)
            '/' -> Pair(0x38.toByte(), false)
            else -> Pair(0x2C.toByte(), false)
        }
    }

    @SuppressLint("MissingPermission")
    fun getConnectedHost(): BluetoothDevice? {
        if (connectedDevice != null) return connectedDevice
        if (!hasBluetoothPermission() || Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null
        return try {
            val list = hidDevice?.getConnectedDevices()
            val host = list?.firstOrNull()
            if (host != null) {
                connectedDevice = host
                _connectionState.value = BluetoothHidState.CONNECTED
                _connectedDeviceName.value = try { host.name } catch (_: Throwable) { "Bluetooth TV" }
            }
            host
        } catch (_: Throwable) {
            null
        }
    }

    @SuppressLint("MissingPermission")
    fun isConnected(): Boolean {
        if (_connectionState.value == BluetoothHidState.CONNECTED) return true
        return getConnectedHost() != null
    }
}
