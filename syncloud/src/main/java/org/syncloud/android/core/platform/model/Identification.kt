package org.syncloud.android.core.platform.model

import java.io.Serializable

class Identification : Serializable {
    var name: String? = null
    var title: String? = null
    var mac_address: String? = null

    constructor() {}
    constructor(mac_address: String?, name: String?, title: String?) {
        this.name = name
        this.title = title
        this.mac_address = mac_address
    }

    fun macAddress(): String? {
        return mac_address
    }

    fun name(): String? {
        return name
    }

    fun title(): String? {
        return title
    }

    override fun toString(): String {
        return "Identification{" +
                "name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", mac_address='" + mac_address + '\'' +
                '}'
    }
}