package org.syncloud.android.core.platform.model

import org.syncloud.android.core.redirect.model.Domain
import java.io.Serializable

class DomainModel(private val domain: Domain) : Serializable {
    val name = domain.name
    val title = domain.device_title
    fun dnsUrl(): String {
        val port = if (domain.map_local_address) domain.web_local_port else domain.web_port
        return if (port != 443) {
            "https://${domain.name}:$port"
        } else {
            "https://${domain.name}"
        }
    }
}