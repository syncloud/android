package org.syncloud.android.core.redirect

import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import org.apache.log4j.Logger
import org.syncloud.android.core.common.SyncloudException
import org.syncloud.android.core.common.WebService
import org.syncloud.android.core.common.jackson.Jackson.createObjectMapper
import org.syncloud.android.core.redirect.RedirectService
import org.syncloud.android.core.redirect.model.User
import org.syncloud.android.core.redirect.model.UserResult
import java.io.IOException
import java.util.*

class RedirectService(apiUrl: String?) : IUserService {
    private val webService: WebService
    override fun getUser(email: String?, password: String?): User? {
        val parameters: MutableList<NameValuePair> = ArrayList()
        parameters.add(BasicNameValuePair("email", email))
        parameters.add(BasicNameValuePair("password", password))
        val json = webService.execute("GET", "/user/get", parameters)
        return try {
            val restUser = mapper.readValue(json, UserResult::class.java)
            restUser.data
        } catch (e: IOException) {
            val message = "Failed to deserialize json"
            logger.error("$message $json", e)
            throw SyncloudException(message)
        }
    }

    override fun createUser(email: String?, password: String?): User? {
        val parameters: MutableList<NameValuePair> = ArrayList()
        parameters.add(BasicNameValuePair("email", email))
        parameters.add(BasicNameValuePair("password", password))
        val json = webService.execute("POST", "/user/create", parameters)
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
        fun getApiUrl(mainDomain: String): String {
            return "https://api.$mainDomain"
        }

        private val logger = Logger.getLogger(
            RedirectService::class.java
        )
        private val mapper = createObjectMapper()
    }

    init {
        webService = WebService(apiUrl!!)
    }
}