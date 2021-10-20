package org.syncloud.android.core.common.http

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.apache.log4j.Logger
import org.syncloud.android.core.common.WebService

open class HttpClient {

    private val logger = Logger.getLogger(WebService::class.java)

    open fun execute(request: Request): Response {
        logger.info("calling: ${request.url}")
        return OkHttpClient().newCall(request).execute()
    }
}