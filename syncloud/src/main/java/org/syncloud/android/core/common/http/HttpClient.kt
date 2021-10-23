package org.syncloud.android.core.common.http

import android.annotation.SuppressLint
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.apache.log4j.Logger
import org.syncloud.android.core.common.WebService
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

open class HttpClient {

    private val logger = Logger.getLogger(WebService::class.java)

    open fun execute(request: Request): Response {
        logger.info("calling: ${request.url}")
        return OkHttpClient.Builder().build().newCall(request).execute()
    }

    open fun executeUnverified(request: Request): Response {
        logger.info("calling: ${request.url}")
        val trustAllCert = @SuppressLint("CustomX509TrustManager")
        object : X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            override fun checkClientTrusted(chain: Array<X509Certificate?>?, authType: String?) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkServerTrusted(chain: Array<X509Certificate?>?, authType: String?) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf(trustAllCert), SecureRandom())
        val sslSocketFactory = sslContext.socketFactory

        val httpBuilder = OkHttpClient.Builder()
        httpBuilder.sslSocketFactory(sslSocketFactory, trustAllCert)
        httpBuilder.hostnameVerifier { _, _ -> true }
        return httpBuilder.build().newCall(request).execute()
    }

}