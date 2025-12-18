package com.example.mcp_tools_demo

/**
 * Configuration for the demo app.
 *
 * To use your own Claude API key:
 * 1. Get a key from https://console.anthropic.com/
 * 2. Add it to local.properties: claude.api.key=YOUR_KEY_HERE
 * 3. Rebuild the project
 */
object Config {
    /**
     * Claude API key for accessing the Anthropic API.
     *
     * This is loaded from local.properties at build time.
     * See local.properties.example for setup instructions.
     */
    val CLAUDE_API_KEY = BuildConfig.CLAUDE_API_KEY

    /**
     * Claude model to use for chat.
     * Options: "claude-sonnet-4-5-20250929", "claude-opus-4-5-20251101", etc.
     */
    const val CLAUDE_MODEL = "claude-sonnet-4-5-20250929"

    /**
     * Maximum tokens for Claude responses.
     */
    const val MAX_TOKENS = 4096
}
