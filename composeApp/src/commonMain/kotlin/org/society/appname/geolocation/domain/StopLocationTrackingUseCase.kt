package org.society.appname.geolocation.domain

/**
 * Use case pour arrêter le suivi de la localisation
 */
class StopLocationTrackingUseCase(
    private val locationRepository: LocationRepository
) {
    operator fun invoke() {
        locationRepository.stopLocationTracking()
    }
}