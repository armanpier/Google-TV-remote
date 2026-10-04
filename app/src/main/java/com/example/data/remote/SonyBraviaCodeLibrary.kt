package com.example.data.remote

import com.example.data.model.RemoteCommand

enum class SonyTvGeneration(val label: String) {
    GOOGLE_TV("Sony BRAVIA Google TV (2021–Present)"),
    ANDROID_TV("Sony BRAVIA Android TV (2015–2020)"),
    LEGACY_SMART_TV("Sony BRAVIA Smart TV (KDL / Linux)"),
    GENERIC_GOOGLE_TV("Google TV / Android TV (Universal)")
}

/**
 * Authoritative Sony Bravia & Google TV IRCC & HID keycode library.
 * Contains verified SIRCS / IRCC code dictionaries categorized by TV generation,
 * along with native USB/Bluetooth HID Keyboard scancodes.
 */
object SonyBraviaCodeLibrary {

    /**
     * Detect TV generation based on Sony model designation string
     * e.g. "XR-65A80K" -> GOOGLE_TV
     * e.g. "XBR-65X900H" -> ANDROID_TV
     * e.g. "KDL-55W805B" -> LEGACY_SMART_TV
     */
    fun detectGeneration(modelName: String): SonyTvGeneration {
        val upper = modelName.uppercase()
        return when {
            // Sony XR series (Cognitive Processor XR) & 2021+ Google TVs (A80K, A90J, X90K, BRAVIA 7/8/9)
            upper.startsWith("XR-") || upper.contains("BRAVIA") || upper.contains("GOOGLE") ||
            upper.matches(Regex(".*[0-9]{2}[A-Z][0-9]{2}[J-Z].*")) -> SonyTvGeneration.GOOGLE_TV

            // Sony XBR / KD Android TV series (2015-2020: X900H, X950G, A8H, etc.)
            upper.startsWith("XBR-") || upper.startsWith("KD-") || upper.contains("ANDROID") -> SonyTvGeneration.ANDROID_TV

            // Sony KDL / KLV / EX Legacy Smart TVs (2010-2014)
            upper.startsWith("KDL-") || upper.startsWith("KLV-") || upper.startsWith("KDL") -> SonyTvGeneration.LEGACY_SMART_TV

            else -> SonyTvGeneration.GOOGLE_TV
        }
    }

    // Google TV (2021+) IRCC map
    private val googleTvCodes = mapOf(
        RemoteCommand.POWER to "AAAAAQAAAAEAAAAVAw==",
        RemoteCommand.POWER_OFF to "AAAAAQAAAAEAAAAvAw==",
        RemoteCommand.UP to "AAAAAQAAAAEAAAB0Aw==",
        RemoteCommand.DOWN to "AAAAAQAAAAEAAAB1Aw==",
        RemoteCommand.LEFT to "AAAAAQAAAAEAAAA0Aw==",
        RemoteCommand.RIGHT to "AAAAAQAAAAEAAAA1Aw==",
        RemoteCommand.CONFIRM to "AAAAAQAAAAEAAABlAw==",
        RemoteCommand.BACK to "AAAAAgAAAJcAAAAjAw==",      // Verified Sony Return/Back
        RemoteCommand.HOME to "AAAAAQAAAAEAAABgAw==",
        RemoteCommand.EXIT to "AAAAAQAAAAEAAABjAw==",
        RemoteCommand.VOLUME_UP to "AAAAAQAAAAEAAAASAw==",
        RemoteCommand.VOLUME_DOWN to "AAAAAQAAAAEAAAATAw==",
        RemoteCommand.MUTE to "AAAAAQAAAAEAAAAUAw==",
        RemoteCommand.CHANNEL_UP to "AAAAAQAAAAEAAAAQAw==",
        RemoteCommand.CHANNEL_DOWN to "AAAAAQAAAAEAAAARAw==",
        RemoteCommand.INPUT_TOGGLE to "AAAAAQAAAAEAAAAlAw==",
        RemoteCommand.HDMI1 to "AAAAAgAAABoAAABaAw==",
        RemoteCommand.HDMI2 to "AAAAAgAAABoAAABbAw==",
        RemoteCommand.HDMI3 to "AAAAAgAAABoAAABcAw==",
        RemoteCommand.HDMI4 to "AAAAAgAAABoAAABdAw==",
        RemoteCommand.ACTION_MENU to "AAAAAgAAAMQAAABLAw==",
        RemoteCommand.QUICK_SETTINGS to "AAAAAgAAAMQAAABLAw==",
        RemoteCommand.TV to "AAAAAQAAAAEAAAAkAw==",
        RemoteCommand.GUIDE to "AAAAAQAAAAEAAABbAw==",
        RemoteCommand.PLAY to "AAAAAgAAAJcAAAAaAw==",
        RemoteCommand.PAUSE to "AAAAAgAAAJcAAAAZAw==",
        RemoteCommand.STOP to "AAAAAgAAAJcAAAAYAw==",
        RemoteCommand.REWIND to "AAAAAgAAAJcAAAAbAw==",
        RemoteCommand.FAST_FORWARD to "AAAAAgAAAJcAAAAcAw==",
        RemoteCommand.PREV to "AAAAAgAAAJcAAAA8Aw==",
        RemoteCommand.NEXT to "AAAAAgAAAJcAAAA9Aw=="
    )

    // Android TV (2015-2020) IRCC map
    private val androidTvCodes = mapOf(
        RemoteCommand.POWER to "AAAAAQAAAAEAAAAVAw==",
        RemoteCommand.UP to "AAAAAQAAAAEAAAB0Aw==",
        RemoteCommand.DOWN to "AAAAAQAAAAEAAAB1Aw==",
        RemoteCommand.LEFT to "AAAAAQAAAAEAAAA0Aw==",
        RemoteCommand.RIGHT to "AAAAAQAAAAEAAAA1Aw==",
        RemoteCommand.CONFIRM to "AAAAAQAAAAEAAABlAw==",
        RemoteCommand.BACK to "AAAAAgAAAJcAAAAjAw==",
        RemoteCommand.HOME to "AAAAAQAAAAEAAABgAw==",
        RemoteCommand.EXIT to "AAAAAQAAAAEAAABjAw==",
        RemoteCommand.VOLUME_UP to "AAAAAQAAAAEAAAASAw==",
        RemoteCommand.VOLUME_DOWN to "AAAAAQAAAAEAAAATAw==",
        RemoteCommand.MUTE to "AAAAAQAAAAEAAAAUAw==",
        RemoteCommand.CHANNEL_UP to "AAAAAQAAAAEAAAAQAw==",
        RemoteCommand.CHANNEL_DOWN to "AAAAAQAAAAEAAAARAw==",
        RemoteCommand.INPUT_TOGGLE to "AAAAAQAAAAEAAAAlAw==",
        RemoteCommand.HDMI1 to "AAAAAgAAABoAAABaAw==",
        RemoteCommand.HDMI2 to "AAAAAgAAABoAAABbAw==",
        RemoteCommand.HDMI3 to "AAAAAgAAABoAAABcAw==",
        RemoteCommand.HDMI4 to "AAAAAgAAABoAAABdAw==",
        RemoteCommand.ACTION_MENU to "AAAAAgAAAMQAAABLAw==",
        RemoteCommand.TV to "AAAAAQAAAAEAAAAkAw==",
        RemoteCommand.GUIDE to "AAAAAQAAAAEAAABbAw=="
    )

    // Legacy Smart TV (KDL series 2010-2014) IRCC map
    private val legacySmartTvCodes = mapOf(
        RemoteCommand.POWER to "AAAAAQAAAAEAAAAVAw==",
        RemoteCommand.UP to "AAAAAQAAAAEAAAB0Aw==",
        RemoteCommand.DOWN to "AAAAAQAAAAEAAAB1Aw==",
        RemoteCommand.LEFT to "AAAAAQAAAAEAAAA0Aw==",
        RemoteCommand.RIGHT to "AAAAAQAAAAEAAAA1Aw==",
        RemoteCommand.CONFIRM to "AAAAAQAAAAEAAABlAw==",
        RemoteCommand.BACK to "AAAAAgAAAJcAAAAjAw==",
        RemoteCommand.HOME to "AAAAAQAAAAEAAABgAw==",
        RemoteCommand.EXIT to "AAAAAQAAAAEAAABjAw==",
        RemoteCommand.VOLUME_UP to "AAAAAQAAAAEAAAASAw==",
        RemoteCommand.VOLUME_DOWN to "AAAAAQAAAAEAAAATAw==",
        RemoteCommand.MUTE to "AAAAAQAAAAEAAAAUAw==",
        RemoteCommand.CHANNEL_UP to "AAAAAQAAAAEAAAAQAw==",
        RemoteCommand.CHANNEL_DOWN to "AAAAAQAAAAEAAAARAw==",
        RemoteCommand.INPUT_TOGGLE to "AAAAAQAAAAEAAAAlAw==",
        RemoteCommand.GUIDE to "AAAAAQAAAAEAAABbAw=="
    )

    /**
     * Resolve IRCC code for a command based on TV generation
     */
    fun getIrccCode(generation: SonyTvGeneration, command: RemoteCommand): String {
        val map = when (generation) {
            SonyTvGeneration.GOOGLE_TV, SonyTvGeneration.GENERIC_GOOGLE_TV -> googleTvCodes
            SonyTvGeneration.ANDROID_TV -> androidTvCodes
            SonyTvGeneration.LEGACY_SMART_TV -> legacySmartTvCodes
        }
        return map[command] ?: command.irccCode
    }

    /**
     * Native USB/Bluetooth HID Keyboard Scancodes for Google TV OS
     * Google TV OS directly translates these HID scancodes into hardware navigation events!
     */
    fun getHidScancode(command: RemoteCommand): Byte? {
        return when (command) {
            RemoteCommand.UP -> 0x52.toByte()          // Keyboard UpArrow
            RemoteCommand.DOWN -> 0x51.toByte()        // Keyboard DownArrow
            RemoteCommand.LEFT -> 0x50.toByte()        // Keyboard LeftArrow
            RemoteCommand.RIGHT -> 0x4F.toByte()       // Keyboard RightArrow
            RemoteCommand.CONFIRM -> 0x28.toByte()     // Keyboard Enter / Select
            RemoteCommand.BACK -> 0x29.toByte()        // Keyboard Escape (Android translates to Back)
            RemoteCommand.HOME -> 0x4A.toByte()        // Keyboard Home
            RemoteCommand.EXIT -> 0x29.toByte()        // Keyboard Escape
            RemoteCommand.VOLUME_UP -> 0x80.toByte()   // Keyboard Volume Up
            RemoteCommand.VOLUME_DOWN -> 0x81.toByte() // Keyboard Volume Down
            RemoteCommand.MUTE -> 0x7F.toByte()        // Keyboard Mute
            RemoteCommand.CHANNEL_UP -> 0x4B.toByte()   // Page Up
            RemoteCommand.CHANNEL_DOWN -> 0x4E.toByte() // Page Down
            RemoteCommand.PLAY -> 0xE8.toByte()        // Media Play
            RemoteCommand.PAUSE -> 0xE8.toByte()       // Media Pause
            RemoteCommand.NUM_0 -> 0x27.toByte()
            RemoteCommand.NUM_1 -> 0x1E.toByte()
            RemoteCommand.NUM_2 -> 0x1F.toByte()
            RemoteCommand.NUM_3 -> 0x20.toByte()
            RemoteCommand.NUM_4 -> 0x21.toByte()
            RemoteCommand.NUM_5 -> 0x22.toByte()
            RemoteCommand.NUM_6 -> 0x23.toByte()
            RemoteCommand.NUM_7 -> 0x24.toByte()
            RemoteCommand.NUM_8 -> 0x25.toByte()
            RemoteCommand.NUM_9 -> 0x26.toByte()
            else -> null
        }
    }
}
