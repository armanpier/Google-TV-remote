package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CommandCategory
import com.example.data.model.DiscoveredTv
import com.example.data.model.RemoteCommand
import com.example.data.model.TvDevice
import com.example.data.remote.SonyBraviaClient
import com.example.data.remote.SonyBraviaCodeLibrary
import com.example.data.remote.SonyTvGeneration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Bravia Remote", appName)
    }

    @Test
    fun `verify tv device model defaults and bluetooth address binding`() {
        val tv = TvDevice(
            name = "Sony BRAVIA 4K",
            ipAddress = "192.168.1.105",
            psk = "0000",
            bluetoothAddress = "AA:BB:CC:DD:EE:FF",
            bluetoothName = "Living Room TV"
        )
        assertTrue(tv.is4K)
        assertEquals(3840, tv.resolutionWidth)
        assertEquals(2160, tv.resolutionHeight)
        assertEquals("0000", tv.psk)
        assertEquals("AA:BB:CC:DD:EE:FF", tv.bluetoothAddress)
        assertEquals("Living Room TV", tv.bluetoothName)
    }

    @Test
    fun `verify remote command ircc codes`() {
        assertNotNull(RemoteCommand.POWER.irccCode)
        assertEquals(CommandCategory.POWER, RemoteCommand.POWER.category)
        assertNotNull(RemoteCommand.HDMI1.irccCode)
        assertEquals(CommandCategory.AUDIO, RemoteCommand.VOLUME_UP.category)
        assertEquals("AAAAAgAAAJcAAAAjAw==", RemoteCommand.BACK.irccCode)
        assertEquals("AAAAAQAAAAEAAAAQAw==", RemoteCommand.CHANNEL_UP.irccCode)
        assertEquals("AAAAAQAAAAEAAAARAw==", RemoteCommand.CHANNEL_DOWN.irccCode)
        assertEquals("AAAAAQAAAAEAAAB0Aw==", RemoteCommand.UP.irccCode)
        assertEquals("AAAAAQAAAAEAAAB1Aw==", RemoteCommand.DOWN.irccCode)
        assertEquals("AAAAAQAAAAEAAAA0Aw==", RemoteCommand.LEFT.irccCode)
        assertEquals("AAAAAQAAAAEAAAA1Aw==", RemoteCommand.RIGHT.irccCode)
    }

    @Test
    fun `verify client code resolution with model generations`() {
        val client = SonyBraviaClient()
        val backCode = client.resolveIrccCode(RemoteCommand.BACK, "XR-65A80K")
        assertEquals("AAAAAgAAAJcAAAAjAw==", backCode)
        val chUpCode = client.resolveIrccCode(RemoteCommand.CHANNEL_UP, "XBR-65X900H")
        assertEquals("AAAAAQAAAAEAAAAQAw==", chUpCode)
    }

    @Test
    fun `verify sony bravia generation detection and hid scancodes`() {
        assertEquals(SonyTvGeneration.GOOGLE_TV, SonyBraviaCodeLibrary.detectGeneration("XR-65A80K"))
        assertEquals(SonyTvGeneration.ANDROID_TV, SonyBraviaCodeLibrary.detectGeneration("XBR-65X900H"))
        assertEquals(SonyTvGeneration.LEGACY_SMART_TV, SonyBraviaCodeLibrary.detectGeneration("KDL-48W600B"))

        // Verify HID hardware scancodes
        assertEquals(0x52.toByte(), SonyBraviaCodeLibrary.getHidScancode(RemoteCommand.UP))
        assertEquals(0x51.toByte(), SonyBraviaCodeLibrary.getHidScancode(RemoteCommand.DOWN))
        assertEquals(0x50.toByte(), SonyBraviaCodeLibrary.getHidScancode(RemoteCommand.LEFT))
        assertEquals(0x4F.toByte(), SonyBraviaCodeLibrary.getHidScancode(RemoteCommand.RIGHT))
        assertEquals(0x28.toByte(), SonyBraviaCodeLibrary.getHidScancode(RemoteCommand.CONFIRM))
        assertEquals(0x29.toByte(), SonyBraviaCodeLibrary.getHidScancode(RemoteCommand.BACK))
        assertEquals(0x4A.toByte(), SonyBraviaCodeLibrary.getHidScancode(RemoteCommand.HOME))
    }

    @Test
    fun `verify discovered tv with nsd mdns source`() {
        val discovered = DiscoveredTv(
            ipAddress = "192.168.1.110",
            name = "Sony BRAVIA XR-65A80K",
            model = "XR-65A80K (Google TV)",
            isSonyBravia = true,
            pingMs = 11,
            port = 80,
            discoverySource = "mDNS / NSD (_googlecast._tcp)"
        )
        assertTrue(discovered.isSonyBravia)
        assertEquals("192.168.1.110", discovered.ipAddress)
        assertTrue(discovered.discoverySource.contains("mDNS"))
    }
}
