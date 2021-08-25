package org.syncloud.android.core.platform.model

import java.io.Serializable

class Endpoint(private val host: String, private val port: Int) : Serializable {
    fun host(): String {
        return host
    }

    fun port(): Int {
        return port
    }

    fun activationUrl(): String {
        return "http://$host:81"
    }

    override fun toString(): String {
        return "$host:$port"
    }
}