package org.syncloud.android.core.redirect.model

class User(
    val active: Boolean = false,
    val email: String? = null,
    val domains: List<Domain>? = null
)