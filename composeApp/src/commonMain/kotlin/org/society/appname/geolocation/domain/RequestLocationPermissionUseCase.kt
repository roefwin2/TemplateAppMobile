package org.society.appname.geolocation.domain

/**
 * Use case pour demander les permissions de localisation
 */
class RequestLocationPermissionUseCase(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke() {
        locationRepository.requestLocationPermission()
    }
}