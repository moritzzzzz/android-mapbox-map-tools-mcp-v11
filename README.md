# Android Mapbox MCP Wrapper

An Android library that enables AI agents like Claude to control Mapbox maps through natural language. This project wraps the Mapbox Maps SDK v11 with Model Context Protocol (MCP) tool definitions, making maps controllable via LLM chat interfaces.

**Android equivalent of:** [mapbox-map-tools-mcp](https://github.com/moritzzzzz/mapbox-map-tools-mcp) (JavaScript/Web version)

## Demo

The demo app includes a chat interface where you can talk to Claude to control the map in real-time:

- **"Show me Paris"** → Map pans to Paris
- **"Add markers at London, Rome, and Madrid"** → Adds 3 markers
- **"Draw a route from New York to Los Angeles"** → Draws a line
- **"Switch to satellite view"** → Changes map style

## Features

- 🗺️ **7 MCP Tools** for comprehensive map control
- 🤖 **Claude API Integration** with conversational UI
- 📍 **Point Annotations** with customizable colors and popups
- 🛣️ **Route/Line Drawing** with styling options
- 🗺️ **Polygon Overlays** with fill and stroke customization
- 📷 **Camera Control** (pan, zoom, fit bounds)
- 🎨 **Style Switching** (streets, satellite, outdoors, dark)
- 🧹 **Layer Management** with selective clearing

---

## Using the Library in Your Project

[![](https://jitpack.io/v/moritzzzzz/android-mapbox-map-tools-mcp-v11.svg)](https://jitpack.io/#moritzzzzz/android-mapbox-map-tools-mcp-v11)

Add the Mapbox MCP Tools library to your Android project via JitPack:

### Step 1: Add JitPack Repository

In your **project-level** `settings.gradle.kts` (or `build.gradle`):

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }  // Add this line
    }
}
```

### Step 2: Add the Dependency

In your **app-level** `build.gradle.kts`:

```kotlin
dependencies {
    // Mapbox MCP Tools Library
    implementation("com.github.moritzzzzz:android-mapbox-map-tools-mcp-v11:v1.0.0")

    // Required: Mapbox Maps SDK (if not already included)
    implementation("com.mapbox.maps:android:11.17.1")

    // Required: Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
}
```

### Step 3: Use the Library

```kotlin
import com.mapbox.maps.MapView
import com.mapbox.mcp.MapboxMapTools

class YourActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var mapboxMapTools: MapboxMapTools

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize MapView
        mapView = findViewById(R.id.mapView)

        // Wait for map style to load
        mapView.mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
            // Create MapboxMapTools wrapper
            mapboxMapTools = MapboxMapTools(mapView)

            // Get tool definitions for your LLM
            val tools = mapboxMapTools.getToolsForLLM()
            // Send these to Claude API or your LLM provider...

            // Execute a tool
            val result = mapboxMapTools.executeTool(
                "pan_map_to_location",
                mapOf(
                    "latitude" to 48.8566,
                    "longitude" to 2.3522,
                    "zoom" to 12.0
                )
            )
        }
    }
}
```

** Full integration example:** See the [demo app source code](mcp_tools_demo_app/app/src/main/java/com/example/mcp_tools_demo/) for a complete working implementation with Claude API integration.

---

## Quick Start - Run the Demo

### Prerequisites

You'll need two API keys (both free):

1. **Mapbox Access Token** - Configure in `res/values/mapbox_access_token.xml`! 
2. **Claude API Key** - Securely configured via `local.properties`

**First-time setup**: Copy `local.properties.example` to `local.properties` and add your Claude API key. See [API Key Setup](mcp_tools_demo_app/API_KEY_SETUP.md) for details.

### Steps to Run

1. **Clone and Open**
   ```bash
   git clone <your-repo-url>
   cd android_mapbox_mcp_wrapper/mcp_tools_demo_app
   ```

   Open the `mcp_tools_demo_app` folder in Android Studio.

2. **Sync Gradle**
   - Android Studio will prompt you to sync
   - Click "Sync Now" and wait for it to complete

3. **Run the App**
   - Click the green "Run" button (or press Shift+F10)
   - Select a device or emulator
   - Wait for the app to install and launch

4. **Start Chatting!**
   - Type a message in the chat box
   - Try: "Show me New York City"
   - Watch the map respond to your natural language commands

That's it! The app is pre-configured and ready to use.

---

## Configuration

### Using Your Own API Keys

#### Mapbox Access Token

1. **Get a token**: Visit [Mapbox Account](https://account.mapbox.com/) (free tier available)
2. **Update the token**: Open `app/src/main/res/values/mapbox_access_token.xml`
3. Replace the existing token with yours:
   ```xml
   <string name="mapbox_access_token">YOUR_MAPBOX_TOKEN_HERE</string>
   ```

#### Claude API Key

1. **Get a key**: Visit [Anthropic Console](https://console.anthropic.com/) (requires account)
2. **Copy the template**:
   ```bash
   cp local.properties.example local.properties
   ```
3. **Add your key**: Edit `local.properties` and replace the placeholder:
   ```properties
   claude.api.key=sk-ant-api03-YOUR_KEY_HERE
   ```
4. **Rebuild**: Run `./gradlew clean assembleDebug`

**Note**: `local.properties` is gitignored and will never be committed. This keeps your API key secure!

For more details, see [`API_KEY_SETUP.md`](mcp_tools_demo_app/API_KEY_SETUP.md).

### Advanced Configuration

In `Config.kt`, you can configure:

```kotlin
object Config {
    // Claude API Key (loaded from local.properties at build time)
    val CLAUDE_API_KEY = BuildConfig.CLAUDE_API_KEY

    // Model selection (sonnet is faster, opus is more capable)
    const val CLAUDE_MODEL = "claude-sonnet-4-5-20250929"
    // Options: "claude-sonnet-4-5-20250929", "claude-opus-4-5-20251101"

    // Maximum tokens for responses
    const val MAX_TOKENS = 4096
}
```

---

## Using the Library in Your Own App

### Installation Options

**Option 1: JitPack (Recommended)** - See [Using the Library in Your Project](#-using-the-library-in-your-project) above

**Option 2: Manual Copy** - Copy the `mapbox-mcp-tools` module into your project:

1. Copy the `mapbox-mcp-tools` folder into your Android project
2. Add to `settings.gradle.kts`:
   ```kotlin
   include(":mapbox-mcp-tools")
   ```
3. Add dependency in your app's `build.gradle.kts`:
   ```kotlin
   dependencies {
       implementation(project(":mapbox-mcp-tools"))
       implementation("com.mapbox.maps:android:11.17.1")
   }
   ```

### Step 2: Initialize MapboxMapTools

```kotlin
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.mcp.MapboxMapTools
import com.mapbox.mcp.models.ToolResult

class YourActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var mapboxMapTools: MapboxMapTools

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create and setup MapView
        mapView = MapView(this)
        setContentView(mapView)

        // Load map style first
        mapView.mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
            // Initialize tools after style loads
            mapboxMapTools = MapboxMapTools(mapView)

            // Now you can use the tools!
            useMapTools()
        }
    }

    private fun useMapTools() {
        // Get tool definitions (for sending to LLM)
        val tools = mapboxMapTools.getToolsForLLM()
        println("Available tools: ${tools.map { it.name }}")

        // Execute a tool directly
        val result = mapboxMapTools.executeTool(
            "add_points_to_map",
            mapOf(
                "points" to listOf(
                    mapOf(
                        "lat" to 40.7128,
                        "lng" to -74.0060,
                        "title" to "New York City",
                        "description" to "The Big Apple"
                    )
                ),
                "iconColor" to "#FF5722",
                "iconSize" to 1.5
            )
        )

        // Handle result
        when (result) {
            is ToolResult.Success -> {
                Log.d("Map", "Success: ${result.data}")
            }
            is ToolResult.Error -> {
                Log.e("Map", "Error: ${result.message}")
            }
        }
    }
}
```

### Step 3: Tool Execution Examples

#### Add Markers

```kotlin
mapboxMapTools.executeTool("add_points_to_map", mapOf(
    "points" to listOf(
        mapOf("lat" to 48.8566, "lng" to 2.3522, "title" to "Paris"),
        mapOf("lat" to 51.5074, "lng" to -0.1278, "title" to "London")
    ),
    "layerName" to "cities",
    "iconColor" to "#4CAF50"
))
```

#### Draw a Route

```kotlin
mapboxMapTools.executeTool("add_route_to_map", mapOf(
    "coordinates" to listOf(
        listOf(-122.4194, 37.7749),  // San Francisco
        listOf(-118.2437, 34.0522)   // Los Angeles
    ),
    "lineColor" to "#2196F3",
    "lineWidth" to 5.0
))
```

#### Pan to Location

```kotlin
mapboxMapTools.executeTool("pan_map_to_location", mapOf(
    "latitude" to 40.7128,
    "longitude" to -74.0060,
    "zoom" to 12.0,
    "animated" to true
))
```

#### Clear Layers

```kotlin
// Clear all layers
mapboxMapTools.executeTool("clear_map_layers", emptyMap())

// Clear specific layers
mapboxMapTools.executeTool("clear_map_layers", mapOf(
    "layerNames" to listOf("cities", "routes")
))
```

---

## Using Multiple MCP Tool Sets / Extending the functions

You can combine the Mapbox MCP Tools with other MCP servers or tool providers to give Claude access to multiple capabilities at once.

### Architecture Overview

```
Claude API
    ↓
Your App sends combined tool list:
    ├─ Mapbox Tools (this library)
    ├─ Weather API Tools (hypothetical)
    └─ Database Tools (hypothetical)
    ↓
Your App routes execution to correct provider
```

### Step 1: Create Multiple Tool Providers

```kotlin
class WeatherTools {
    fun getToolsForLLM(): List<ToolDefinition> {
        return listOf(
            ToolDefinition(
                name = "get_weather",
                description = "Get current weather for a location",
                inputSchema = InputSchema(
                    type = "object",
                    properties = mapOf(
                        "city" to Property("string", "City name"),
                        "country" to Property("string", "Country code (optional)")
                    ),
                    required = listOf("city")
                )
            )
        )
    }

    fun executeTool(name: String, params: Map<String, Any?>): ToolResult {
        return when (name) {
            "get_weather" -> {
                val city = params["city"] as? String ?: return ToolResult.Error("Missing city")
                // Call your weather API here...
                ToolResult.Success("Weather in $city: Sunny, 72°F")
            }
            else -> ToolResult.Error("Unknown tool: $name")
        }
    }
}
```

### Step 2: Update ChatManager to Combine Tools

Modify `ChatManager.kt` to accept multiple tool providers:

```kotlin
class ChatManager(
    private val claudeApiClient: ClaudeApiClient,
    private val mapboxMapTools: MapboxMapTools,
    private val weatherTools: WeatherTools  // Add additional providers
) {
    suspend fun processMessage(userMessage: String): String {
        // ... existing code ...

        // Combine all tool definitions
        val allTools = buildList {
            addAll(mapboxMapTools.getToolsForLLM())
            addAll(weatherTools.getToolsForLLM())
            // Add more tool providers here...
        }

        Log.d(TAG, "Available tools: ${allTools.map { it.name }}")

        // Send to Claude with all tools
        val response = claudeApiClient.sendMessage(
            messages = conversationHistory.toList(),
            tools = allTools
        )

        // ... rest of existing code ...
    }

    private suspend fun executeToolCall(toolUseBlock: ContentBlock): String {
        val toolName = toolUseBlock.name ?: return "Error: No tool name"
        val params = convertJsonElementMapToAny(toolUseBlock.input ?: emptyMap())

        Log.d(TAG, "Executing tool: $toolName with params: $params")

        // Route to appropriate tool provider based on tool name
        val result = when {
            toolName.startsWith("add_") ||
            toolName.startsWith("pan_") ||
            toolName.startsWith("fit_") ||
            toolName.startsWith("clear_") ||
            toolName.startsWith("set_") -> {
                // Mapbox tools
                mapboxMapTools.executeTool(toolName, params)
            }
            toolName == "get_weather" -> {
                // Weather tools
                weatherTools.executeTool(toolName, params)
            }
            else -> {
                ToolResult.Error("Unknown tool: $toolName")
            }
        }

        return when (result) {
            is ToolResult.Success -> result.data ?: "Success"
            is ToolResult.Error -> "Error: ${result.message}"
        }
    }
}
```

### Step 3: Update MainActivity Initialization

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var mapboxMapTools: MapboxMapTools
    private lateinit var weatherTools: WeatherTools
    private lateinit var chatManager: ChatManager

    private fun initializeChatManager() {
        val claudeApiClient = ClaudeApiClient(CLAUDE_API_KEY)
        weatherTools = WeatherTools()  // Initialize additional provider

        chatManager = ChatManager(
            claudeApiClient,
            mapboxMapTools,
            weatherTools  // Pass additional provider
        )

        Log.d(TAG, "ChatManager initialized with ${
            mapboxMapTools.getToolsForLLM().size + weatherTools.getToolsForLLM().size
        } tools")
    }
}
```

### Step 4: Try Combined Commands

Now Claude can use tools from multiple providers:

```
User: "Show me Paris and tell me the weather there"
  ↓
Claude calls:
  1. pan_map_to_location (Mapbox)
  2. get_weather (Weather API)
  ↓
App: "I've centered the map on Paris. The weather is Sunny, 72°F."
```

### Advanced: Dynamic Tool Routing

For more complex scenarios, use a tool registry:

```kotlin
class ToolRegistry {
    private val providers = mutableMapOf<String, ToolProvider>()

    fun register(toolNamePrefix: String, provider: ToolProvider) {
        providers[toolNamePrefix] = provider
    }

    fun getAllTools(): List<ToolDefinition> {
        return providers.values.flatMap { it.getToolsForLLM() }
    }

    fun executeTool(toolName: String, params: Map<String, Any?>): ToolResult {
        // Find provider by checking tool name prefixes or patterns
        val provider = providers.entries.find { (prefix, _) ->
            toolName.startsWith(prefix)
        }?.value

        return provider?.executeTool(toolName, params)
            ?: ToolResult.Error("No provider found for tool: $toolName")
    }
}

// Usage
val registry = ToolRegistry()
registry.register("map", mapboxMapTools)
registry.register("weather", weatherTools)
registry.register("db", databaseTools)

val allTools = registry.getAllTools()
val result = registry.executeTool("map_add_points", params)
```

### Example MCP Server Integrations

**Common MCP servers you might want to integrate:**

1. **File System MCP** - Read/write files
   ```kotlin
   implementation("com.github.user:filesystem-mcp-android:1.0.0")
   ```

2. **Database MCP** - Query databases
   ```kotlin
   implementation("com.github.user:sqlite-mcp-tools:1.0.0")
   ```

3. **Calendar MCP** - Access device calendar
   ```kotlin
   implementation("com.github.user:calendar-mcp-tools:1.0.0")
   ```

4. **Custom REST API Tools** - Wrap your own APIs
   ```kotlin
   class MyApiTools {
       fun getToolsForLLM() = listOf(/* your tools */)
       fun executeTool(name: String, params: Map<String, Any?>) = /* execute */
   }
   ```

### Benefits of Multiple Tool Sets

**Richer Interactions** - Claude can perform complex multi-domain tasks
**Modularity** - Easy to add/remove tool providers
**Reusability** - Share tool providers across different apps
**Separation of Concerns** - Each provider handles its own domain

### Best Practices

1. **Use clear tool naming** - Prefix tool names with domain (e.g., `map_`, `weather_`, `db_`)
2. **Document tool interactions** - Note which tools work well together
3. **Handle errors gracefully** - Each provider should return consistent error formats
4. **Test tool combinations** - Ensure Claude can chain tools correctly
5. **Monitor token usage** - More tools = larger context, consider grouping related tools

### Downsides of too many tools

The major concern is the context length of the LLM request. The more tools are sent with the request, the more expensive each single request gets.

---

## Available Tools

The library provides 7 MCP tools:

### 1. `add_points_to_map`

Add markers/pins to the map.

**Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `points` | Array | ✅ Yes | - | Array of `{lat, lng, title?, description?}` |
| `layerName` | String | No | "points" | Layer identifier for grouping |
| `iconColor` | String | No | "#FF0000" | Hex color for markers |
| `iconSize` | Float | No | 1.0 | Size multiplier |

**Example:**
```kotlin
mapOf(
    "points" to listOf(
        mapOf("lat" to 40.7128, "lng" to -74.0060, "title" to "NYC")
    ),
    "iconColor" to "#FF5722"
)
```

### 2. `add_route_to_map`

Draw a line/path on the map.

**Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `coordinates` | Array | ✅ Yes | - | Array of `[lng, lat]` pairs |
| `layerName` | String | No | "route" | Layer identifier |
| `lineColor` | String | No | "#3b9ddd" | Hex color |
| `lineWidth` | Float | No | 4.0 | Width in pixels |
| `lineOpacity` | Float | No | 0.8 | Opacity (0-1) |

### 3. `add_polygon_to_map`

Draw a filled area on the map.

**Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `coordinates` | Array | ✅ Yes | - | Array of `[lng, lat]` pairs forming boundary |
| `layerName` | String | No | "polygon" | Layer identifier |
| `fillColor` | String | No | "#3b9ddd" | Hex fill color |
| `fillOpacity` | Float | No | 0.5 | Fill opacity (0-1) |
| `strokeColor` | String | No | "#000000" | Border color |
| `strokeWidth` | Float | No | 2.0 | Border width |

### 4. `pan_map_to_location`

Move the camera to a specific location.

**Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `latitude` | Double | ✅ Yes | - | Latitude coordinate |
| `longitude` | Double | ✅ Yes | - | Longitude coordinate |
| `zoom` | Double | No | current | Zoom level (optional) |
| `animated` | Boolean | No | true | Animate transition |
| `duration` | Long | No | 1000 | Animation duration (ms) |

### 5. `fit_map_to_bounds`

Adjust camera to show all specified points.

**Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `coordinates` | Array | ✅ Yes | - | Array of `[lng, lat]` to fit in view |
| `padding` | Int | No | 50 | Padding in pixels |
| `animated` | Boolean | No | true | Animate transition |

### 6. `clear_map_layers`

Remove annotations/layers from the map.

**Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `layerNames` | Array | No | null | Layer names to clear (null = all) |

### 7. `set_map_style`

Change the map's visual appearance.

**Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `styleUrl` | String | ✅ Yes | - | Mapbox style URL |

**Popular styles:**
- `mapbox://styles/mapbox/streets-v12` - Default street map
- `mapbox://styles/mapbox/outdoors-v12` - Outdoor/hiking
- `mapbox://styles/mapbox/satellite-v9` - Satellite imagery
- `mapbox://styles/mapbox/satellite-streets-v12` - Satellite with labels
- `mapbox://styles/mapbox/dark-v11` - Dark theme
- `mapbox://styles/mapbox/light-v11` - Light theme

---

## Example Prompts for Claude

Once the demo is running, try these natural language commands:

### Basic Navigation
- "Show me Tokyo"
- "Take me to the Eiffel Tower"
- "Pan to coordinates 37.7749, -122.4194"
- "Zoom in on Central Park"

### Adding Markers
- "Add a red marker at the Statue of Liberty"
- "Put markers on Paris, London, and Berlin"
- "Add a marker at coordinates 35.6762, 139.6503 with title 'Tokyo Tower'"

### Drawing Routes
- "Draw a blue line from New York to Boston"
- "Show me a route connecting San Francisco, Las Vegas, and Los Angeles"
- "Draw a path along these coordinates: [[0,0], [1,1], [2,0]]"

### Polygons
- "Draw a green polygon around Manhattan"
- "Create a semi-transparent blue area covering these points: [[0,0], [1,0], [1,1], [0,1]]"

### Map Styles
- "Switch to satellite view"
- "Use dark mode"
- "Change to outdoors style"
- "Show me the streets map"

### Complex Multi-step Commands
- "Clear the map, then show me Europe and add markers for the capitals of France, Germany, and Italy"
- "Show me California, add a marker at San Francisco, and draw a line to Los Angeles"
- "Switch to satellite view, zoom to New York, and add markers at Times Square and Central Park"

### Combining Features
- "Show me North America, fit the view to show Canada, USA, and Mexico, then add markers at their capitals"
- "Clear all layers, pan to Paris with zoom level 12, and add a red marker"

---

## Architecture

### Library Design

The `mapbox-mcp-tools` library provides a simple, server-free API:

```
MapboxMapTools
├── getToolsForLLM() → List<ToolDefinition>
│   Returns MCP tool schemas for LLM
│
└── executeTool(name, params) → ToolResult
    Executes tools and returns Success/Error
```

**Key Points:**
- ✅ No server component - pure wrapper library
- ✅ Thread-safe (runs map operations on main thread)
- ✅ Layer tracking for organized annotations
- ✅ Comprehensive error handling

### Demo App Architecture

```
┌─────────────────────────────────┐
│ User types message in chat      │
└──────────────┬──────────────────┘
               ▼
┌─────────────────────────────────┐
│ ChatManager.processMessage()    │
├─────────────────────────────────┤
│ 1. Get tools from library       │
│ 2. Send to Claude API           │
│ 3. Parse tool_use blocks        │
│ 4. Execute via MapboxMapTools   │
│ 5. Send results back to Claude  │
│ 6. Return final response        │
└──────────────┬──────────────────┘
               ▼
┌─────────────────────────────────┐
│ Display response + Update map   │
└─────────────────────────────────┘
```

**Flow Example:**
```
User: "Show me Paris"
  ↓
Claude: [analyzes] → tool_use: pan_map_to_location(lat: 48.8566, lng: 2.3522)
  ↓
App: Executes tool → Map pans to Paris
  ↓
Claude: "I've centered the map on Paris, France (48.8566°N, 2.3522°E)"
  ↓
User sees response + map at Paris
```

---

##  Troubleshooting

### App won't build

**Error:** "Could not resolve dependency..."
- **Fix:** Make sure you've synced Gradle. Go to File → Sync Project with Gradle Files

**Error:** "Failed to resolve: com.mapbox.maps..."
- **Fix:** Check your internet connection. Gradle needs to download dependencies.

### Map doesn't load

**Symptom:** Gray or blank screen where map should be
- **Fix:** Check your Mapbox access token in `mapbox_access_token.xml`
- **Fix:** Ensure you have internet connection
- **Fix:** Check Logcat for error messages (View → Tool Windows → Logcat)

### Chat doesn't respond

**Error:** "Chat not initialized. Check API key."
- **Fix:** Verify Claude API key in `Config.kt` is valid and not blank

**Error:** "Claude API error: 401"
- **Fix:** Your Claude API key is invalid or expired. Get a new one from https://console.anthropic.com/

**Error:** "Claude API error: 429"
- **Fix:** You've hit rate limits. Wait a moment and try again.

### Map loads but tools don't work

**Symptom:** Chat works but map doesn't respond to commands
- **Fix:** Check that `MapboxMapTools` is initialized AFTER the map style loads
- **Fix:** Look in Logcat for errors starting with "MapboxMapTools"

### Coordinates are wrong

**Issue:** Markers appear in wrong locations
- **Remember:** Mapbox uses `[longitude, latitude]` order, not `[lat, lng]`
- **Fix:** In tool calls, make sure coordinates are `[lng, lat]`

---

## Requirements

- **Min SDK:** 29 (Android 10)
- **Target SDK:** 36
- **Compile SDK:** 36
- **Kotlin:** 2.0.21
- **Mapbox SDK:** 11.17.1
- **Internet Permission:** Required for map tiles and Claude API

---

## Security Best Practices

**Important:** This demo includes API keys for easy testing. For production apps:

### ❌ Don't Do This
```kotlin
// DON'T commit API keys to version control
const val CLAUDE_API_KEY = "sk-ant-api03-xxxx"
```

### ✅ Do This Instead

**Option 1: Use BuildConfig**
```kotlin
// build.gradle.kts
android {
    defaultConfig {
        buildConfigField(
            "String",
            "CLAUDE_API_KEY",
            "\"${System.getenv("CLAUDE_API_KEY")}\""
        )
    }
}

// Config.kt
const val CLAUDE_API_KEY = BuildConfig.CLAUDE_API_KEY
```

**Option 2: Use local.properties**
```properties
# local.properties (add to .gitignore)
CLAUDE_API_KEY=sk-ant-api03-xxxx
```

```kotlin
// build.gradle.kts
val localProperties = Properties()
localProperties.load(FileInputStream(rootProject.file("local.properties")))

android {
    defaultConfig {
        buildConfigField(
            "String",
            "CLAUDE_API_KEY",
            "\"${localProperties.getProperty("CLAUDE_API_KEY")}\""
        )
    }
}
```

**Option 3: Environment Variables**
```bash
export CLAUDE_API_KEY="sk-ant-api03-xxxx"
```

### Additional Security Tips

1. **Add to .gitignore:**
   ```
   Config.kt
   local.properties
   **/api_keys.xml
   ```

2. **Use secret management services:**
   - AWS Secrets Manager
   - Google Secret Manager
   - HashiCorp Vault

3. **Implement rate limiting** in production
4. **Monitor API usage** for unexpected activity
5. **Rotate keys regularly**

---

## Project Structure

```
android_mapbox_mcp_wrapper/
├── README.md                           # This file
└── mcp_tools_demo_app/                 # Android Studio project
    ├── mapbox-mcp-tools/               # Library module
    │   ├── build.gradle.kts
    │   └── src/main/java/com/mapbox/mcp/
    │       ├── MapboxMapTools.kt       # Main API
    │       ├── models/
    │       │   ├── ToolDefinition.kt   # MCP tool schema
    │       │   ├── ToolResult.kt       # Result type
    │       │   ├── Point.kt            # Point data
    │       │   ├── Route.kt            # Route data
    │       │   └── Polygon.kt          # Polygon data
    │       └── internal/
    │           └── (implementation)
    │
    └── app/                            # Demo application
        ├── build.gradle.kts
        └── src/main/java/com/example/mcp_tools_demo/
            ├── MainActivity.kt          # Main UI + integration
            ├── Config.kt               # Configuration (API keys)
            ├── ClaudeApiClient.kt      # Claude API client
            ├── ChatManager.kt          # Conversation orchestrator
            ├── ChatAdapter.kt          # Chat UI adapter
            └── models/
                └── ChatMessage.kt      # UI message model
```

---

## Contributing

Contributions welcome! Areas for improvement:

- [ ] Additional map tools (3D terrain, geocoding, etc.)
- [ ] Support for more LLM providers (OpenAI, etc.)
- [ ] Map state query tools (get current position, etc.)
- [ ] Batch operations for better performance
- [ ] Unit tests and instrumentation tests

---

## License

MIT License

Copyright (c) 2025 Moritz Forster

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

---

## Credits

- **Inspired by:** [mapbox-map-tools-mcp](https://github.com/moritzzzzz/mapbox-map-tools-mcp) (JavaScript version)
- **Built with:** [Mapbox Maps SDK for Android](https://docs.mapbox.com/android/maps/)
- **Powered by:** [Claude API](https://www.anthropic.com/claude) by Anthropic

---

## Support

- **Library Issues:** Open an issue on GitHub
- **Mapbox SDK Help:** [Mapbox Documentation](https://docs.mapbox.com/android/maps/)
- **Claude API Help:** [Anthropic Documentation](https://docs.anthropic.com/)

---

## Learn More

- [Model Context Protocol (MCP)](https://modelcontextprotocol.io/)
- [Mapbox Maps SDK Guide](https://docs.mapbox.com/android/maps/guides/)
- [Claude API Reference](https://docs.anthropic.com/claude/reference/)
- [Kotlin for Android](https://developer.android.com/kotlin)
