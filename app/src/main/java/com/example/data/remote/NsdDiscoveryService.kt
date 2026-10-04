package com.example.data.remote

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import com.example.data.model.DiscoveredTv
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Android Network Service Discovery (NSD) / mDNS Service.
 * Scans the local network for Sony Bravia and Google TV devices broadcasting
 * over _googlecast._tcp, _androidtvremote2._tcp, and _sony-bravia._tcp.
 */
class NsdDiscoveryService(private val context: Context) {

    private val tag = "NsdDiscovery"
    private val nsdManager: NsdManager? =
        context.applicationContext.getSystemService(Context.NSD_SERVICE) as? NsdManager
    private val wifiManager: WifiManager? =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private var multicastLock: WifiManager.MulticastLock? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val isDiscovering = AtomicBoolean(false)
    private val activeListeners = mutableListOf<Pair<String, NsdManager.DiscoveryListener>>()

    private val _discoveredDevices = MutableSharedFlow<DiscoveredTv>(replay = 20)
    val discoveredDevices: SharedFlow<DiscoveredTv> = _discoveredDevices.asSharedFlow()

    // Sequential resolution queue to prevent NsdManager.FAILURE_ALREADY_ACTIVE
    private val resolveQueue = Channel<NsdServiceInfo>(capacity = Channel.UNLIMITED)
    private val knownIps = mutableSetOf<String>()

    init {
        // Worker to resolve discovered services sequentially
        scope.launch {
            for (serviceInfo in resolveQueue) {
                resolveServiceSafely(serviceInfo)
            }
        }
    }

    /**
     * Start mDNS / NSD service discovery on the local Wi-Fi network.
     */
    fun startDiscovery(onDeviceFound: (DiscoveredTv) -> Unit) {
        if (isDiscovering.getAndSet(true)) {
            Log.d(tag, "Discovery already active")
            return
        }

        acquireMulticastLock()

        val serviceTypes = listOf(
            "_googlecast._tcp.",       // Broadcasted by all Google TVs, Sony Bravia, and Android TVs
            "_androidtvremote2._tcp.", // Google TV remote v2 service
            "_sony-bravia._tcp."       // Sony Bravia native control
        )

        for (serviceType in serviceTypes) {
            val listener = createDiscoveryListener(serviceType, onDeviceFound)
            try {
                nsdManager?.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, listener)
                synchronized(activeListeners) {
                    activeListeners.add(Pair(serviceType, listener))
                }
                Log.d(tag, "Started NSD discovery for $serviceType")
            } catch (e: Exception) {
                Log.w(tag, "Failed to start discovery for $serviceType: ${e.message}")
            }
        }
    }

    /**
     * Stop active mDNS / NSD discovery listeners and release multicast lock.
     */
    fun stopDiscovery() {
        if (!isDiscovering.getAndSet(false)) return

        synchronized(activeListeners) {
            for ((serviceType, listener) in activeListeners) {
                try {
                    nsdManager?.stopServiceDiscovery(listener)
                    Log.d(tag, "Stopped discovery for $serviceType")
                } catch (e: Exception) {
                    Log.w(tag, "Error stopping discovery for $serviceType: ${e.message}")
                }
            }
            activeListeners.clear()
        }

        releaseMulticastLock()
    }

    private fun createDiscoveryListener(
        targetServiceType: String,
        onDeviceFound: (DiscoveredTv) -> Unit
    ): NsdManager.DiscoveryListener {
        return object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d(tag, "NSD discovery started: $regType")
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.d(tag, "Service found: ${serviceInfo.serviceName} (${serviceInfo.serviceType})")
                // Queue for sequential resolution
                resolveQueue.trySend(serviceInfo)
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                Log.d(tag, "Service lost: ${serviceInfo.serviceName}")
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.d(tag, "Discovery stopped: $serviceType")
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(tag, "Start discovery failed for $serviceType with code $errorCode")
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(tag, "Stop discovery failed for $serviceType with code $errorCode")
            }
        }
    }

    private suspend fun resolveServiceSafely(serviceInfo: NsdServiceInfo) = withContext(Dispatchers.IO) {
        val resolverListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(service: NsdServiceInfo, errorCode: Int) {
                Log.w(tag, "Resolve failed for ${service.serviceName}: code $errorCode")
            }

            override fun onServiceResolved(resolved: NsdServiceInfo) {
                val hostAddress = resolved.host?.hostAddress ?: return
                if (knownIps.contains(hostAddress)) return
                knownIps.add(hostAddress)

                // Decode mDNS TXT records
                val attributes = parseAttributes(resolved)
                val friendlyName = attributes["fn"] ?: resolved.serviceName
                val modelName = attributes["md"] ?: "Google TV / Sony Bravia"

                val isSony = modelName.contains("BRAVIA", ignoreCase = true) ||
                             friendlyName.contains("Sony", ignoreCase = true) ||
                             friendlyName.contains("BRAVIA", ignoreCase = true) ||
                             resolved.serviceType.contains("sony", ignoreCase = true)

                // Measure quick ping latency
                val ping = measureLatency(hostAddress, resolved.port.takeIf { it > 0 } ?: 80)

                val discovered = DiscoveredTv(
                    ipAddress = hostAddress,
                    name = friendlyName,
                    model = modelName,
                    isSonyBravia = isSony,
                    pingMs = ping,
                    port = resolved.port.takeIf { it > 0 } ?: 80,
                    discoverySource = "mDNS / NSD (${resolved.serviceType})"
                )

                scope.launch {
                    _discoveredDevices.emit(discovered)
                }
                Log.i(tag, "Discovered device via mDNS: $friendlyName ($hostAddress)")
            }
        }

        try {
            nsdManager?.resolveService(serviceInfo, resolverListener)
        } catch (e: Exception) {
            Log.w(tag, "Exception during resolveService: ${e.message}")
        }
    }

    private fun parseAttributes(serviceInfo: NsdServiceInfo): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val rawAttributes = serviceInfo.attributes
                for ((key, valueBytes) in rawAttributes) {
                    if (valueBytes != null) {
                        map[key] = String(valueBytes, Charsets.UTF_8)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to parse service attributes: ${e.message}")
        }
        return map
    }

    private fun measureLatency(ip: String, port: Int): Long {
        val start = System.currentTimeMillis()
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), 250)
                System.currentTimeMillis() - start
            }
        } catch (_: Exception) {
            15L // Estimated typical local Wi-Fi latency
        }
    }

    private fun acquireMulticastLock() {
        try {
            if (multicastLock == null) {
                multicastLock = wifiManager?.createMulticastLock("bravia_nsd_multicast_lock")?.apply {
                    setReferenceCounted(true)
                }
            }
            multicastLock?.let {
                if (!it.isHeld) {
                    it.acquire()
                    Log.d(tag, "Acquired Wi-Fi MulticastLock for mDNS discovery")
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Could not acquire MulticastLock: ${e.message}")
        }
    }

    private fun releaseMulticastLock() {
        try {
            multicastLock?.let {
                if (it.isHeld) {
                    it.release()
                    Log.d(tag, "Released Wi-Fi MulticastLock")
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Could not release MulticastLock: ${e.message}")
        }
    }
}
