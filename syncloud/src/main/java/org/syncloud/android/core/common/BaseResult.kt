package org.syncloud.android.core.common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.syncloud.android.core.common.ParameterMessages

@JsonIgnoreProperties(ignoreUnknown = true)
open class BaseResult {
    @JvmField
    var success = false
    var message: String? = null
    @JvmField
    var parameters_messages: List<ParameterMessages>? = null
}