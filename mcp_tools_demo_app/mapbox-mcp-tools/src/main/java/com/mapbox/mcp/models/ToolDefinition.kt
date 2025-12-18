package com.mapbox.mcp.models

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an MCP tool definition that can be sent to an LLM.
 *
 * This follows the MCP (Model Context Protocol) tool schema format,
 * which is compatible with Claude API and other LLM tool calling systems.
 */
@Serializable
data class ToolDefinition(
    /**
     * Unique identifier for the tool (e.g., "add_points_to_map")
     */
    val name: String,

    /**
     * Human-readable description of what the tool does
     */
    val description: String,

    /**
     * JSON Schema defining the input parameters for the tool
     */
    @SerialName("input_schema")
    val inputSchema: InputSchema
)

/**
 * JSON Schema definition for tool parameters
 */
@Serializable
data class InputSchema(
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault
    val type: String = "object",
    val properties: Map<String, Property>,
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault
    val required: List<String> = emptyList()
)

/**
 * Individual property definition in the JSON Schema
 */
@Serializable
data class Property(
    val type: String,
    val description: String? = null,
    val default: String? = null,
    val items: Items? = null
)

/**
 * Schema for array items
 */
@Serializable
data class Items(
    val type: String? = null,
    val properties: Map<String, Property>? = null
)
