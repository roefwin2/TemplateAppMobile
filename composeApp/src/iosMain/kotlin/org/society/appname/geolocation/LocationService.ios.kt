@file:OptIn(ExperimentalForeignApi::class)

package org.society.appname.geolocation

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreLocation.*
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Factory iOS
 */
actual fun createLocationService(): LocationService {
    return IOSLocationService.instance
}

actual fun currentTimeMillis(): Long {
    return (NSDate().timeIntervalSinceReferenceDate * 1000).toLong()
}

/**
 * Implémentation iOS du service de localisation
 */
class IOSLocationService private constructor() : LocationService {

    private val locationManager = CLLocationManager()
    private var currentAlertId: String? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var locationDelegate: LocationDelegate? = null

    companion object {
        val instance = IOSLocationService()
    }

    /**
     * Initialise le service
     */
    fun initialize() {
        // Configuration initiale si nécessaire
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun startLocationTracking(alertId: String) {
        currentAlertId = alertId

        locationManager.apply {
            desiredAccuracy = kCLLocationAccuracyBest
            distanceFilter = 10.0
            allowsBackgroundLocationUpdates = true
            pausesLocationUpdatesAutomatically = false
        }

        locationDelegate = LocationDelegate { location ->
            // Les mises à jour sont disponibles via observeLocation()
            println("📍 Location update: ${location.coordinate.useContents { latitude }}, ${location.coordinate.useContents { longitude }}")
        }

        locationManager.delegate = locationDelegate
        locationManager.startUpdatingLocation()
    }

    override fun stopLocationTracking() {
        locationManager.stopUpdatingLocation()
        locationManager.delegate = null
        locationDelegate = null
        currentAlertId = null
    }

    override suspend fun getCurrentLocation(): LocationData? = suspendCoroutine { continuation ->
        val currentLocation = locationManager.location

        if (currentLocation != null) {
            continuation.resume(
                LocationData(
                    latitude = currentLocation.coordinate.useContents { latitude },
                    longitude = currentLocation.coordinate.useContents { longitude },
                    accuracy = currentLocation.horizontalAccuracy.toFloat(),
                    timestamp = (currentLocation.timestamp.timeIntervalSinceReferenceDate * 1000).toLong()
                )
            )
        } else {
            val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                    val location = didUpdateLocations.lastOrNull() as? CLLocation
                    if (location != null) {
                        continuation.resume(
                            LocationData(
                                latitude = location.coordinate.useContents { latitude },
                                longitude = location.coordinate.useContents { longitude },
                                accuracy = location.horizontalAccuracy.toFloat(),
                                timestamp = (location.timestamp.timeIntervalSinceReferenceDate * 1000).toLong()
                            )
                        )
                        manager.stopUpdatingLocation()
                        manager.delegate = null
                    }
                }

                override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                    continuation.resume(null)
                    manager.delegate = null
                }
            }

            locationManager.delegate = delegate
            locationManager.startUpdatingLocation()

            serviceScope.launch {
                delay(10_000)
                if (continuation.context.isActive) {
                    locationManager.stopUpdatingLocation()
                    locationManager.delegate = null
                    continuation.resume(null)
                }
            }
        }
    }

    override fun observeLocation(): Flow<LocationData> = callbackFlow {
        val delegate = LocationDelegate { location ->
            trySend(
                LocationData(
                    latitude = location.coordinate.useContents { latitude },
                    longitude = location.coordinate.useContents { longitude },
                    accuracy = location.horizontalAccuracy.toFloat(),
                    timestamp = (location.timestamp.timeIntervalSinceReferenceDate * 1000).toLong()
                )
            )
        }

        locationManager.delegate = delegate
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.startUpdatingLocation()

        awaitClose {
            locationManager.stopUpdatingLocation()
            locationManager.delegate = null
        }
    }

    override suspend fun hasLocationPermission(): Boolean {
        return when (CLLocationManager.authorizationStatus()) {
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse -> true
            else -> false
        }
    }

    override suspend fun requestLocationPermission() = suspendCoroutine { continuation ->
        val currentStatus = CLLocationManager.authorizationStatus()

        if (currentStatus == kCLAuthorizationStatusAuthorizedAlways ||
            currentStatus == kCLAuthorizationStatusAuthorizedWhenInUse) {
            continuation.resume(Unit)
            return@suspendCoroutine
        }

        val permissionDelegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                when (manager.authorizationStatus) {
                    kCLAuthorizationStatusAuthorizedAlways,
                    kCLAuthorizationStatusAuthorizedWhenInUse,
                    kCLAuthorizationStatusDenied,
                    kCLAuthorizationStatusRestricted -> {
                        manager.delegate = null
                        continuation.resume(Unit)
                    }
                    else -> {}
                }
            }
        }

        locationManager.delegate = permissionDelegate
        locationManager.requestAlwaysAuthorization()

        serviceScope.launch {
            delay(30_000)
            if (continuation.context.isActive) {
                locationManager.delegate = null
                continuation.resume(Unit)
            }
        }
    }

    private class LocationDelegate(
        private val onLocationUpdate: (CLLocation) -> Unit
    ) : NSObject(), CLLocationManagerDelegateProtocol {

        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            (didUpdateLocations.lastOrNull() as? CLLocation)?.let { location ->
                onLocationUpdate(location)
            }
        }

        override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
            println("❌ Location error: ${didFailWithError.localizedDescription}")
        }

        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            when (manager.authorizationStatus) {
                kCLAuthorizationStatusAuthorizedAlways,
                kCLAuthorizationStatusAuthorizedWhenInUse -> manager.startUpdatingLocation()
                else -> {}
            }
        }
    }
}

/**
 * Initialise le service de localisation
 */
fun initializeLocationService() {
    IOSLocationService.instance.initialize()
}