package org.syncloud.android.discovery.nsd

import android.net.nsd.NsdManager
import android.net.nsd.NsdManager.DiscoveryListener
import com.google.common.collect.Lists
import org.syncloud.android.discovery.nsd.EventToDeviceConverter
import android.net.nsd.NsdServiceInfo
import org.apache.log4j.Logger

class EventToDeviceConverter(
    private val manager: NsdManager,
    lookForServiceNameInput: String,
    private val resolver: Resolver
) : DiscoveryListener {
    private val lookForServiceName: String = lookForServiceNameInput.lowercase()
    private val discoveredServices: MutableList<String> = Lists.newArrayList()

    override fun onStartDiscoveryFailed(s: String, i: Int) {
        val text = "start discovery failed $s"
        logger.error(text)
        manager.stopServiceDiscovery(this)
    }

    override fun onStopDiscoveryFailed(s: String, i: Int) {
        val text = "stop discovery failed $s"
        logger.error(text)
        manager.stopServiceDiscovery(this)
    }

    override fun onDiscoveryStarted(s: String) {
        val text = "discovery started $s"
        logger.info(text)
    }

    override fun onDiscoveryStopped(s: String) {
        val text = "discovery stopped $s"
        logger.info(text)
    }

    override fun onServiceFound(serviceInfo: NsdServiceInfo) {
        val serviceName = serviceInfo.serviceName.lowercase()
        var text = "service found $serviceName"
        logger.info(text)
        if (!discoveredServices.contains(serviceName)) {
            if (serviceName.contains(lookForServiceName)) {
                discoveredServices.add(serviceName)
                text = "starting resolving service $serviceName"
                logger.info(text)
                resolver.resolve(serviceInfo)
            }
        }
    }

    override fun onServiceLost(serviceInfo: NsdServiceInfo) {
        val text = "service lost " + serviceInfo.serviceName
        logger.info(text)
    }

    companion object {
        private val logger = Logger.getLogger(EventToDeviceConverter::class.java.name)
    }
}