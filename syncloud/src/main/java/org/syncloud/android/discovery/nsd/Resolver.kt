package org.syncloud.android.discovery.nsd

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.log4j.Logger
import java.net.Inet6Address
import java.util.*

class Resolver(
        private val manager: NsdManager,
        val added: suspend (endpoint: String) -> Unit
) {
    private var isBusy = false
    private val queue: Queue<NsdServiceInfo> = LinkedList()
    private val resolveListener: ResolveListener = ResolveListener()

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

    private suspend fun deviceFound(device: String) = added(device)

    inner class ResolveListener : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            val text =
                    "resolve failed for service: " + serviceInfo.serviceName + ", error code: " + errorCode
            logger.error(text)
            endResolving()
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            val serviceName = serviceInfo.serviceName
            val text = "service: $serviceName resolved"
            logger.info(text)
            val host = serviceInfo.host
            if (host != null) {
                val address =
                        if (host is Inet6Address)
                            "[" + host.hostAddress + "]"
                        else
                            host.hostAddress

                CoroutineScope(Dispatchers.IO).launch {
                    deviceFound(address)
                }

            }
            endResolving()
        }
    }

    companion object {
        private val logger = Logger.getLogger(Resolver::class.java.name)
    }
}