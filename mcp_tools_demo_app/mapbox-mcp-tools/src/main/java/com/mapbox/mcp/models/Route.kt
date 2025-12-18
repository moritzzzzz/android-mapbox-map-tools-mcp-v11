package com.mapbox.mcp.models

/**
 * Represents a line/route annotation on the map.
 *
 * @param coordinates Array of coordinate pairs [longitude, latitude]
 * @param lineColor Hex color string (e.g., "#3b9ddd")
 * @param lineWidth Line width in pixels
 * @param lineOpacity Line opacity (0.0 to 1.0)
 */
data class Route(
    val coordinates: List<List<Double>>, // [[lng, lat], [lng, lat], ...]
    val lineColor: String = "#3b9ddd",
    val lineWidth: Float = 4.0f,
    val lineOpacity: Float = 0.8f
)
