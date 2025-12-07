package org.society.appname.geolocation.presentation

import org.society.appname.geolocation.LocationData

/**
 * État UI pour la carte
 */
data class MapUiState(
    val currentLocation: LocationData? = null,
    val isLoading: Boolean = false,
    val isTracking: Boolean = false,
    val hasPermission: Boolean = false,
    val permissionDenied: Boolean = false,
    val error: String? = null,
    val markers: List<MapMarker> = emptyList(),
    val polylinePoints: List<MapPosition> = emptyList()
) {
    val cameraPosition: CameraPosition
        get() = CameraPosition(
            target = MapPosition(
                latitude = currentLocation?.latitude ?: 0.0,
                longitude = currentLocation?.longitude ?: 0.0
            ),
            zoom = if (currentLocation != null) 15f else 5f
        )
}

/**
 * Events UI pour la carte
 */
sealed interface MapUiEvent {
    data object RequestPermission : MapUiEvent
    data object StartTracking : MapUiEvent
    data object StopTracking : MapUiEvent
    data object RefreshLocation : MapUiEvent
    data class OnMapClick(val position: MapPosition) : MapUiEvent
    data class OnMarkerClick(val marker: MapMarker) : MapUiEvent
    data class AddMarker(val marker: MapMarker) : MapUiEvent
    data class RemoveMarker(val marker: MapMarker) : MapUiEvent
    data object ClearMarkers : MapUiEvent
    data object ClearError : MapUiEvent
}