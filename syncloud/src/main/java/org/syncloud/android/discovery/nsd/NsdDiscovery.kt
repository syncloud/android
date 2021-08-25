package org.syncloud.android.discovery.nsd

import android.net.nsd.NsdManager
import org.syncloud.android.discovery.DeviceEndpointListener
import org.syncloud.android.discovery.Discovery
import android.net.nsd.NsdManager.DiscoveryListener
import org.apache.log4j.Logger
import org.syncloud.android.discovery.nsd.NsdDiscovery
import org.syncloud.android.discovery.nsd.EventToDeviceConverter
import java.lang.Exception

class NsdDiscovery(
    private val manager: NsdManager,
    deviceEndpointListener: DeviceEndpointListener?,
    serviceName: String?
) : Discovery {
    private val listener: DiscoveryListener
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
        private val logger = Logger.getLogger(
            NsdDiscovery::class.java.name
        )
        const val TYPE = "_ssh._tcp."
    }

    init {
        val resolver = Resolver(
            manager, deviceEndpointListener
        )
        listener = EventToDeviceConverter(manager, serviceName!!, resolver)
    }
}