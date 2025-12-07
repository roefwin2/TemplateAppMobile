package org.society.appname.geolocation

import kotlinx.coroutines.flow.Flow

/**
 * Service de localisation multiplatform
 */
interface LocationService {
    /**
     * Démarre le suivi de la localisation
     */
    suspend fun startLocationTracking(alertId: String)

    /**
     * Arrête le suivi de la localisation
     */
    fun stopLocationTracking()

    /**
     * Obtient la position actuelle
     */
    suspend fun getCurrentLocation(): LocationData?

    /**
     * Flow de mises à jour de position
     */
    fun observeLocation(): Flow<LocationData>

    /**
     * Vérifie si les permissions sont accordées
     */
    suspend fun hasLocationPermission(): Boolean

    /**
     * Demande les permissions de localisation
     */
    suspend fun requestLocationPermission()
}

/**
 * Données de localisation
 */
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float?,
    val timestamp: Long = currentTimeMillis()
)

/**
 * Factory pour créer le service selon la plateforme
 */
expect fun createLocationService(): LocationService

/**
 * Fonction expect pour obtenir le timestamp actuel
 */
expect fun currentTimeMillis(): Long