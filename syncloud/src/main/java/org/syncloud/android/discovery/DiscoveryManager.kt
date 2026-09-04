package org.syncloud.android.discovery

import android.net.nsd.NsdManager
import android.net.wifi.WifiManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.syncloud.android.Logger
import org.syncloud.android.discovery.nsd.NsdDiscovery
import java.util.Collections

const val SERVICE_NAME = "syncloud"

class DiscoveryManager(wifi: WifiManager, private val manager: NsdManager) {
    private val lock: MulticastLock = MulticastLock(wifi)
    private var discovery: Discovery? = null
    private var canceled = false

    suspend fun run(timeoutSeconds: Int, added: suspend (device: String) -> Unit) {
        canceled = false
        logger.info("starting discovery")
        if (discovery != null) return

        val reported = Collections.synchronizedSet(mutableSetOf<String>())
        val report: suspend (String) -> Unit = { device ->
            if (reported.add(device)) added(device)
        }

        lock.acquire()
        discovery = NsdDiscovery(manager, report, SERVICE_NAME)
        (discovery as NsdDiscovery).start()
        coroutineScope {
            launch {
                UnicastDiscovery(SERVICE_NAME).query(timeoutSeconds, { canceled }, report)
            }
            logger.info("waiting for $timeoutSeconds seconds")
            var count = 0
            while (count < timeoutSeconds && !canceled) {
                delay(1000)
                count++
            }
        }
        stop()
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
