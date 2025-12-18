package com.mapbox.mcp.models

/**
 * Represents a point/marker annotation on the map.
 *
 * @param latitude Latitude coordinate
 * @param longitude Longitude coordinate
 * @param title Optional title for the marker popup
 * @param description Optional description for the marker popup
 */
data class Point(
    val latitude: Double,
    val longitude: Double,
    val title: String? = null,
    val description: String? = null
)
