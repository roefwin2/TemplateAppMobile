package org.society.appname.geolocation.domain

import org.society.appname.geolocation.LocationData
import kotlinx.coroutines.flow.Flow

/**
 * Use case pour observer les changements de position
 */
class ObserveLocationUseCase(
    private val locationRepository: LocationRepository
) {
    operator fun invoke(): Flow<LocationData> {
        return locationRepository.observeLocation()
    }
}