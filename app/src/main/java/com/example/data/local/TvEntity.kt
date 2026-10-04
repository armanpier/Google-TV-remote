package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.TvDevice

@Entity(tableName = "tv_devices")
data class TvEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val ipAddress: String,
    val port: Int = 80,
    val psk: String = "0000",
    val is4K: Boolean = true,
    val resolutionWidth: Int = 3840,
    val resolutionHeight: Int = 2160,
    val modelName: String = "Sony BRAVIA 4K Google TV",
    val isDefault: Boolean = false,
    val lastConnected: Long = System.currentTimeMillis(),
    val bluetoothAddress: String? = null,
    val bluetoothName: String? = null
) {
    fun toDomain(): TvDevice = TvDevice(
        id = id,
        name = name,
        ipAddress = ipAddress,
        port = port,
        psk = psk,
        is4K = is4K,
        resolutionWidth = resolutionWidth,
        resolutionHeight = resolutionHeight,
        modelName = modelName,
        isDefault = isDefault,
        lastConnected = lastConnected,
        bluetoothAddress = bluetoothAddress,
        bluetoothName = bluetoothName
    )

    companion object {
        fun fromDomain(device: TvDevice): TvEntity = TvEntity(
            id = device.id,
            name = device.name,
            ipAddress = device.ipAddress,
            port = device.port,
            psk = device.psk,
            is4K = device.is4K,
            resolutionWidth = device.resolutionWidth,
            resolutionHeight = device.resolutionHeight,
            modelName = device.modelName,
            isDefault = device.isDefault,
            lastConnected = device.lastConnected,
            bluetoothAddress = device.bluetoothAddress,
            bluetoothName = device.bluetoothName
        )
    }
}
