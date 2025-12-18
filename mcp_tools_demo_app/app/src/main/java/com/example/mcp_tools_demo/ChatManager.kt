package com.example.mcp_tools_demo

import android.util.Log
import com.mapbox.mcp.MapboxMapTools
import com.mapbox.mcp.models.ToolResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*

/**
 * Manages the conversation flow between the user, Claude API, and MapboxMapTools.
 *
 * Flow:
 * 1. User sends message
 * 2. Send to Claude with tool definitions
 * 3. Parse response for tool_use blocks
 * 4. Execute tools via MapboxMapTools
 * 5. Send tool results back to Claude
 * 6. Return Claude's final response
 */
class ChatManager(
    private val claudeApiClient: ClaudeApiClient,
    private val mapboxMapTools: MapboxMapTools
) {
    private val conversationHistory = mutableListOf<Message>()
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val TAG = "ChatManager"
    }

    /**
     * Process a user message through the full conversation + tool execution flow.
     *
     * @param userMessage The user's message text
     * @return Claude's final response text
     */
    suspend fun processMessage(userMessage: String): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Processing message: $userMessage")

            // 1. Add user message to history
            conversationHistory.add(
                Message(
                    role = "user",
                    content = listOf(ContentBlock(type = "text", text = userMessage))
                )
            )

            // 2. Get tool definitions from MapboxMapTools
            val tools = mapboxMapTools.getToolsForLLM()
            Log.d(TAG, "Available tools: ${tools.map { it.name }}")

            // 3. Send to Claude API with tools
            var response = claudeApiClient.sendMessage(
                messages = conversationHistory,
                tools = tools
            )

            Log.d(TAG, "Claude response: ${response.content.size} content blocks")

            // 4. Check if Claude wants to use tools
            var toolUseBlocks = response.content.filter { it.type == "tool_use" }

            // 5. Execute tools if Claude requested them
            while (toolUseBlocks.isNotEmpty()) {
                Log.d(TAG, "Found ${toolUseBlocks.size} tool use blocks")

                // Add Claude's response (including tool_use) to history
                conversationHistory.add(
                    Message(
                        role = "assistant",
                        content = response.content
                    )
                )

                // Execute each tool and collect results
                val toolResults = mutableListOf<ContentBlock>()

                toolUseBlocks.forEach { toolUseBlock ->
                    val toolName = toolUseBlock.name ?: "unknown"
                    val toolId = toolUseBlock.id ?: ""
                    val toolInput = toolUseBlock.input ?: emptyMap()

                    Log.d(TAG, "Executing tool: $toolName with params: $toolInput")

                    // Convert JsonElement params to Map<String, Any?>
                    val params = convertJsonElementToMap(toolInput)

                    // Execute the tool
                    val result = mapboxMapTools.executeTool(toolName, params)

                    Log.d(TAG, "Tool result: $result")

                    // Create tool_result content block
                    val resultContent = when (result) {
                        is ToolResult.Success -> result.data ?: "Success"
                        is ToolResult.Error -> "Error: ${result.message}"
                    }

                    toolResults.add(
                        ContentBlock(
                            type = "tool_result",
                            toolUseId = toolId,
                            content = resultContent
                        )
                    )
                }

                // 6. Send tool results back to Claude
                conversationHistory.add(
                    Message(
                        role = "user",
                        content = toolResults
                    )
                )

                // Get Claude's final response after tool execution
                response = claudeApiClient.sendMessage(
                    messages = conversationHistory,
                    tools = tools
                )

                // Check if Claude wants to use more tools
                toolUseBlocks = response.content.filter { it.type == "tool_use" }
            }

            // 7. Add final response to history
            conversationHistory.add(
                Message(
                    role = "assistant",
                    content = response.content
                )
            )

            // 8. Extract text response
            val textResponses = response.content.filter { it.type == "text" }
            val finalResponse = textResponses.joinToString("\n") { it.text ?: "" }

            Log.d(TAG, "Final response: $finalResponse")
            finalResponse

        } catch (e: Exception) {
            Log.e(TAG, "Error processing message", e)
            "Error: ${e.message}"
        }
    }

    /**
     * Clears the conversation history.
     */
    fun clearHistory() {
        conversationHistory.clear()
        Log.d(TAG, "Conversation history cleared")
    }

    /**
     * Converts JsonElement map to Map<String, Any?> for tool execution.
     */
    private fun convertJsonElementToMap(input: Map<String, JsonElement>): Map<String, Any?> {
        return input.mapValues { (_, value) -> convertJsonElement(value) }
    }

    private fun convertJsonElement(element: JsonElement): Any? {
        return when (element) {
            is JsonPrimitive -> {
                when {
                    element.isString -> element.content
                    element.booleanOrNull != null -> element.boolean
                    element.intOrNull != null -> element.int
                    element.longOrNull != null -> element.long
                    element.doubleOrNull != null -> element.double
                    else -> element.content
                }
            }
            is JsonArray -> element.map { convertJsonElement(it) }
            is JsonObject -> element.mapValues { (_, v) -> convertJsonElement(v) }
            JsonNull -> null
        }
    }
}
