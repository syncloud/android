package org.syncloud.android.core.common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.syncloud.android.core.common.BaseResult

@JsonIgnoreProperties(ignoreUnknown = true)
class Result<T> : BaseResult() {
    @JvmField
    var data: T? = null
}