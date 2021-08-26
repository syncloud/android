package org.syncloud.android.core.platform.model

data class Identification(val mac_address: String?, val name: String?, val title: String?) {
    override fun toString(): String {
        return "Identification{" +
                "name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", mac_address='" + mac_address + '\'' +
                '}'
    }
}