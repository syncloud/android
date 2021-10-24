package org.syncloud.android.discovery

import android.net.wifi.WifiManager
import org.apache.log4j.Logger
import java.lang.Exception

class MulticastLock(private val wifi: WifiManager) {
    private var lock: WifiManager.MulticastLock? = null

    fun acquire() {
        logger.info("creating multicast lock")
        try {
            lock = wifi.createMulticastLock(MulticastLock::class.java.toString())
            lock?.also {
                it.setReferenceCounted(true)
                it.acquire()
            }
        } catch (e: Exception) {
            logger.error("failed to acquire multicast lock", e)
            release()
        }
    }

    fun release() {
        if (lock != null) {
            try {
                logger.info("releasing multicast lock")
                lock!!.release()
            } catch (e: Exception) {
                logger.error("failed to release multicast lock", e)
            }
        }
        lock = null
    }

    companion object {
        private val logger = Logger.getLogger(MulticastLock::class.java.name)
    }
}