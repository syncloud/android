package org.syncloud.android.discovery.nsd

import android.net.nsd.NsdManager
import org.syncloud.android.discovery.DeviceEndpointListener
import android.net.nsd.NsdServiceInfo
import org.apache.log4j.Logger
import org.syncloud.android.core.platform.model.Endpoint
import org.syncloud.android.discovery.DiscoveryManager
import java.util.*

class Resolver(
    private val manager: NsdManager,
    private val deviceEndpointListener: DeviceEndpointListener?
) {
    private var isBusy = false
    private val queue: Queue<NsdServiceInfo> = LinkedList()
    private val resolveListener: ResolveListener

    inner class QueueItem(var serviceName: String, var serviceInfo: NsdServiceInfo)

    fun resolve(serviceInfo: NsdServiceInfo) {
        queue.add(serviceInfo)
        checkQueue()
    }

    @Synchronized
    private fun checkQueue() {
        if (isBusy) return
        val serviceInfo = queue.poll()
        if (serviceInfo != null) {
            isBusy = true
            manager.resolveService(serviceInfo, resolveListener)
        }
    }

    private fun endResolving() {
        isBusy = false
        checkQueue()
    }

    private fun deviceFound(device: Endpoint) {
        deviceEndpointListener?.added(device)
    }

    inner class ResolveListener : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            val text =
                "resolve failed for service: " + serviceInfo.serviceName + ", error code: " + errorCode
            logger.error(text)
            endResolving()
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            val serviceName = serviceInfo.serviceName
            val text = "service: $serviceName resovled"
            logger.info(text)
            val host = serviceInfo.host
            if (host != null) {
                val address = host.hostAddress
                if (!address.contains(":")) {
                    val device = Endpoint(address, DiscoveryManager.ACTIVATION_PORT)
                    deviceFound(device)
                }
            }
            endResolving()
        }
    }

    companion object {
        private val logger = Logger.getLogger(Resolver::class.java.name)
    }

    init {
        resolveListener = ResolveListener()
    }
}