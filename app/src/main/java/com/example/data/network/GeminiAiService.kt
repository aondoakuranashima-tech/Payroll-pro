package com.example.data.network

import com.example.BuildConfig
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Secure AI gateway for Payroll Pro.
 *
 * The class name is retained for compatibility with the existing repository,
 * but it no longer calls Gemini directly. Provider credentials stay on the
 * Softwall backend, which owns Gemini/OpenAI routing and fallback behavior.
 */
class GeminiAiService {
  private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

  suspend fun askCopilot(
    userPrompt: String,
    conversationHistory: List<ChatMessage> = emptyList()
  ): ChatMessage = withContext(Dispatchers.IO) {
    val baseUrl = BuildConfig.SOFTWALL_API_BASE_URL.trimEnd('/')
    val payload = JSONObject().apply {
      put("question", userPrompt.trim())
    }

    val request = Request.Builder()
      .url("$baseUrl/ai/assistant")
      .post(payload.toString().toRequestBody("application/json".toMediaType()))
      .header("Accept", "application/json")
      .build()

    try {
      client.newCall(request).execute().use { response ->
        val body = response.body?.string().orEmpty()
        if (!response.isSuccessful) {
          throw IllegalStateException("Softwall AI request failed (${response.code})")
        }

        val json = JSONObject(body)
        val answer = json.optString("answer").trim()
        if (answer.isBlank()) throw IllegalStateException("Softwall AI returned an empty answer")

        val suggestions = mutableListOf<String>()
        json.optJSONArray("suggestions")?.let { items ->
          for (i in 0 until items.length()) {
            items.optString(i).takeIf { it.isNotBlank() }?.let(suggestions::add)
          }
        }

        ChatMessage(
          sender = MessageSender.AI_COPILOT,
          text = answer,
          suggestedPrompts = suggestions
        )
      }
    } catch (_: Exception) {
      ChatMessage(
        sender = MessageSender.AI_COPILOT,
        text = "The Softwall AI service is temporarily unavailable. Please try again in a moment.",
        suggestedPrompts = listOf(
          "Review payroll anomalies",
          "Check employee deductions",
          "Generate payroll cost forecast"
        )
      )
    }
  }
}
