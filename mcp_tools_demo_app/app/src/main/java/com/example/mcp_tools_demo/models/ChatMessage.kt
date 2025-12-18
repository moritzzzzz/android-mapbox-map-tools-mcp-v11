package com.example.mcp_tools_demo.models

/**
 * Represents a chat message in the UI.
 *
 * @param text The message text
 * @param isUser True if message is from user, false if from Claude
 * @param timestamp Message timestamp
 */
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
