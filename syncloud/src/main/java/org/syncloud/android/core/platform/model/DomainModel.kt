package org.syncloud.android.core.platform.model

import org.syncloud.android.core.redirect.model.Domain
import org.syncloud.android.core.platform.model.Identification
import org.syncloud.android.core.platform.model.DomainModel
import java.io.Serializable

class DomainModel(private val domain: Domain) : Serializable {
    private val id: Identification?

    init {
        id = deviceId(domain)
    }

    fun name(): String {
        return domain.name
    }

    fun id(): Identification? {
        return id
    }

    val dnsUrl: String
        get() {
            val port = if (domain.map_local_address) domain.web_local_port else domain.web_port
            var url = String.format("%s://%s", domain.web_protocol, domain.name)
            if (domain.web_protocol == "http" && port != 80 ||
                domain.web_protocol == "https" && port != 443
            ) url += ":" + port as Int
            return url
        }

    override fun toString(): String {
        return "DomainModel{" +
                "domain=" + domain +
                ", id=" + id +
                '}'
    }

    companion object {
        private fun deviceId(domain: Domain): Identification? {
            return if (domain.device_mac_address != null && domain.device_name != null && domain.device_title != null) Identification(
                domain.device_mac_address,
                domain.device_name,
                domain.device_title
            ) else null
        }
    }


}