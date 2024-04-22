package com.martinszuc.phishing_emails_detection.di

import android.content.Context
import com.martinszuc.phishing_emails_detection.data.data_repository.remote.repository.BaseUrlRepository
import com.martinszuc.phishing_emails_detection.data.data_repository.remote.network.retrofit.ModelWeightsService
import com.martinszuc.phishing_emails_detection.utils.NetworkUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Arrays
import javax.inject.Singleton
import javax.net.ssl.X509TrustManager

/**
 * Authored by matoszuc@gmail.com
 */

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideBaseUrlRepository(@ApplicationContext context: Context): BaseUrlRepository {
        return BaseUrlRepository(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val trustManagerFactory = NetworkUtils.createCustomTrustManager(context)
        val trustManagers = trustManagerFactory.trustManagers
        if (trustManagers.size != 1 || trustManagers[0] !is X509TrustManager) {
            throw IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers))
        }

        val sslSocketFactory = NetworkUtils.createSSLSocketFactory(context)

        return OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustManagers[0] as X509TrustManager)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, baseUrlRepository: BaseUrlRepository): Retrofit {
        val baseUrl = baseUrlRepository.getBaseUrl()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideModelWeightsService(retrofit: Retrofit): ModelWeightsService {
        return retrofit.create(ModelWeightsService::class.java)
    }
}
