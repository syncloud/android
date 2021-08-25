package org.syncloud.android

import org.syncloud.android.core.redirect.model.Domain
import org.syncloud.android.core.platform.model.DomainModel
import com.google.common.collect.Lists

object Utils {
    @JvmStatic
    fun toModels(domains: List<Domain>): List<DomainModel> {
        val models: MutableList<DomainModel> = Lists.newArrayList()
        for (domain in domains) {
            if (hasDeviceInfo(domain)) {
                val domainModel = DomainModel(domain)
                models.add(domainModel)
            }
        }
        return models
    }

    private fun hasDeviceInfo(domain: Domain): Boolean {
        return domain.device_mac_address != null && domain.device_name != null && domain.device_title != null
    }
}