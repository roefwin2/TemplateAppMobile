package org.society.appname.geolocation.domain

import org.society.appname.geolocation.LocationData
import kotlinx.coroutines.flow.Flow

/**
 * Repository pour la gestion de la localisation
 */
interface LocationRepository {
    /**
     * Obtient la position actuelle
     */
    suspend fun getCurrentLocation(): LocationData?

    /**
     * Observe les mises à jour de position
     */
    fun observeLocation(): Flow<LocationData>

    /**
     * Démarre le suivi de la localisation
     */
    suspend fun startLocationTracking(alertId: String)

    /**
     * Arrête le suivi de la localisation
     */
    fun stopLocationTracking()

    /**
     * Vérifie si les permissions sont accordées
     */
    suspend fun hasLocationPermission(): Boolean

    /**
     * Demande les permissions de localisation
     */
    suspend fun requestLocationPermission()
}