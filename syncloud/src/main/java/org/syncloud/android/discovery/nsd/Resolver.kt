package org.syncloud.android.discovery.nsd

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.syncloud.android.Logger
import java.net.Inet6Address
import java.net.InetAddress
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class Resolver(
    private val manager: NsdManager,
    val added: suspend (endpoint: String) -> Unit
) {
    private val busy = AtomicBoolean(false)
    private val queue = ConcurrentLinkedQueue<NsdServiceInfo>()
    private val callbackExecutor = Executors.newSingleThreadExecutor()

    fun resolve(serviceInfo: NsdServiceInfo) {
        queue.add(serviceInfo)
        checkQueue()
    }

    private fun checkQueue() {
        if (!busy.compareAndSet(false, true)) return
        val serviceInfo = queue.poll()
        if (serviceInfo == null) {
            busy.set(false)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            registerCallback(serviceInfo)
        } else {
            resolveLegacy(serviceInfo)
        }
    }

    private fun endResolving() {
        busy.set(false)
        checkQueue()
    }

    private fun resolved(serviceName: String, addresses: List<InetAddress>) {
        logger.info("service: $serviceName resolved")
        val host = addresses.firstOrNull()
        if (host == null) {
            logger.error("service: $serviceName has no address")
            return
        }
        val address =
            if (host is Inet6Address)
                "[" + host.hostAddress + "]"
            else
                host.hostAddress
        address ?: return
        CoroutineScope(Dispatchers.IO).launch {
            added(address)
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun registerCallback(serviceInfo: NsdServiceInfo) {
        val serviceName = serviceInfo.serviceName
        val callback = object : NsdManager.ServiceInfoCallback {
            private var done = false

            private fun finish() {
                if (done) return
                done = true
                try {
                    manager.unregisterServiceInfoCallback(this)
                } catch (e: IllegalArgumentException) {
                    logger.error("callback already unregistered for $serviceName", e)
                }
                endResolving()
            }

            override fun onServiceInfoCallbackRegistrationFailed(errorCode: Int) {
                logger.error("registration failed for service: $serviceName, error code: $errorCode")
                if (!done) {
                    done = true
                    endResolving()
                }
            }

            override fun onServiceUpdated(info: NsdServiceInfo) {
                resolved(serviceName, info.hostAddresses)
                finish()
            }

            override fun onServiceLost() {
                logger.error("service lost while resolving: $serviceName")
                finish()
            }

            override fun onServiceInfoCallbackUnregistered() {
            }
        }
        try {
            manager.registerServiceInfoCallback(serviceInfo, callbackExecutor, callback)
        } catch (e: IllegalArgumentException) {
            logger.error("failed to register callback for service: $serviceName", e)
            endResolving()
        }
    }

    @Suppress("DEPRECATION")
    private fun resolveLegacy(serviceInfo: NsdServiceInfo) {
        manager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
            override fun onResolveFailed(info: NsdServiceInfo, errorCode: Int) {
                logger.error("resolve failed for service: ${info.serviceName}, error code: $errorCode")
                endResolving()
            }

            override fun onServiceResolved(info: NsdServiceInfo) {
                val host = info.host
                resolved(info.serviceName, if (host == null) emptyList() else listOf(host))
                endResolving()
            }
        })
    }

    companion object {
        private val logger = Logger.getLogger(Resolver::class.java.name)
    }
}
