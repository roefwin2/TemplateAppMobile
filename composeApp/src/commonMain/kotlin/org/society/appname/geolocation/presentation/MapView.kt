package org.society.appname.geolocation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Position sur la carte
 */
data class MapPosition(
    val latitude: Double,
    val longitude: Double
)

/**
 * Marqueur sur la carte
 */
data class MapMarker(
    val position: MapPosition,
    val title: String,
    val snippet: String? = null,
    val icon: MarkerIcon = MarkerIcon.DEFAULT
)

/**
 * Type d'icône pour les marqueurs
 */
enum class MarkerIcon {
    DEFAULT,
    SOS,
    USER,
    LOCATION_UPDATE
}

/**
 * Configuration de la caméra
 */
data class CameraPosition(
    val target: MapPosition,
    val zoom: Float = 15f
)

/**
 * MapView multiplateforme
 */
@Composable
expect fun MapView(
    modifier: Modifier = Modifier,
    cameraPosition: CameraPosition,
    markers: List<MapMarker> = emptyList(),
    polylinePoints: List<MapPosition> = emptyList(),
    onMapClick: (MapPosition) -> Unit = {},
    onMarkerClick: (MapMarker) -> Unit = {}
)