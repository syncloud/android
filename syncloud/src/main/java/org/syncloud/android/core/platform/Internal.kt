package org.syncloud.android.core.platform

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Optional
import org.apache.log4j.Logger
import org.syncloud.android.core.common.Result
import org.syncloud.android.core.common.SyncloudException
import org.syncloud.android.core.common.WebService
import org.syncloud.android.core.platform.Internal
import org.syncloud.android.core.platform.model.Identification
import java.io.IOException

class Internal {
    private fun getRestUrl(host: String): String = String.format("https://%s/rest", host)
    private fun getRestWebService(host: String): WebService = WebService(getRestUrl(host))
    fun getId(host: String): Optional<Identification?> {
        val webService = getRestWebService(host)
        val json: String
        json = try {
            webService.execute("GET", "/id")
        } catch (e: SyncloudException) {
            val message = "Unable to get identification response"
            logger.error(message, e)
            return Optional.absent()
        }
        return try {
            val result = JSON.readValue<Result<Identification>>(
                json,
                object : TypeReference<Result<Identification?>?>() {})
            Optional.of(result.data)
        } catch (e: IOException) {
            val message = "Unable to parse identification response"
            logger.error("$message $json", e)
            Optional.absent()
        }
    }

    companion object {
        private val logger = Logger.getLogger(Internal::class.java)
        val JSON = ObjectMapper()
    }
}