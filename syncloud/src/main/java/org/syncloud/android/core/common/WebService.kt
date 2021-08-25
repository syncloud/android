package org.syncloud.android.core.common

import org.syncloud.android.core.common.jackson.Jackson.createObjectMapper
import org.apache.http.NameValuePair
import kotlin.jvm.JvmOverloads
import org.syncloud.android.core.common.WebService
import org.apache.http.client.methods.HttpUriRequest
import org.syncloud.android.core.common.BaseResult
import org.syncloud.android.core.common.SyncloudException
import org.syncloud.android.core.common.SyncloudResultException
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.conn.ssl.AllowAllHostnameVerifier
import org.apache.http.conn.ssl.SSLContextBuilder
import org.apache.http.conn.ssl.TrustStrategy
import com.fasterxml.jackson.databind.ObjectMapper
import org.syncloud.android.core.common.jackson.Jackson
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.log4j.Logger
import java.io.*
import java.lang.Exception
import java.lang.StringBuilder
import java.net.URLEncoder
import java.security.cert.X509Certificate
import java.util.ArrayList
import kotlin.Throws

class WebService {
    private var apiUrl = ""

    constructor() {}
    constructor(apiUrl: String) {
        this.apiUrl = apiUrl
    }

    private fun parametersToString(parameters: List<NameValuePair>): String {
        val builder = StringBuilder()
        for (pair in parameters) {
            builder.append(pair.name)
            builder.append("=")
            builder.append(pair.value)
            builder.append(" ")
        }
        return builder.toString()
    }

    @JvmOverloads
    fun execute(type: String, url: String, parameters: List<NameValuePair> = ArrayList()): String {
        val fullUrl = apiUrl + url
        logger.info("calling: $fullUrl")
        val request = request(type, fullUrl, parameters)
        val response = getResponse(request)
        val jsonBaseResponse: BaseResult
        jsonBaseResponse = try {
            mapper.readValue(response.output, BaseResult::class.java)
        } catch (e: IOException) {
            val message = "Failed to deserialize json"
            logger.error(message + " " + response.output, e)
            throw SyncloudException(message)
        }
        if (!jsonBaseResponse.success) {
            val message = "Returned JSON indicates an error"
            logger.error(message + " " + response.output)
            throw SyncloudResultException(message, jsonBaseResponse)
        }
        return response.output
    }

    private inner class Response(var statusCode: Int, var output: String)

    private fun getResponse(request: HttpUriRequest): Response {
        var response: CloseableHttpResponse? = null
        return try {
            val http = HttpClients
                .custom()
                .setHostnameVerifier(AllowAllHostnameVerifier()).setSslcontext(SSLContextBuilder()
                    .loadTrustMaterial(null) { arg0: Array<X509Certificate?>?, arg1: String? -> true }
                    .build()
                ).build()
            response = http.execute(request)
            val jsonResponse = response.entity.content
            val textJsonResponse = readText(jsonResponse)
            val statusCode = response.statusLine.statusCode
            Response(statusCode, textJsonResponse)
        } catch (e: Exception) {
            val message = "Failed to get response"
            logger.error("Failed to get response", e)
            throw SyncloudException(message)
        } finally {
            if (response != null) try {
                response.close()
            } catch (ignore: IOException) {
            }
        }
    }

    companion object {
        private val logger = Logger.getLogger(WebService::class.java)
        private val mapper = createObjectMapper()
        private fun request(
            type: String,
            url: String,
            parameters: List<NameValuePair>
        ): HttpUriRequest {
            try {
                if (type.toUpperCase() == "POST") {
                    val post = HttpPost(url)
                    post.entity = UrlEncodedFormEntity(parameters)
                    return post
                }
                if (type.toUpperCase() == "GET") {
                    var urlFull: String? = url
                    for (pair in parameters) {
                        val first = pair === parameters[0]
                        urlFull += if (first) "?" else "&"
                        urlFull += pair.name
                        urlFull += "="
                        urlFull += URLEncoder.encode(pair.value, "utf-8")
                    }
                    return HttpGet(urlFull)
                }
            } catch (e: UnsupportedEncodingException) {
                val message = "Failed to form request"
                logger.error(message, e)
                throw SyncloudException(message)
            }
            val message = "Unknown request type $type"
            logger.error(message)
            throw SyncloudException(message)
        }

        @Throws(IOException::class)
        private fun readText(inputStream: InputStream): String {
            val r = BufferedReader(InputStreamReader(inputStream))
            val total = StringBuilder()
            var line: String?
            while (r.readLine().also { line = it } != null) {
                total.append(line)
            }
            return total.toString()
        }
    }
}