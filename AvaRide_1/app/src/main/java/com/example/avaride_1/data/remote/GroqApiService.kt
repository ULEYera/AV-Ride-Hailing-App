package com.example.avaride_1.data.remote

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// Models
data class GroqRequest(
    val messages: List<GroqMessage>,
    val model: String = "llama3-8b-8192" // Default or user-selectable
)

data class GroqMessage(
    val role: String,
    val content: String
)

data class GroqResponse(
    val choices: List<GroqChoice>
)

data class GroqChoice(
    val message: GroqMessage
)

// API Service
interface GroqApiService {
    @POST("v1/chat/completions")
    suspend fun chatCompletion(@Body request: GroqRequest): GroqResponse
}
