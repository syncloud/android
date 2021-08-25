package org.syncloud.android.discovery

import android.net.nsd.NsdManager
import android.net.wifi.WifiManager
import org.apache.log4j.Logger
import org.syncloud.android.discovery.DiscoveryManager
import org.syncloud.android.discovery.nsd.NsdDiscovery

class DiscoveryManager(wifi: WifiManager, manager: NsdManager) {
    private val lock: MulticastLock
    private lateinit var discovery: Discovery
    private val manager: NsdManager
    private var canceled = false
    fun run(timeoutSeconds: Int, deviceEndpointListener: DeviceEndpointListener?) {
        canceled = false
        logger.info("starting discovery")
        if (discovery == null) {
            lock.acquire()
            discovery = NsdDiscovery(manager, deviceEndpointListener, "syncloud")
            discovery.start()
            try {
                logger.info("waiting for $timeoutSeconds seconds")
                var count = 0
                while (count < timeoutSeconds && !canceled) {
                    Thread.sleep(1000)
                    count++
                }
            } catch (e: InterruptedException) {
                logger.error("sleep interrupted", e)
            }
            stop()
        }
    }

    fun cancel() {
        canceled = true
    }

    private fun stop() {
        logger.info("stopping discovery")
        if (discovery != null) {
            try {
                discovery!!.stop()
            } catch (e: Exception) {
                logger.error("failed to stop discovery", e)
            }
            lock.release()
        }
    }

    companion object {
        private val logger = Logger.getLogger(
            DiscoveryManager::class.java.name
        )
        var ACTIVATION_PORT = 81
    }

    init {
        lock = MulticastLock(wifi!!)
        this.manager = manager
    }
}