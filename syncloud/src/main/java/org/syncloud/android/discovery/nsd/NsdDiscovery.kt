package org.syncloud.android.discovery.nsd

import android.net.nsd.NsdManager
import org.apache.log4j.Logger
import org.syncloud.android.discovery.Discovery

const val TYPE = "_ssh._tcp."

class NsdDiscovery(
        private val manager: NsdManager,
        added : suspend (device: String) -> Unit,
        serviceName: String
) : Discovery {
    val resolver = Resolver(manager, added)
    private val listener = EventToDeviceConverter(manager, serviceName, resolver)
    private var started = false

    override fun start() {
        logger.info("starting discovery")
        if (started) {
            logger.error("already started, stop first")
            return
        }
        try {
            logger.info("starting discovery with listener")
            manager.discoverServices(TYPE, NsdManager.PROTOCOL_DNS_SD, listener)
            started = true
        } catch (e: Exception) {
            e.printStackTrace()
            logger.error(e.message)
        }
    }

    override fun stop() {
        logger.info("stopping discovery")
        if (!started) {
            logger.error("discovery not started, start first")
            return
        }
        try {
            started = false
            logger.info("stopping discovery with listener")
            manager.stopServiceDiscovery(listener)
        } catch (e: Exception) {
            e.printStackTrace()
            logger.error(e.message)
        }
    }

    companion object {
        private val logger = Logger.getLogger(NsdDiscovery::class.java.name)
    }

}