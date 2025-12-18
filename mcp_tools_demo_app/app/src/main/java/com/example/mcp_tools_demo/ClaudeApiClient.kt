package com.example.mcp_tools_demo

import com.mapbox.mcp.models.ToolDefinition
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Client for communicating with Claude API (Anthropic Messages API).
 *
 * Handles:
 * - Sending messages with tool definitions
 * - Parsing tool_use blocks from responses
 * - Sending tool results back to Claude
 */
class ClaudeApiClient(private val apiKey: String) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    /**
     * Sends a message to Claude API with optional tools.
     *
     * @param messages Conversation history
     * @param tools Tool definitions to make available to Claude
     * @param model Claude model to use (defaults to Config.CLAUDE_MODEL)
     * @param maxTokens Maximum tokens for response (defaults to Config.MAX_TOKENS)
     * @return Claude's response
     */
    suspend fun sendMessage(
        messages: List<Message>,
        tools: List<ToolDefinition> = emptyList(),
        model: String = Config.CLAUDE_MODEL,
        maxTokens: Int = Config.MAX_TOKENS
    ): ClaudeResponse {
        val requestBody = ClaudeRequest(
            model = model,
            maxTokens = maxTokens,
            messages = messages,
            tools = if (tools.isNotEmpty()) tools else null
        )

        val requestJson = json.encodeToString(ClaudeRequest.serializer(), requestBody)

        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .header("content-type", "application/json")
            .post(requestJson.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
            ?: throw Exception("Empty response from Claude API")

        if (!response.isSuccessful) {
            throw Exception("Claude API error: ${response.code} - $responseBody")
        }

        return json.decodeFromString(ClaudeResponse.serializer(), responseBody)
    }
}

// ========== Request/Response Models ==========

@Serializable
data class ClaudeRequest(
    val model: String,
    @SerialName("max_tokens")
    val maxTokens: Int,
    val messages: List<Message>,
    val tools: List<ToolDefinition>? = null
)

@Serializable
data class Message(
    val role: String, // "user" or "assistant"
    val content: List<ContentBlock>
)

@Serializable
data class ContentBlock(
    val type: String, // "text", "tool_use", or "tool_result"
    val text: String? = null,
    val id: String? = null, // For tool_use and tool_result
    val name: String? = null, // For tool_use
    val input: Map<String, JsonElement>? = null, // For tool_use
    @SerialName("tool_use_id")
    val toolUseId: String? = null, // For tool_result
    val content: String? = null // For tool_result
)

@Serializable
data class ClaudeResponse(
    val id: String,
    val type: String = "message",
    val role: String,
    val content: List<ContentBlock>,
    val model: String,
    @SerialName("stop_reason")
    val stopReason: String? = null,
    val usage: Usage
)

@Serializable
data class Usage(
    @SerialName("input_tokens")
    val inputTokens: Int,
    @SerialName("output_tokens")
    val outputTokens: Int
)
