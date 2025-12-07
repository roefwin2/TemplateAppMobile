package org.society.appname.geolocation.domain

/**
 * Use case pour démarrer le suivi de la localisation
 */
class StartLocationTrackingUseCase(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(alertId: String) {
        locationRepository.startLocationTracking(alertId)
    }
}