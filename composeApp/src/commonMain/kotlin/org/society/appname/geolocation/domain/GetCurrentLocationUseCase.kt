package org.society.appname.geolocation.domain

import org.society.appname.geolocation.LocationData

/**
 * Use case pour récupérer la position actuelle
 */
class GetCurrentLocationUseCase(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(): LocationData? {
        return locationRepository.getCurrentLocation()
    }
}