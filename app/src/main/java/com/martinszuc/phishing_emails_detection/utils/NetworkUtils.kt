package com.martinszuc.phishing_emails_detection.utils

import android.content.Context
import com.martinszuc.phishing_emails_detection.R
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

object NetworkUtils {

    fun createCustomTrustManager(context: Context): TrustManagerFactory {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val inputStream = context.resources.openRawResource(R.raw.cert) // Replace 'cert' with your certificate file's actual name
        val certificate = certificateFactory.generateCertificate(inputStream)
        inputStream.close()

        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)
        keyStore.setCertificateEntry("ca", certificate)

        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)
        return trustManagerFactory
    }

    fun createSSLSocketFactory(context: Context): SSLSocketFactory {
        val trustManagerFactory = createCustomTrustManager(context)
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustManagerFactory.trustManagers, SecureRandom())
        return sslContext.socketFactory
    }
}
