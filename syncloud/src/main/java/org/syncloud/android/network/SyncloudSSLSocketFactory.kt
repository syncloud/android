package org.syncloud.android.network

import org.apache.http.conn.ssl.SSLSocketFactory
import kotlin.Throws
import org.syncloud.android.network.SyncloudSSLSocketFactory.EasyX509TrustManager
import java.io.IOException
import java.net.Socket
import java.net.UnknownHostException
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class SyncloudSSLSocketFactory(truststore: KeyStore?) : SSLSocketFactory(truststore) {
    inner class EasyX509TrustManager(keystore: KeyStore?) : X509TrustManager {
        private var standardTrustManager: X509TrustManager? = null
        @Throws(CertificateException::class)
        override fun checkClientTrusted(certificates: Array<X509Certificate>, authType: String) {
            standardTrustManager!!.checkClientTrusted(certificates, authType)
        }

        @Throws(CertificateException::class)
        override fun checkServerTrusted(certificates: Array<X509Certificate>, authType: String) {
            if (certificates != null && (certificates.size == 1 || certificates.size == 2)) {
                certificates[0].checkValidity()
                if (certificates.size == 2) certificates[1].checkValidity()
            } else {
                standardTrustManager!!.checkServerTrusted(certificates, authType)
            }
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return standardTrustManager!!.acceptedIssuers
        }

        init {
            val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            factory.init(keystore)
            val trustmanagers = factory.trustManagers
            if (trustmanagers.size == 0) {
                throw NoSuchAlgorithmException("no trust manager found")
            }
            standardTrustManager = trustmanagers[0] as X509TrustManager
        }
    }

    var sslContext = SSLContext.getInstance("TLS")
    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(socket: Socket, host: String, port: Int, autoClose: Boolean): Socket {
        return sslContext.socketFactory.createSocket(socket, host, port, autoClose)
    }

    @Throws(IOException::class)
    override fun createSocket(): Socket {
        return sslContext.socketFactory.createSocket()
    }

    init {
        val tm: TrustManager = EasyX509TrustManager(truststore)
        sslContext.init(null, arrayOf(tm), null)
    }
}