package com.martinszuc.phishing_emails_detection.data.model_manager.retrofit

import com.martinszuc.phishing_emails_detection.data.data_class.WeightsResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ModelWeightsService {
    @Multipart
    @POST("upload_weights")
    suspend fun uploadWeights(
        @Part clientId: MultipartBody.Part,
        @Part weightsFile: MultipartBody.Part
    ): Response<ResponseBody>

    @GET("get_weights")
    suspend fun downloadWeights(): Response<WeightsResponse>

    @GET("check")
    suspend fun checkServer(): Response<ResponseBody>
}