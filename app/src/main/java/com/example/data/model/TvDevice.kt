package com.example.data.model

data class TvDevice(
    val id: Long = 0,
    val name: String = "Sony BRAVIA 4K TV",
    val ipAddress: String = "192.168.1.100",
    val port: Int = 80,
    val psk: String = "0000",
    val is4K: Boolean = true,
    val resolutionWidth: Int = 3840,
    val resolutionHeight: Int = 2160,
    val modelName: String = "BRAVIA Google TV",
    val isDefault: Boolean = true,
    val lastConnected: Long = System.currentTimeMillis(),
    val bluetoothAddress: String? = null,
    val bluetoothName: String? = null
)

data class DiscoveredTv(
    val ipAddress: String,
    val name: String,
    val model: String,
    val isSonyBravia: Boolean = true,
    val pingMs: Long = 0L,
    val port: Int = 80,
    val discoverySource: String = "mDNS / NSD (_googlecast._tcp)",
    val isOnline: Boolean = true
)

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    FAILED
}
