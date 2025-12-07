package org.society.appname.geolocation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.society.appname.geolocation.LocationData
import org.society.appname.geolocation.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel pour la gestion de la carte et de la localisation
 */
class MapViewModel(
    private val getCurrentLocation: GetCurrentLocationUseCase,
    private val observeLocation: ObserveLocationUseCase,
    private val startLocationTracking: StartLocationTrackingUseCase,
    private val stopLocationTracking: StopLocationTrackingUseCase,
    private val checkLocationPermission: CheckLocationPermissionUseCase,
    private val requestLocationPermission: RequestLocationPermissionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    // Historique des positions pour la polyline
    private val _locationHistory = mutableListOf<MapPosition>()

    init {
        checkPermissions()
        observeLocationUpdates()
    }

    /**
     * Gère les événements UI
     */
    fun onEvent(event: MapUiEvent) {
        when (event) {
            is MapUiEvent.RequestPermission -> requestPermissions()
            is MapUiEvent.StartTracking -> startTracking()
            is MapUiEvent.StopTracking -> stopTracking()
            is MapUiEvent.RefreshLocation -> refreshCurrentLocation()
            is MapUiEvent.OnMapClick -> onMapClick(event.position)
            is MapUiEvent.OnMarkerClick -> onMarkerClick(event.marker)
            is MapUiEvent.AddMarker -> addMarker(event.marker)
            is MapUiEvent.RemoveMarker -> removeMarker(event.marker)
            is MapUiEvent.ClearMarkers -> clearMarkers()
            is MapUiEvent.ClearError -> clearError()
        }
    }

    /**
     * Vérifie les permissions au démarrage
     */
    private fun checkPermissions() {
        viewModelScope.launch {
            val hasPermission = checkLocationPermission()
            _uiState.update { it.copy(hasPermission = hasPermission) }
            
            if (hasPermission) {
                refreshCurrentLocation()
            }
        }
    }

    /**
     * Demande les permissions de localisation
     */
    private fun requestPermissions() {
        viewModelScope.launch {
            try {
                requestLocationPermission()
                val hasPermission = checkLocationPermission()
                _uiState.update { 
                    it.copy(
                        hasPermission = hasPermission,
                        permissionDenied = !hasPermission
                    ) 
                }
                
                if (hasPermission) {
                    refreshCurrentLocation()
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Erreur lors de la demande de permission: ${e.message}",
                        permissionDenied = true
                    ) 
                }
            }
        }
    }

    /**
     * Observe les mises à jour de position en temps réel
     */
    private fun observeLocationUpdates() {
        viewModelScope.launch {
            observeLocation()
                .catch { e ->
                    _uiState.update { 
                        it.copy(error = "Erreur de localisation: ${e.message}") 
                    }
                }
                .collect { location ->
                    updateLocation(location)
                }
        }
    }

    /**
     * Met à jour la position actuelle
     */
    private fun updateLocation(location: LocationData) {
        val position = MapPosition(location.latitude, location.longitude)
        
        // Ajouter à l'historique si tracking actif
        if (_uiState.value.isTracking) {
            _locationHistory.add(position)
        }

        // Créer le marqueur de position actuelle
        val currentMarker = MapMarker(
            position = position,
            title = "Ma position",
            snippet = "Précision: ${location.accuracy?.toInt() ?: "N/A"}m",
            icon = MarkerIcon.USER
        )

        _uiState.update { state ->
            val updatedMarkers = state.markers
                .filter { it.icon != MarkerIcon.USER } + currentMarker
            
            state.copy(
                currentLocation = location,
                isLoading = false,
                markers = updatedMarkers,
                polylinePoints = if (state.isTracking) _locationHistory.toList() else state.polylinePoints
            )
        }
    }

    /**
     * Rafraîchit la position actuelle
     */
    private fun refreshCurrentLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val location = getCurrentLocation()
                if (location != null) {
                    updateLocation(location)
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Impossible d'obtenir la position actuelle"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Erreur: ${e.message}"
                    ) 
                }
            }
        }
    }

    /**
     * Démarre le tracking de la position
     */
    private fun startTracking(alertId: String = "default") {
        viewModelScope.launch {
            if (!_uiState.value.hasPermission) {
                requestPermissions()
                return@launch
            }

            try {
                _locationHistory.clear()
                startLocationTracking(alertId)
                _uiState.update { 
                    it.copy(
                        isTracking = true,
                        polylinePoints = emptyList()
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Erreur démarrage tracking: ${e.message}") 
                }
            }
        }
    }

    /**
     * Arrête le tracking
     */
    private fun stopTracking() {
        stopLocationTracking()
        _uiState.update { it.copy(isTracking = false) }
    }

    /**
     * Gère le clic sur la carte
     */
    private fun onMapClick(position: MapPosition) {
        // À personnaliser selon vos besoins
        println("Map clicked at: ${position.latitude}, ${position.longitude}")
    }

    /**
     * Gère le clic sur un marqueur
     */
    private fun onMarkerClick(marker: MapMarker) {
        // À personnaliser selon vos besoins
        println("Marker clicked: ${marker.title}")
    }

    /**
     * Ajoute un marqueur
     */
    private fun addMarker(marker: MapMarker) {
        _uiState.update { state ->
            state.copy(markers = state.markers + marker)
        }
    }

    /**
     * Supprime un marqueur
     */
    private fun removeMarker(marker: MapMarker) {
        _uiState.update { state ->
            state.copy(markers = state.markers.filter { it != marker })
        }
    }

    /**
     * Supprime tous les marqueurs (sauf la position actuelle)
     */
    private fun clearMarkers() {
        _uiState.update { state ->
            state.copy(markers = state.markers.filter { it.icon == MarkerIcon.USER })
        }
    }

    /**
     * Efface l'erreur
     */
    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Ajoute un marqueur SOS
     */
    fun addSOSMarker(latitude: Double, longitude: Double, title: String = "SOS") {
        addMarker(
            MapMarker(
                position = MapPosition(latitude, longitude),
                title = title,
                icon = MarkerIcon.SOS
            )
        )
    }

    /**
     * Démarre le tracking pour une alerte SOS
     */
    fun startSOSTracking(alertId: String) {
        viewModelScope.launch {
            startTracking(alertId)
            
            // Ajouter un marqueur SOS à la position actuelle
            _uiState.value.currentLocation?.let { location ->
                addSOSMarker(location.latitude, location.longitude, "Alerte SOS")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (_uiState.value.isTracking) {
            stopLocationTracking()
        }
    }
}