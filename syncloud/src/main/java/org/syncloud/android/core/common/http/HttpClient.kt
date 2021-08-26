package org.syncloud.android.core.common.http

import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.conn.ssl.AllowAllHostnameVerifier
import org.apache.http.conn.ssl.SSLContextBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.apache.log4j.Logger
import org.syncloud.android.core.common.SyncloudException
import org.syncloud.android.core.common.WebService
import java.io.*
import java.net.URLEncoder
import java.security.cert.X509Certificate

open class HttpClient {

    private val logger = Logger.getLogger(WebService::class.java)

    open fun execute(type: String, url: String, parameters: List<Pair<String, String>>): Response {
        logger.info("calling: $url")
        val request = request(type, url, parameters)
        return getResponse(request)
    }

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
            } catch (e: IOException) {
                logger.error("Failed to close response", e)
            }
        }
    }

    private fun request(
            type: String,
            url: String,
            parameters: List<Pair<String, String>>
    ): HttpUriRequest {
        try {
            if (type.uppercase() == "POST") {
                val post = HttpPost(url)
                post.entity = UrlEncodedFormEntity(parameters.map { (name, value) -> BasicNameValuePair(name, value) })
                return post
            }
            if (type.uppercase() == "GET") {
                var urlFull: String? = url
                for (pair in parameters) {
                    val first = pair === parameters[0]
                    urlFull += if (first) "?" else "&"
                    urlFull += pair.first
                    urlFull += "="
                    urlFull += URLEncoder.encode(pair.second, "utf-8")
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