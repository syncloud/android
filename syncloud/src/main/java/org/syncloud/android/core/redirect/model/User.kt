package org.syncloud.android.core.redirect.model

import java.io.Serializable

class User : Serializable {
    var active = false
    var email: String? = null
    var domains: List<Domain>? = null
}