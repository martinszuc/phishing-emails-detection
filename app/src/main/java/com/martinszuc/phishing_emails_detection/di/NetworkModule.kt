package com.martinszuc.phishing_emails_detection.di

import com.martinszuc.phishing_emails_detection.data.model_manager.retrofit.ModelWeightsService

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://192.168.114.78:5000/") // TODO

            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideModelWeightsService(retrofit: Retrofit): ModelWeightsService {
        return retrofit.create(ModelWeightsService::class.java)
    }
}
