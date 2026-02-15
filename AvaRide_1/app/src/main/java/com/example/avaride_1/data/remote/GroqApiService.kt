package com.example.avaride_1.data.remote

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

import com.google.gson.annotations.SerializedName

// Models
data class GroqRequest(
    @SerializedName("messages") val messages: List<GroqMessage>,
    @SerializedName("model") val model: String = "llama-3.3-70b-versatile" // Updated to supported model
)

data class GroqMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class GroqResponse(
    @SerializedName("choices") val choices: List<GroqChoice>
)

data class GroqChoice(
    @SerializedName("message") val message: GroqMessage
)

// API Service
interface GroqApiService {
    @POST("v1/chat/completions")
    suspend fun chatCompletion(@Body request: GroqRequest): GroqResponse
}
