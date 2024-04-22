package com.martinszuc.phishing_emails_detection.data.network.retrofit

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Streaming

/**
 * Interface for the service that manages the model weights.
 *
 * Authored by matoszuc@gmail.com
 */
interface ModelWeightsService {

    /**
     * Uploads the model weights to the server.
     *
     * @param clientId The ID of the client.
     * @param weightsFile The file containing the model weights.
     * @return The server's response.
     */
    @Multipart
    @POST("upload_weights")
    suspend fun uploadWeights(
        @Part clientId: MultipartBody.Part,
        @Part weightsFile: MultipartBody.Part
    ): Response<ResponseBody>

    /**
     * Downloads the model weights from the server.
     *
     * @return The server's response containing the model weights.
     */
    @Streaming
    @GET("get_weights")
    suspend fun downloadWeights(): Response<ResponseBody>

    /**
     * Checks the server's status.
     *
     * @return The server's response.
     */
    @GET("check")
    suspend fun checkServer(): Response<ResponseBody>
}
