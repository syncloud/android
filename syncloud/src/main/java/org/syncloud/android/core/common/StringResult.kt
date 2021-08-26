package org.syncloud.android.core.common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.syncloud.android.core.common.BaseResult

@JsonIgnoreProperties(ignoreUnknown = true)
class StringResult : BaseResult() {
    var data: String? = null
}