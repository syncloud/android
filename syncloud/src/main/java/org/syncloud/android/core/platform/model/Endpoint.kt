package org.syncloud.android.core.platform.model

import java.io.Serializable

class Endpoint(val host: String, val port: Int) : Serializable {
    val activationUrl: String get() = "http://$host:81"
    override fun toString(): String = "$host:$port"
}