package org.syncloud.android.core.common

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.log4j.Logger
import org.syncloud.android.core.common.http.HttpClient
import java.io.IOException

open class WebService(private val httpClient: HttpClient) {

    private val logger = Logger.getLogger(WebService::class.java)
    private val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    open fun execute(type: String, url: String, parameters: List<Pair<String, String>> = ArrayList()): String {
        val response = httpClient.execute(type, url, parameters)
        try {
            val jsonBaseResponse = mapper.readValue<BaseResult>(response.output)
            if (!jsonBaseResponse.success) {
                val message = "Returned JSON indicates an error"
                logger.error(message + " " + response.output)
                throw SyncloudResultException(message, jsonBaseResponse)
            }
            return response.output

        } catch (e: IOException) {
            val message = "Failed to deserialize json"
            logger.error(message + " " + response.output, e)
            throw SyncloudException(message)
        }
    }

}