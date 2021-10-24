package org.syncloud.android.core.redirect

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.log4j.Logger
import org.syncloud.android.core.common.SyncloudException
import org.syncloud.android.core.common.WebService
import org.syncloud.android.core.redirect.model.User
import org.syncloud.android.core.redirect.model.UserCredentials
import org.syncloud.android.core.redirect.model.UserResult
import java.io.IOException

class RedirectService(private val mainDomain: String, private val webService: WebService) : IUserService {

    private val logger = Logger.getLogger(RedirectService::class.java)
    private val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    override fun getUser(email: String, password: String): User? {
        val url = "https://api.$mainDomain/user"
        val requestJson = mapper.writeValueAsString(UserCredentials(email, password))
        val json = webService.post(requestJson, url)
        return try {
            val restUser = mapper.readValue<UserResult>(json)
            restUser.data
        } catch (e: IOException) {
            val message = "Failed to deserialize json"
            logger.error("$message $json", e)
            throw SyncloudException(message)
        }
    }
}