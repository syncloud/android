package org.syncloud.android.ui

import org.syncloud.android.core.platform.model.Endpoint
import org.syncloud.android.core.platform.model.IdentifiedEndpoint

class Progress(
        isAdded: Boolean,
        endpoint: Endpoint,
        identifiedEndpoint: IdentifiedEndpoint
) {
    var isAdded = true
    var endpoint: Endpoint
    var identifiedEndpoint: IdentifiedEndpoint

    init {
        this.isAdded = isAdded
        this.endpoint = endpoint
        this.identifiedEndpoint = identifiedEndpoint
    }
}