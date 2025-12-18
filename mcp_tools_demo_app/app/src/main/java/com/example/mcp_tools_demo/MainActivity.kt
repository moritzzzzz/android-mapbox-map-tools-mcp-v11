package com.example.mcp_tools_demo

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mcp_tools_demo.models.ChatMessage
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.mcp.MapboxMapTools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var mapboxMapTools: MapboxMapTools
    private lateinit var chatManager: ChatManager
    private lateinit var chatAdapter: ChatAdapter

    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var chatRecyclerView: RecyclerView

    // Claude API key - You can change this in the Config object below
    private val CLAUDE_API_KEY = Config.CLAUDE_API_KEY

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize map
        initializeMap()

        // Initialize chat UI
        initializeChatUI()

        // Show welcome message
        showWelcomeMessage()
    }

    private fun initializeMap() {
        // Create MapView and add to container
        mapView = MapView(this)

        // Set initial camera position (center of USA)
        mapView.mapboxMap.setCamera(
            CameraOptions.Builder()
                .center(Point.fromLngLat(-98.0, 39.5))
                .pitch(0.0)
                .zoom(2.0)
                .bearing(0.0)
                .build()
        )

        // Load map style
        mapView.mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
            Log.d(TAG, "Map style loaded")

            // Initialize MapboxMapTools after style is loaded
            mapboxMapTools = MapboxMapTools(mapView)

            // Initialize ChatManager
            initializeChatManager()

            Log.d(TAG, "MapboxMapTools initialized with ${mapboxMapTools.getToolsForLLM().size} tools")
        }

        // Add MapView to container
        val mapContainer = findViewById<FrameLayout>(R.id.mapContainer)
        mapContainer.addView(mapView)
    }

    private fun initializeChatUI() {
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        chatRecyclerView = findViewById(R.id.chatRecyclerView)

        // Setup RecyclerView
        chatAdapter = ChatAdapter()
        chatRecyclerView.adapter = chatAdapter
        chatRecyclerView.layoutManager = LinearLayoutManager(this)

        // Setup send button
        sendButton.setOnClickListener {
            sendMessage()
        }

        // Send on Enter key
        messageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }
    }

    private fun initializeChatManager() {
        if (CLAUDE_API_KEY.isBlank()) {
            Toast.makeText(
                this,
                "Please set your Claude API key in Config.kt",
                Toast.LENGTH_LONG
            ).show()
            Log.e(TAG, "Claude API key not set!")
            return
        }

        val claudeApiClient = ClaudeApiClient(CLAUDE_API_KEY)
        chatManager = ChatManager(claudeApiClient, mapboxMapTools)

        Log.d(TAG, "ChatManager initialized with Claude API")
    }

    private fun showWelcomeMessage() {
        val welcomeMessage = """
            Welcome to Mapbox MCP Demo!

            Try asking Claude to:
            • "Show me New York City"
            • "Add a marker at the Eiffel Tower"
            • "Draw a route from San Francisco to Los Angeles"
            • "Show me Europe and add markers for Paris, London, and Rome"
        """.trimIndent()

        chatAdapter.addMessage(ChatMessage(welcomeMessage, isUser = false))
        chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)
    }

    private fun sendMessage() {
        val messageText = messageInput.text.toString().trim()

        if (messageText.isEmpty()) {
            return
        }

        if (!::chatManager.isInitialized) {
            Toast.makeText(this, "Chat not initialized. Check API key.", Toast.LENGTH_SHORT).show()
            return
        }

        // Clear input
        messageInput.text.clear()

        // Add user message to chat
        chatAdapter.addMessage(ChatMessage(messageText, isUser = true))
        chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)

        // Disable send button while processing
        sendButton.isEnabled = false
        sendButton.text = "Sending..."

        // Process message with ChatManager
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    chatManager.processMessage(messageText)
                }

                // Add Claude's response to chat
                chatAdapter.addMessage(ChatMessage(response, isUser = false))
                chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)

            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
                chatAdapter.addMessage(
                    ChatMessage(
                        "Error: ${e.message ?: "Unknown error occurred"}",
                        isUser = false
                    )
                )
                chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)
            } finally {
                // Re-enable send button
                sendButton.isEnabled = true
                sendButton.text = "Send"
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
