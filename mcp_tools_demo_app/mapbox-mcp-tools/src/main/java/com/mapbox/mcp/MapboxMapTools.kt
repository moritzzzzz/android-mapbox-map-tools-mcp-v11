package com.mapbox.mcp

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CoordinateBounds
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.style.expressions.dsl.generated.rgb
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.mcp.models.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

/**
 * Main wrapper class that exposes Mapbox Maps SDK functionality as MCP tools.
 *
 * Usage:
 * ```kotlin
 * val mapboxMapTools = MapboxMapTools(mapView)
 * val tools = mapboxMapTools.getToolsForLLM() // Get tool definitions
 * val result = mapboxMapTools.executeTool("add_points_to_map", params) // Execute a tool
 * ```
 */
class MapboxMapTools(private val mapView: MapView) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val annotationApi = mapView.annotations

    // Layer tracking: maps layer name to annotation managers
    private val pointLayerMap = mutableMapOf<String, PointAnnotationManager>()
    private val lineLayerMap = mutableMapOf<String, PolylineAnnotationManager>()
    private val polygonLayerMap = mutableMapOf<String, PolygonAnnotationManager>()

    /**
     * Returns list of MCP tool definitions that can be sent to an LLM.
     *
     * These definitions follow the MCP/Claude API format with name, description,
     * and JSON Schema for input parameters.
     */
    fun getToolsForLLM(): List<ToolDefinition> {
        return listOf(
            createAddPointsToolDefinition(),
            createAddRouteToolDefinition(),
            createAddPolygonToolDefinition(),
            createPanMapToolDefinition(),
            createFitBoundsToolDefinition(),
            createClearLayersToolDefinition(),
            createSetStyleToolDefinition()
        )
    }

    /**
     * Executes a tool by name with the given parameters.
     *
     * @param name Tool name (e.g., "add_points_to_map")
     * @param params Parameter map matching the tool's input schema
     * @return ToolResult.Success or ToolResult.Error
     */
    fun executeTool(name: String, params: Map<String, Any?>): ToolResult {
        return try {
            when (name) {
                "add_points_to_map" -> executeAddPoints(params)
                "add_route_to_map" -> executeAddRoute(params)
                "add_polygon_to_map" -> executeAddPolygon(params)
                "pan_map_to_location" -> executePanMap(params)
                "fit_map_to_bounds" -> executeFitBounds(params)
                "clear_map_layers" -> executeClearLayers(params)
                "set_map_style" -> executeSetStyle(params)
                else -> ToolResult.Error("Unknown tool: $name", "UNKNOWN_TOOL")
            }
        } catch (e: Exception) {
            ToolResult.Error("Error executing tool: ${e.message}", "EXECUTION_ERROR")
        }
    }

    // ========== Tool Definitions ==========

    private fun createAddPointsToolDefinition() = ToolDefinition(
        name = "add_points_to_map",
        description = "Add markers/points to the map with optional titles and descriptions",
        inputSchema = InputSchema(
            type = "object",
            properties = mapOf(
                "points" to Property(
                    type = "array",
                    description = "Array of points to add",
                    items = Items(
                        type = "object",
                        properties = mapOf(
                            "lat" to Property("number", "Latitude coordinate"),
                            "lng" to Property("number", "Longitude coordinate"),
                            "title" to Property("string", "Optional marker title"),
                            "description" to Property("string", "Optional marker description")
                        )
                    )
                ),
                "layerName" to Property("string", "Layer name for grouping", default = "points"),
                "iconColor" to Property("string", "Hex color for markers", default = "#FF0000"),
                "iconSize" to Property("number", "Marker size multiplier", default = "1.0")
            ),
            required = listOf("points")
        )
    )

    private fun createAddRouteToolDefinition() = ToolDefinition(
        name = "add_route_to_map",
        description = "Draw a line/route on the map",
        inputSchema = InputSchema(
            type = "object",
            properties = mapOf(
                "coordinates" to Property(
                    type = "array",
                    description = "Array of [lng, lat] coordinate pairs",
                    items = Items(type = "array")
                ),
                "layerName" to Property("string", "Layer name for grouping", default = "route"),
                "lineColor" to Property("string", "Hex color for line", default = "#3b9ddd"),
                "lineWidth" to Property("number", "Line width in pixels", default = "4.0"),
                "lineOpacity" to Property("number", "Line opacity 0-1", default = "0.8")
            ),
            required = listOf("coordinates")
        )
    )

    private fun createAddPolygonToolDefinition() = ToolDefinition(
        name = "add_polygon_to_map",
        description = "Draw a filled polygon area on the map",
        inputSchema = InputSchema(
            type = "object",
            properties = mapOf(
                "coordinates" to Property(
                    type = "array",
                    description = "Array of [lng, lat] coordinate pairs forming polygon boundary",
                    items = Items(type = "array")
                ),
                "layerName" to Property("string", "Layer name for grouping", default = "polygon"),
                "fillColor" to Property("string", "Hex color for fill", default = "#3b9ddd"),
                "fillOpacity" to Property("number", "Fill opacity 0-1", default = "0.5"),
                "strokeColor" to Property("string", "Hex color for border", default = "#000000"),
                "strokeWidth" to Property("number", "Border width in pixels", default = "2.0")
            ),
            required = listOf("coordinates")
        )
    )

    private fun createPanMapToolDefinition() = ToolDefinition(
        name = "pan_map_to_location",
        description = "Move the map camera to a specific location",
        inputSchema = InputSchema(
            type = "object",
            properties = mapOf(
                "latitude" to Property("number", "Latitude coordinate"),
                "longitude" to Property("number", "Longitude coordinate"),
                "zoom" to Property("number", "Zoom level (optional, keeps current if not provided)"),
                "animated" to Property("boolean", "Animate the transition", default = "true"),
                "duration" to Property("number", "Animation duration in ms", default = "1000")
            ),
            required = listOf("latitude", "longitude")
        )
    )

    private fun createFitBoundsToolDefinition() = ToolDefinition(
        name = "fit_map_to_bounds",
        description = "Adjust camera to show all specified coordinates",
        inputSchema = InputSchema(
            type = "object",
            properties = mapOf(
                "coordinates" to Property(
                    type = "array",
                    description = "Array of [lng, lat] coordinate pairs to fit in view",
                    items = Items(type = "array")
                ),
                "padding" to Property("number", "Padding in pixels", default = "50"),
                "animated" to Property("boolean", "Animate the transition", default = "true")
            ),
            required = listOf("coordinates")
        )
    )

    private fun createClearLayersToolDefinition() = ToolDefinition(
        name = "clear_map_layers",
        description = "Remove annotations/layers from the map",
        inputSchema = InputSchema(
            type = "object",
            properties = mapOf(
                "layerNames" to Property(
                    type = "array",
                    description = "Array of layer names to clear. If null/empty, clears all layers.",
                    items = Items(type = "string")
                )
            ),
            required = emptyList()
        )
    )

    private fun createSetStyleToolDefinition() = ToolDefinition(
        name = "set_map_style",
        description = "Change the map's visual style",
        inputSchema = InputSchema(
            type = "object",
            properties = mapOf(
                "styleUrl" to Property(
                    "string",
                    "Mapbox style URL (e.g., 'mapbox://styles/mapbox/streets-v12')"
                )
            ),
            required = listOf("styleUrl")
        )
    )

    // ========== Tool Implementations ==========

    private fun executeAddPoints(params: Map<String, Any?>): ToolResult {
        val points = params["points"] as? List<Map<String, Any?>>
            ?: return ToolResult.Error("Missing or invalid 'points' parameter", "INVALID_PARAMS")

        val layerName = params["layerName"] as? String ?: "points"
        val iconColor = params["iconColor"] as? String ?: "#FF0000"
        val iconSize = (params["iconSize"] as? Number)?.toFloat() ?: 1.0f

        mainHandler.post {
            try {
                val manager = pointLayerMap.getOrPut(layerName) {
                    annotationApi.createPointAnnotationManager()
                }

                points.forEach { pointData ->
                    val lat = (pointData["lat"] as? Number)?.toDouble()
                        ?: throw IllegalArgumentException("Missing latitude")
                    val lng = (pointData["lng"] as? Number)?.toDouble()
                        ?: throw IllegalArgumentException("Missing longitude")

                    val pointAnnotationOptions = PointAnnotationOptions()
                        .withPoint(Point.fromLngLat(lng, lat))
                        .withIconSize(iconSize.toDouble())
                        .withIconColor(parseColor(iconColor))

                    manager.create(pointAnnotationOptions)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return ToolResult.Success("Added ${points.size} point(s) to layer '$layerName'")
    }

    private fun executeAddRoute(params: Map<String, Any?>): ToolResult {
        val coordinates = params["coordinates"] as? List<List<Number>>
            ?: return ToolResult.Error("Missing or invalid 'coordinates' parameter", "INVALID_PARAMS")

        val layerName = params["layerName"] as? String ?: "route"
        val lineColor = params["lineColor"] as? String ?: "#3b9ddd"
        val lineWidth = (params["lineWidth"] as? Number)?.toFloat() ?: 4.0f
        val lineOpacity = (params["lineOpacity"] as? Number)?.toFloat() ?: 0.8f

        mainHandler.post {
            try {
                val manager = lineLayerMap.getOrPut(layerName) {
                    annotationApi.createPolylineAnnotationManager()
                }

                val points = coordinates.map { coord ->
                    Point.fromLngLat(coord[0].toDouble(), coord[1].toDouble())
                }

                val polylineAnnotationOptions = PolylineAnnotationOptions()
                    .withPoints(points)
                    .withLineColor(parseColor(lineColor))
                    .withLineWidth(lineWidth.toDouble())
                    .withLineOpacity(lineOpacity.toDouble())

                manager.create(polylineAnnotationOptions)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return ToolResult.Success("Added route with ${coordinates.size} points to layer '$layerName'")
    }

    private fun executeAddPolygon(params: Map<String, Any?>): ToolResult {
        val coordinates = params["coordinates"] as? List<List<Number>>
            ?: return ToolResult.Error("Missing or invalid 'coordinates' parameter", "INVALID_PARAMS")

        val layerName = params["layerName"] as? String ?: "polygon"
        val fillColor = params["fillColor"] as? String ?: "#3b9ddd"
        val fillOpacity = (params["fillOpacity"] as? Number)?.toFloat() ?: 0.5f
        val strokeColor = params["strokeColor"] as? String ?: "#000000"
        val strokeWidth = (params["strokeWidth"] as? Number)?.toFloat() ?: 2.0f

        mainHandler.post {
            try {
                val manager = polygonLayerMap.getOrPut(layerName) {
                    annotationApi.createPolygonAnnotationManager()
                }

                val points = listOf(coordinates.map { coord ->
                    Point.fromLngLat(coord[0].toDouble(), coord[1].toDouble())
                })

                val polygonAnnotationOptions = PolygonAnnotationOptions()
                    .withPoints(points)
                    .withFillColor(parseColor(fillColor))
                    .withFillOpacity(fillOpacity.toDouble())
                    .withFillOutlineColor(parseColor(strokeColor))

                manager.create(polygonAnnotationOptions)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return ToolResult.Success("Added polygon with ${coordinates.size} points to layer '$layerName'")
    }

    private fun executePanMap(params: Map<String, Any?>): ToolResult {
        val latitude = (params["latitude"] as? Number)?.toDouble()
            ?: return ToolResult.Error("Missing 'latitude' parameter", "INVALID_PARAMS")
        val longitude = (params["longitude"] as? Number)?.toDouble()
            ?: return ToolResult.Error("Missing 'longitude' parameter", "INVALID_PARAMS")

        val zoom = (params["zoom"] as? Number)?.toDouble()
        val animated = params["animated"] as? Boolean ?: true
        val duration = (params["duration"] as? Number)?.toLong() ?: 1000

        mainHandler.post {
            try {
                val cameraOptions = CameraOptions.Builder()
                    .center(Point.fromLngLat(longitude, latitude))
                    .apply { zoom?.let { zoom(it) } }
                    .build()

                if (animated) {
                    mapView.getMapboxMap().flyTo(
                        cameraOptions,
                        MapAnimationOptions.Builder().duration(duration).build()
                    )
                } else {
                    mapView.getMapboxMap().setCamera(cameraOptions)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return ToolResult.Success("Panned map to ($latitude, $longitude)")
    }

    private fun executeFitBounds(params: Map<String, Any?>): ToolResult {
        val coordinates = params["coordinates"] as? List<List<Number>>
            ?: return ToolResult.Error("Missing or invalid 'coordinates' parameter", "INVALID_PARAMS")

        val padding = (params["padding"] as? Number)?.toDouble() ?: 50.0
        val animated = params["animated"] as? Boolean ?: true

        mainHandler.post {
            try {
                val points = coordinates.map { coord ->
                    Point.fromLngLat(coord[0].toDouble(), coord[1].toDouble())
                }

                // Calculate bounds
                var minLng = Double.MAX_VALUE
                var maxLng = -Double.MAX_VALUE
                var minLat = Double.MAX_VALUE
                var maxLat = -Double.MAX_VALUE

                points.forEach { point ->
                    minLng = minOf(minLng, point.longitude())
                    maxLng = maxOf(maxLng, point.longitude())
                    minLat = minOf(minLat, point.latitude())
                    maxLat = maxOf(maxLat, point.latitude())
                }

                val southwest = Point.fromLngLat(minLng, minLat)
                val northeast = Point.fromLngLat(maxLng, maxLat)

                val cameraOptions = mapView.getMapboxMap().cameraForCoordinateBounds(
                    CoordinateBounds(southwest, northeast),
                    EdgeInsets(padding, padding, padding, padding)
                )

                if (animated) {
                    mapView.getMapboxMap().flyTo(
                        cameraOptions,
                        MapAnimationOptions.Builder().duration(1000).build()
                    )
                } else {
                    mapView.getMapboxMap().setCamera(cameraOptions)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return ToolResult.Success("Fitted map to ${coordinates.size} coordinates")
    }

    private fun executeClearLayers(params: Map<String, Any?>): ToolResult {
        val layerNames = params["layerNames"] as? List<String>

        mainHandler.post {
            try {
                if (layerNames == null || layerNames.isEmpty()) {
                    // Clear all layers
                    pointLayerMap.values.forEach { it.deleteAll() }
                    lineLayerMap.values.forEach { it.deleteAll() }
                    polygonLayerMap.values.forEach { it.deleteAll() }
                    pointLayerMap.clear()
                    lineLayerMap.clear()
                    polygonLayerMap.clear()
                } else {
                    // Clear specific layers
                    layerNames.forEach { layerName ->
                        pointLayerMap[layerName]?.deleteAll()
                        pointLayerMap.remove(layerName)

                        lineLayerMap[layerName]?.deleteAll()
                        lineLayerMap.remove(layerName)

                        polygonLayerMap[layerName]?.deleteAll()
                        polygonLayerMap.remove(layerName)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val message = if (layerNames == null || layerNames.isEmpty()) {
            "Cleared all layers"
        } else {
            "Cleared layers: ${layerNames.joinToString(", ")}"
        }

        return ToolResult.Success(message)
    }

    private fun executeSetStyle(params: Map<String, Any?>): ToolResult {
        val styleUrl = params["styleUrl"] as? String
            ?: return ToolResult.Error("Missing 'styleUrl' parameter", "INVALID_PARAMS")

        mainHandler.post {
            try {
                mapView.getMapboxMap().loadStyleUri(styleUrl)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return ToolResult.Success("Set map style to: $styleUrl")
    }

    // ========== Helper Methods ==========

    private fun parseColor(hexColor: String): Int {
        return try {
            Color.parseColor(hexColor)
        } catch (e: Exception) {
            Color.RED // Default fallback
        }
    }
}
