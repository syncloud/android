package org.syncloud.android.core.platform.model

import com.google.common.base.Optional
import org.syncloud.android.core.platform.model.Identification
import java.io.Serializable

class IdentifiedEndpoint(val endpoint: Endpoint, val id: Optional<Identification>) : Serializable