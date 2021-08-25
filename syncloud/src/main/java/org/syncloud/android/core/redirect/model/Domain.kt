package org.syncloud.android.core.redirect.model

import java.io.Serializable

data class Domain(
    val name: String,
    val device_mac_address: String,
    val device_name : String,
    var device_title: String,
    var map_local_address: Boolean,
    var web_protocol: String,
    var web_local_port: Int,
    var web_port: Int
) : Serializable