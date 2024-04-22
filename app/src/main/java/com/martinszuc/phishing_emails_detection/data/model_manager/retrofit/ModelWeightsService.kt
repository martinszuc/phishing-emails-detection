package com.martinszuc.phishing_emails_detection.data.model_manager.retrofit

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Streaming

interface ModelWeightsService {
    @Multipart
    @POST("upload_weights")
    suspend fun uploadWeights(
        @Part clientId: MultipartBody.Part,
        @Part weightsFile: MultipartBody.Part
    ): Response<ResponseBody>

    @Streaming
    @GET("get_weights")
    suspend fun downloadWeights(): Response<ResponseBody>

    @GET("check")
    suspend fun checkServer(): Response<ResponseBody>
}