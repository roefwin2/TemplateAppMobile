package org.society.appname.geolocation.data

import org.society.appname.geolocation.LocationData
import org.society.appname.geolocation.LocationService
import org.society.appname.geolocation.domain.LocationRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implémentation du repository de localisation
 */
class LocationRepositoryImpl(
    private val locationService: LocationService
) : LocationRepository {

    override suspend fun getCurrentLocation(): LocationData? {
        return locationService.getCurrentLocation()
    }

    override fun observeLocation(): Flow<LocationData> {
        return locationService.observeLocation()
    }

    override suspend fun startLocationTracking(alertId: String) {
        locationService.startLocationTracking(alertId)
    }

    override fun stopLocationTracking() {
        locationService.stopLocationTracking()
    }

    override suspend fun hasLocationPermission(): Boolean {
        return locationService.hasLocationPermission()
    }

    override suspend fun requestLocationPermission() {
        locationService.requestLocationPermission()
    }
}