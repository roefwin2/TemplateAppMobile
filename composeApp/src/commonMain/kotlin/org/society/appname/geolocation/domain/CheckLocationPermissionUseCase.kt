package org.society.appname.geolocation.domain

/**
 * Use case pour vérifier les permissions de localisation
 */
class CheckLocationPermissionUseCase(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(): Boolean {
        return locationRepository.hasLocationPermission()
    }
}