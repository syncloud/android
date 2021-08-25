package org.syncloud.android.core.platform.model

import com.google.common.base.Optional
import org.syncloud.android.core.platform.model.Identification
import java.io.Serializable

class IdentifiedEndpoint(private val enpoint: Endpoint, private val id: Optional<Identification>) :
    Serializable {
    fun endpoint(): Endpoint {
        return enpoint
    }

    fun id(): Optional<Identification> {
        return id
    }
}