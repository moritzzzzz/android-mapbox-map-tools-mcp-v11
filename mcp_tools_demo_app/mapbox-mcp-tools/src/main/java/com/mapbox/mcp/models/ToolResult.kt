package com.mapbox.mcp.models

import kotlinx.serialization.Serializable

/**
 * Result type for tool execution.
 *
 * Use pattern matching to handle success vs error cases:
 * ```kotlin
 * when (result) {
 *     is ToolResult.Success -> // handle success
 *     is ToolResult.Error -> // handle error
 * }
 * ```
 */
@Serializable
sealed class ToolResult {
    /**
     * Tool executed successfully.
     *
     * @param data Optional data returned by the tool (can be used for debugging or confirmation messages)
     */
    @Serializable
    data class Success(val data: String? = null) : ToolResult()

    /**
     * Tool execution failed.
     *
     * @param message Descriptive error message explaining what went wrong
     * @param code Optional error code for programmatic handling
     */
    @Serializable
    data class Error(val message: String, val code: String? = null) : ToolResult()
}
