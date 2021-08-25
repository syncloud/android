package org.syncloud.android.core.common

open class BaseResult {
    var success = false
    var message: String? = null
    var parameters_messages: List<ParameterMessages>? = null
}