package org.syncloud.android.core.platform

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.log4j.Logger
import org.syncloud.android.core.common.Result
import org.syncloud.android.core.common.SyncloudException
import org.syncloud.android.core.common.WebService
import org.syncloud.android.core.platform.model.Identification
import java.io.IOException

class Internal(private val webService: WebService) {

    private val logger = Logger.getLogger(Internal::class.java)
    private val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)


    fun getId(host: String): Identification? {
         try {
             val json = webService.getUnverified("https://$host/rest/id")
             try {
                 val result = mapper.readValue<Result<Identification>>(json)
                 return result.data
             } catch (e: IOException) {
                 val message = "Unable to parse identification response"
                 logger.error("$message $json", e)
             }
         } catch (e: SyncloudException) {
            val message = "Unable to get identification response"
            logger.error(message, e)
        }
        return null
    }

}