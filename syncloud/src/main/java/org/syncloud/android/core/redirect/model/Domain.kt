package org.syncloud.android.core.redirect.model

import com.google.common.collect.Lists
import org.syncloud.android.core.platform.model.DomainModel
import java.io.Serializable

data class Domain(
    val name: String,
    val device_name : String,
    var device_title: String,
    var map_local_address: Boolean,
    var web_protocol: String,
    var web_local_port: Int,
    var web_port: Int
) : Serializable

fun List<Domain>.toModels(): List<DomainModel> {
    val models: MutableList<DomainModel> = Lists.newArrayList()
    for (domain in this) {
        if (hasDeviceInfo(domain)) {
            val domainModel = DomainModel(domain)
            models.add(domainModel)
        }
    }
    return models
}

fun hasDeviceInfo(domain: Domain): Boolean = domain.device_title != null