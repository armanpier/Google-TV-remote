package com.example.ui.viewmodel

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.bluetooth.BluetoothHidManager
import com.example.data.bluetooth.BluetoothHidState
import com.example.data.local.AppDatabase
import com.example.data.local.TvEntity
import com.example.data.model.ConnectionStatus
import com.example.data.model.DiscoveredTv
import com.example.data.model.RemoteCommand
import com.example.data.model.TvDevice
import com.example.data.remote.NetworkDiscovery
import com.example.data.remote.SonyBraviaClient
import com.example.util.HapticHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RemoteUiState(
    val currentTv: TvDevice = TvDevice(),
    val connectionStatus: ConnectionStatus = ConnectionStatus.CONNECTED,
    val latencyMs: Long = 18L,
    val volume: Int = 22,
    val isMuted: Boolean = false,
    val currentInput: String = "HDMI 1",
    // Mouse Cursor Simulation & Bluetooth HID State
    val is4KMode: Boolean = true,
    val cursorSensitivity: Float = 1.2f,
    val isDragLocked: Boolean = false,
    val bluetoothState: BluetoothHidState = BluetoothHidState.INITIALIZING,
    val bluetoothDeviceName: String? = null,
    val pairedBluetoothDevices: List<BluetoothDevice> = emptyList(),
    // Virtual Keyboard & Search State
    val keyboardInput: String = "",
    val recentSearches: List<String> = listOf("Netflix", "YouTube 4K", "Spotify", "BBC Earth"),
    // Discovery
    val isScanning: Boolean = false,
    val discoveredTvs: List<DiscoveredTv> = emptyList(),
    // Status banner
    val statusMessage: String? = null,
    // Help Guide Dialog
    val isHelpDialogOpen: Boolean = false
)

class TvRemoteViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val tvDao = db.tvDao()
    private val client = SonyBraviaClient()
    private val discovery = NetworkDiscovery(application)
    val bluetoothHidManager = BluetoothHidManager(application)

    private val _uiState = MutableStateFlow(RemoteUiState())
    val uiState: StateFlow<RemoteUiState> = _uiState.asStateFlow()

    val savedTvs: StateFlow<List<TvDevice>> = tvDao.getAllTvs()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var statusDismissJob: Job? = null
    private var lastMoveTimestamp: Long = 0L

    init {
        // Observe Bluetooth HID connection state
        viewModelScope.launch {
            try {
                bluetoothHidManager.connectionState.collect { state ->
                    _uiState.update { it.copy(bluetoothState = state) }
                }
            } catch (_: Throwable) {}
        }
        viewModelScope.launch {
            try {
                bluetoothHidManager.connectedDeviceName.collect { name ->
                    _uiState.update { it.copy(bluetoothDeviceName = name) }
                }
            } catch (_: Throwable) {}
        }

        // Initialize default TV profile in Room database
        viewModelScope.launch {
            try {
                val defaultTv = tvDao.getDefaultTv()
                val tvToLoad = if (defaultTv != null) {
                    defaultTv.toDomain()
                } else {
                    val initialBravia = TvDevice(
                        name = "Sony BRAVIA TV",
                        ipAddress = "192.168.1.105",
                        port = 80,
                        psk = "0000",
                        is4K = true,
                        resolutionWidth = 3840,
                        resolutionHeight = 2160,
                        modelName = "Sony BRAVIA 4K Google TV",
                        isDefault = true
                    )
                    val id = tvDao.insertTv(TvEntity.fromDomain(initialBravia))
                    initialBravia.copy(id = id)
                }
                _uiState.update { it.copy(currentTv = tvToLoad) }

                // Sync hardware commands & volume from TV
                syncTvState(tvToLoad)

                // Refresh paired Bluetooth devices safely
                refreshBluetoothDevices()

                // If this TV has a bound Bluetooth MAC address, attempt to connect to it
                if (!tvToLoad.bluetoothAddress.isNullOrBlank()) {
                    bluetoothHidManager.connectToAddress(tvToLoad.bluetoothAddress)
                }

                // Automatically start background NSD/mDNS discovery to find nearby TVs
                startScan()
            } catch (e: Throwable) {
                Log.w("TvRemoteViewModel", "Init error: ${e.message}")
            }
        }
    }

    /**
     * Query TV for actual supported IRCC codes and current volume level
     */
    private fun syncTvState(tv: TvDevice) {
        viewModelScope.launch {
            client.fetchRemoteControllerInfo(tv.ipAddress, tv.port, tv.psk)
            val volumeInfo = client.getVolumeInformation(tv.ipAddress, tv.port, tv.psk)
            if (volumeInfo != null) {
                _uiState.update {
                    it.copy(
                        volume = volumeInfo.first,
                        isMuted = volumeInfo.second
                    )
                }
            }
        }
    }

    /**
     * Send command to the TV with dynamic code resolution and haptic feedback
     */
    fun sendCommand(command: RemoteCommand, context: Context, fromMouseScreen: Boolean = false) {
        HapticHelper.click(context)

        // Optimistic UI updates
        when (command) {
            RemoteCommand.VOLUME_UP -> {
                val next = (_uiState.value.volume + 1).coerceAtMost(100)
                setVolume(next, context)
                return
            }
            RemoteCommand.VOLUME_DOWN -> {
                val next = (_uiState.value.volume - 1).coerceAtLeast(0)
                setVolume(next, context)
                return
            }
            RemoteCommand.MUTE -> {
                _uiState.update { it.copy(isMuted = !it.isMuted) }
            }
            RemoteCommand.HDMI1 -> _uiState.update { it.copy(currentInput = "HDMI 1") }
            RemoteCommand.HDMI2 -> _uiState.update { it.copy(currentInput = "HDMI 2") }
            RemoteCommand.HDMI3 -> _uiState.update { it.copy(currentInput = "HDMI 3") }
            RemoteCommand.HDMI4 -> _uiState.update { it.copy(currentInput = "HDMI 4") }
            RemoteCommand.TV -> _uiState.update { it.copy(currentInput = "TV Tuner") }
            else -> {}
        }

        viewModelScope.launch {
            val tv = _uiState.value.currentTv

            // When in Mouse screen, send native HID scancode if Bluetooth is linked
            if (fromMouseScreen && bluetoothHidManager.isConnected()) {
                val hidCode = com.example.data.remote.SonyBraviaCodeLibrary.getHidScancode(command)
                if (hidCode != null) {
                    bluetoothHidManager.sendKeyboardKey(hidCode)
                }
            }

            // Always transmit verified Sony IP IRCC code to maintain 100% normal remote mode
            val resolvedCode = client.resolveIrccCode(command, tv.modelName)
            val result = client.sendIrcc(
                ipAddress = tv.ipAddress,
                port = tv.port,
                psk = tv.psk,
                irccCode = resolvedCode
            )

            _uiState.update {
                it.copy(
                    latencyMs = result.latencyMs.coerceAtLeast(8L),
                    statusMessage = command.title
                )
            }
            scheduleStatusDismiss()
        }
    }

    fun onTabSelected(tab: Any) {
        if (tab.toString() == "MOUSE") {
            if (bluetoothHidManager.isConnected()) {
                bluetoothHidManager.sendWakeupMovement()
            }
        }
    }

    /**
     * Volume slider direct adjustment via Sony /sony/audio setAudioVolume API.
     * Instantly sets the TV to the exact target volume level (0-100).
     */
    fun setVolume(newVolume: Int, context: Context) {
        HapticHelper.tick(context)
        val clamped = newVolume.coerceIn(0, 100)
        _uiState.update { it.copy(volume = clamped, isMuted = false) }

        viewModelScope.launch {
            val tv = _uiState.value.currentTv
            val result = client.setAudioVolumeExact(
                ipAddress = tv.ipAddress,
                port = tv.port,
                psk = tv.psk,
                volumeLevel = clamped
            )
            _uiState.update {
                it.copy(
                    latencyMs = result.latencyMs,
                    statusMessage = "Volume: $clamped"
                )
            }
            scheduleStatusDismiss()
        }
    }

    /**
     * Real Standalone Bluetooth Mouse Movement Handler.
     * Transmits raw relative motion reports over Bluetooth HID interrupt channel.
     * The Google TV / Sony Bravia OS natively interprets these reports to render and move the on-screen mouse pointer arrow!
     */
    fun onTrackpadMove(dx: Float, dy: Float, context: Context) {
        val sensitivity = _uiState.value.cursorSensitivity
        val scaledDx = (dx * sensitivity * 2.5f).toInt().coerceIn(-127, 127)
        val scaledDy = (dy * sensitivity * 2.5f).toInt().coerceIn(-127, 127)

        if (scaledDx != 0 || scaledDy != 0) {
            val sent = bluetoothHidManager.sendMouseMove(scaledDx, scaledDy)
            HapticHelper.tick(context)
            if (!sent && !bluetoothHidManager.isConnected()) {
                val pairedTv = bluetoothHidManager.getPairedTvDevices().firstOrNull()
                if (pairedTv != null) {
                    bluetoothHidManager.connectToDevice(pairedTv)
                }
            }
        }
    }

    /**
     * Mouse Left / Right Click Handler.
     * Dispatches native mouse button 1 (Left Click) or button 2 (Right Click / Back).
     */
    fun onTrackpadClick(isRightClick: Boolean = false, context: Context) {
        HapticHelper.heavyClick(context)
        val sent = bluetoothHidManager.sendMouseClick(isRightClick)
        if (!sent && !bluetoothHidManager.isConnected()) {
            val pairedTv = bluetoothHidManager.getPairedTvDevices().firstOrNull()
            if (pairedTv != null) {
                bluetoothHidManager.connectToDevice(pairedTv)
            }
        }
    }

    /**
     * Mouse Scroll Wheel Handler.
     * Dispatches native mouse vertical wheel report over Bluetooth HID.
     */
    fun onTrackpadScroll(deltaY: Float, context: Context) {
        val wheel = if (deltaY < 0) 1 else -1
        bluetoothHidManager.sendMouseMove(0, 0, wheel)
        HapticHelper.tick(context)
    }

    fun setSensitivity(value: Float) {
        _uiState.update { it.copy(cursorSensitivity = value) }
    }

    fun toggleDragLock() {
        _uiState.update { it.copy(isDragLocked = !it.isDragLocked) }
    }

    /**
     * Built-in Virtual Keyboard & Search Functions
     */
    fun updateKeyboardInput(text: String) {
        _uiState.update { it.copy(keyboardInput = text) }
    }

    fun sendVirtualText(text: String, context: Context) {
        if (text.isBlank()) return
        HapticHelper.click(context)

        val updatedSearches = (_uiState.value.recentSearches.toMutableList().apply {
            remove(text)
            add(0, text)
        }).take(8)

        _uiState.update { it.copy(recentSearches = updatedSearches) }

        if (bluetoothHidManager.isConnected()) {
            bluetoothHidManager.sendText(text)
            showStatus("Typed \"$text\"")
            return
        }

        viewModelScope.launch {
            val tv = _uiState.value.currentTv
            client.sendVirtualText(tv.ipAddress, tv.port, tv.psk, text)
            showStatus("Sent text to TV")
        }
    }

    fun sendKeyboardSpecialKey(keyCode: Byte, context: Context) {
        HapticHelper.click(context)
        if (bluetoothHidManager.isConnected()) {
            bluetoothHidManager.sendKeyboardKey(keyCode)
        } else {
            when (keyCode) {
                0x28.toByte() -> sendCommand(RemoteCommand.CONFIRM, context) // Enter
                0x2A.toByte() -> sendCommand(RemoteCommand.BACK, context)    // Backspace
                0x29.toByte() -> sendCommand(RemoteCommand.BACK, context)    // Escape
            }
        }
    }

    fun triggerTvSearch(context: Context) {
        HapticHelper.click(context)
        viewModelScope.launch {
            val tv = _uiState.value.currentTv
            val code = client.resolveIrccCode(RemoteCommand.ACTION_MENU)
            client.sendIrcc(tv.ipAddress, tv.port, tv.psk, code)
            showStatus("Opened Search on TV")
        }
    }

    /**
     * Input Switching
     */
    fun switchInput(port: Int, context: Context) {
        HapticHelper.heavyClick(context)
        _uiState.update { it.copy(currentInput = "HDMI $port") }
        viewModelScope.launch {
            val tv = _uiState.value.currentTv
            client.setInputPort(tv.ipAddress, tv.port, tv.psk, port)
            showStatus("Switched to HDMI $port")
        }
    }

    /**
     * Bluetooth HID device pairing, connection, and disconnection
     */
    fun refreshBluetoothDevices() {
        try {
            bluetoothHidManager.initHidProfile()
            val paired = bluetoothHidManager.getPairedTvDevices()
            _uiState.update { it.copy(pairedBluetoothDevices = paired) }
        } catch (e: Throwable) {
            Log.w("TvRemoteViewModel", "Could not refresh Bluetooth devices: ${e.message}")
            _uiState.update { it.copy(pairedBluetoothDevices = emptyList()) }
        }
    }

    fun connectBluetoothDevice(device: BluetoothDevice) {
        try {
            val ok = bluetoothHidManager.connectToDevice(device)
            val name = try { device.name ?: "TV" } catch (_: Throwable) { "TV" }
            val mac = try { device.address } catch (_: Throwable) { null }
            if (ok) {
                val updatedTv = _uiState.value.currentTv.copy(
                    bluetoothAddress = mac,
                    bluetoothName = name
                )
                saveTv(updatedTv)
                showStatus("Connecting to $name...")
            } else {
                showStatus("Connection failed. Put TV in 'Add accessory' mode.")
            }
        } catch (e: Throwable) {
            showStatus("Bluetooth error: ${e.localizedMessage}")
        }
    }

    fun disconnectBluetooth() {
        bluetoothHidManager.disconnect()
        showStatus("Disconnected Bluetooth")
    }

    fun makePhoneDiscoverable(context: Context) {
        bluetoothHidManager.makeDiscoverable(context, 180)
    }

    fun onDiscoverableResult(granted: Boolean) {
        if (granted) {
            showStatus("Phone is discoverable for 3 minutes! On TV: Add Accessory.")
        } else {
            showStatus("Discoverable mode cancelled.")
        }
    }

    /**
     * Network Discovery & TV Management
     */
    fun startScan() {
        if (_uiState.value.isScanning) return
        _uiState.update { it.copy(isScanning = true, discoveredTvs = emptyList()) }

        viewModelScope.launch {
            val found = discovery.scanNetwork { discovered ->
                _uiState.update { current ->
                    val list = current.discoveredTvs.toMutableList()
                    if (list.none { it.ipAddress == discovered.ipAddress }) {
                        list.add(discovered)
                    }
                    current.copy(discoveredTvs = list)
                }
            }
            _uiState.update {
                it.copy(
                    isScanning = false,
                    statusMessage = "Found ${found.size} TV(s)"
                )
            }
            scheduleStatusDismiss()
        }
    }

    override fun onCleared() {
        super.onCleared()
        discovery.stopContinuousNsd()
    }

    /**
     * Switch currently controlled TV.
     * Prevents cross-room confusion by disconnecting from previous TV's Bluetooth HID
     * and reconnecting specifically to the selected TV's paired Bluetooth device.
     */
    fun selectTv(tv: TvDevice, context: Context) {
        HapticHelper.click(context)

        // Disconnect HID if currently connected to a different TV
        val currentConnectedBt = bluetoothHidManager.getConnectedDeviceAddress()
        if (currentConnectedBt != null && currentConnectedBt != tv.bluetoothAddress) {
            bluetoothHidManager.disconnect()
        }

        _uiState.update {
            it.copy(
                currentTv = tv,
                connectionStatus = ConnectionStatus.CONNECTED,
                statusMessage = "Connected to ${tv.name}"
            )
        }

        viewModelScope.launch {
            tvDao.clearDefaultFlags()
            tvDao.setDefaultTv(tv.id)
            syncTvState(tv)

            // If this TV has a bound Bluetooth MAC address, auto-connect to it!
            if (!tv.bluetoothAddress.isNullOrBlank() && !bluetoothHidManager.isConnected()) {
                bluetoothHidManager.connectToAddress(tv.bluetoothAddress)
            }
        }
        scheduleStatusDismiss()
    }

    fun saveTv(tv: TvDevice) {
        viewModelScope.launch {
            if (tv.id == 0L) {
                val newId = tvDao.insertTv(TvEntity.fromDomain(tv))
                _uiState.update { it.copy(currentTv = tv.copy(id = newId), statusMessage = "Saved ${tv.name}") }
            } else {
                tvDao.updateTv(TvEntity.fromDomain(tv))
                _uiState.update { it.copy(currentTv = tv, statusMessage = "Updated ${tv.name}") }
            }
            scheduleStatusDismiss()
        }
    }

    fun deleteTv(tv: TvDevice) {
        viewModelScope.launch {
            tvDao.deleteTv(TvEntity.fromDomain(tv))
            showStatus("Deleted ${tv.name}")
        }
    }

    fun testCurrentConnection() {
        _uiState.update { it.copy(connectionStatus = ConnectionStatus.CONNECTING) }
        viewModelScope.launch {
            val tv = _uiState.value.currentTv
            val (ok, message) = client.testConnection(tv.ipAddress, tv.port, tv.psk)
            _uiState.update {
                it.copy(
                    connectionStatus = if (ok) ConnectionStatus.CONNECTED else ConnectionStatus.FAILED,
                    statusMessage = if (ok) "Connected to ${tv.name}" else "Connection Failed"
                )
            }
            if (ok) {
                syncTvState(tv)
            }
            scheduleStatusDismiss()
        }
    }

    fun setHelpDialogOpen(open: Boolean) {
        _uiState.update { it.copy(isHelpDialogOpen = open) }
    }

    private fun showStatus(msg: String) {
        _uiState.update { it.copy(statusMessage = msg) }
        scheduleStatusDismiss()
    }

    private fun scheduleStatusDismiss() {
        statusDismissJob?.cancel()
        statusDismissJob = viewModelScope.launch {
            delay(3000)
            _uiState.update { it.copy(statusMessage = null) }
        }
    }
}
