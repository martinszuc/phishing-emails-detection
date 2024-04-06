package com.martinszuc.phishing_emails_detection.data.model_manager.retrofit

import com.martinszuc.phishing_emails_detection.data.data_class.WeightData
import com.martinszuc.phishing_emails_detection.data.data_class.WeightsResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ModelWeightsService {
    @POST("upload_weights")
    suspend fun uploadWeights(@Body weightsData: WeightData): Response<ResponseBody>

    @GET("get_weights")
    suspend fun downloadWeights(): Response<WeightsResponse>
}