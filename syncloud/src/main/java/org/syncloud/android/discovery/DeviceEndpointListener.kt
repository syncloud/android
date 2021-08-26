package org.syncloud.android.discovery

import org.syncloud.android.core.platform.model.Endpoint

interface DeviceEndpointListener {
    fun added(endpoint: Endpoint?)
    fun removed(endpoint: Endpoint?)
}