package org.syncloud.android.core.redirect

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.log4j.Logger
import org.syncloud.android.core.common.SyncloudException
import org.syncloud.android.core.common.WebService
import org.syncloud.android.core.redirect.model.User
import org.syncloud.android.core.redirect.model.UserResult
import java.io.IOException

class RedirectService(private val webService: WebService) : IUserService {
    private val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    override fun getUser(email: String, password: String): User? {
        val json = webService.execute("GET", "/user/get", listOf(Pair("email", email), Pair("password", password)))
        return try {
            val restUser = mapper.readValue<UserResult>(json)
            restUser.data
        } catch (e: IOException) {
            val message = "Failed to deserialize json"
            logger.error("$message $json", e)
            throw SyncloudException(message)
        }
    }

    override fun createUser(email: String, password: String): User? {
        val json = webService.execute("POST", "/user/create", listOf(Pair("email", email), Pair("password", password)))
        return try {
            val restUser = mapper.readValue(json, UserResult::class.java)
            restUser.data
        } catch (e: IOException) {
            val message = "Failed to deserialize json"
            logger.error("$message $json", e)
            throw SyncloudException(message)
        }
    }

    companion object {
        fun getApiUrl(mainDomain: String): String = "https://api.$mainDomain"
        private val logger = Logger.getLogger(RedirectService::class.java)
    }
}