package com.example.avaride_1.data.remote

import com.example.avaride_1.domain.model.Destination
import com.example.avaride_1.domain.model.UserContext
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Gemini AI Service for predictive destination modeling
 * Uses Google's Gemini API to predict where the user is likely going
 */
class GeminiPredictiveService(private val apiKey: String) {

    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-pro",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.7f
                topK = 40
                topP = 0.95f
                maxOutputTokens = 1024
            }
        )
    }

    /**
     * Predict destination based on user context
     * Format: Conversational prompt that mimics Apple Intelligence behavior
     */
    suspend fun predictDestination(context: UserContext): Destination? = withContext(Dispatchers.IO) {
        try {
            val prompt = buildPredictionPrompt(context)
            val response = model.generateContent(prompt)
            parseDestinationFromResponse(response.text ?: "")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun buildPredictionPrompt(context: UserContext): String {
        val dayName = getDayName(context.dayOfWeek)
        val timeOfDay = getTimeOfDay(context.hourOfDay)

        val recentDestinationsText = if (context.recentDestinations.isNotEmpty()) {
            "Recent trips: ${context.recentDestinations.joinToString(", ") { it.name }}"
        } else {
            "No recent trip history"
        }

        val savedLocationsText = if (context.favoriteLocations.isNotEmpty()) {
            "Saved locations: ${context.favoriteLocations.entries.joinToString(", ") { "${it.key.name}: ${it.value.name}" }}"
        } else {
            ""
        }

        return """
You are an intelligent transportation assistant. Predict where the user is likely heading based on context.

Current Context:
- Location: ${context.currentLocation.address}
- Day: $dayName
- Time: ${context.hourOfDay}:00 ($timeOfDay)
- $recentDestinationsText
$savedLocationsText

Based on typical human behavior patterns and the context above, predict the most likely destination.

Respond ONLY in this exact JSON format:
{
  "destination_name": "Name of place",
  "address": "Full address",
  "confidence": 0.85,
  "reasoning": "Brief one-sentence reason"
}

Do not include any other text or explanation.
        """.trimIndent()
    }

    private fun parseDestinationFromResponse(responseText: String): Destination? {
        return try {
            // Extract JSON from response (handle potential markdown code blocks)
            val jsonText = responseText
                .substringAfter("{")
                .substringBeforeLast("}")
                .let { "{$it}" }

            val json = JSONObject(jsonText)

            Destination(
                name = json.getString("destination_name"),
                address = json.getString("address"),
                latitude = 0.0, // These would be geocoded in production
                longitude = 0.0,
                confidence = json.optDouble("confidence", 0.0).toFloat()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getDayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            7 -> "Sunday"
            else -> "Unknown"
        }
    }

    private fun getTimeOfDay(hour: Int): String {
        return when (hour) {
            in 5..11 -> "Morning"
            in 12..16 -> "Afternoon"
            in 17..20 -> "Evening"
            else -> "Night"
        }
    }
}

