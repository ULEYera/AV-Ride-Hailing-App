package com.example.avaride_1.presentation.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avaride_1.BuildConfig
import com.example.avaride_1.data.remote.GroqApiService
import com.example.avaride_1.data.remote.GroqMessage
import com.example.avaride_1.data.remote.GroqRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Groq API Service
    private val groqService: GroqApiService

    init {
        val apiKey = BuildConfig.GROQ_API_KEY

        // Logging Interceptor
        val logging = okhttp3.logging.HttpLoggingInterceptor()
        logging.setLevel(okhttp3.logging.HttpLoggingInterceptor.Level.BODY)

        val client = OkHttpClient.Builder()
            .addInterceptor(logging) // Add logging
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.groq.com/openai/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        groqService = retrofit.create(GroqApiService::class.java)
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(text = text, isUser = true)
        _messages.value += userMessage
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Prepare conversation history
                val groqMessages = listOf(
                    GroqMessage(role = "system", content = "You are Ava, a helpful ride-hailing assistant."),
                    GroqMessage(role = "user", content = text)
                )

                val request = GroqRequest(
                    messages = groqMessages,
                    model = "llama-3.3-70b-versatile" // Updated to supported model
                )

                val response = groqService.chatCompletion(request)
                val aiText = response.choices.firstOrNull()?.message?.content ?: "No response from AI."

                val aiMessage = ChatMessage(text = aiText, isUser = false)
                _messages.value += aiMessage
            } catch (e: Exception) {
                val errorMessage = ChatMessage(text = "Error: ${e.localizedMessage}", isUser = false)
                _messages.value += errorMessage
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Adds a message from the AI directly (e.g. system greeting).
     */
    fun addAiMessage(text: String) {
        val message = ChatMessage(text = text, isUser = false)
        _messages.value += message
    }
}
