package com.mapbox.mcp.models

/**
 * Represents a polygon annotation on the map.
 *
 * @param coordinates Array of coordinate pairs [longitude, latitude] defining the polygon boundary
 * @param fillColor Hex color string for the polygon fill (e.g., "#3b9ddd")
 * @param fillOpacity Fill opacity (0.0 to 1.0)
 * @param strokeColor Hex color string for the polygon border (e.g., "#000000")
 * @param strokeWidth Border width in pixels
 */
data class Polygon(
    val coordinates: List<List<Double>>, // [[lng, lat], [lng, lat], ...]
    val fillColor: String = "#3b9ddd",
    val fillOpacity: Float = 0.5f,
    val strokeColor: String = "#000000",
    val strokeWidth: Float = 2.0f
)
