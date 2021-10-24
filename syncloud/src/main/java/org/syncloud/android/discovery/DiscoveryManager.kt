package org.syncloud.android.discovery

import android.net.nsd.NsdManager
import android.net.wifi.WifiManager
import kotlinx.coroutines.delay
import org.apache.log4j.Logger
import org.syncloud.android.discovery.nsd.NsdDiscovery

class DiscoveryManager(wifi: WifiManager, private val manager: NsdManager) {
    private val lock: MulticastLock = MulticastLock(wifi)
    private var discovery: Discovery? = null
    private var canceled = false

    suspend fun run(timeoutSeconds: Int, added: suspend (device: String) -> Unit ) {
        canceled = false
        logger.info("starting discovery")
        if (discovery == null) {
            lock.acquire()
            discovery = NsdDiscovery(manager, added, "syncloud")
            (discovery as NsdDiscovery).start()
            try {
                logger.info("waiting for $timeoutSeconds seconds")
                var count = 0
                while (count < timeoutSeconds && !canceled) {
                    delay(1000)
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
                discovery?.stop()
                discovery = null
            } catch (e: Exception) {
                logger.error("failed to stop discovery", e)
            }
            lock.release()
        }
    }

    companion object {
        private val logger = Logger.getLogger(DiscoveryManager::class.java.name)
    }
}