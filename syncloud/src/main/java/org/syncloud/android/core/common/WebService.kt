package org.syncloud.android.core.common

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.log4j.Logger
import org.syncloud.android.core.common.http.HttpClient
import java.io.IOException

open class WebService(private val client: HttpClient) {

    private val logger = Logger.getLogger(WebService::class.java)
    private val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    open fun post(requestJson: String, url: String): String {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestJson.toRequestBody(mediaType)
        val request = Request.Builder().url(url).post(body).build()
        return execute(request)
    }

    open fun get(url: String): String {
        val request = Request.Builder().url(url).get().build()
        return execute(request)
    }

    private fun execute(request: Request): String {
        client.execute(request).use { response ->
            val responseBody = response.body
            if (responseBody != null) {
                val json = responseBody.string()
                try {
                    val jsonBaseResponse = mapper.readValue<BaseResult>(json)
                    if (!jsonBaseResponse.success) {
                        val message = "Returned JSON indicates an error"
                        logger.error("$message $json")
                        throw SyncloudResultException(message, jsonBaseResponse)
                    }
                    return json

                } catch (e: IOException) {
                    val message = "Failed to deserialize json"
                    logger.error("$message $json", e)
                    throw SyncloudException(message)
                }
            } else {
                val message = "empty response"
                logger.error(message)
                throw SyncloudException(message)
            }
        }
    }

}