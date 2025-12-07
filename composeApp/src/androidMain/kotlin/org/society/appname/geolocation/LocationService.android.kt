package org.society.appname.geolocation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Factory Android
 */
actual fun createLocationService(): LocationService {
    return AndroidLocationService.instance
}

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

/**
 * Implémentation Android du service de localisation
 */
class AndroidLocationService private constructor() : LocationService {

    private var context: Context? = null
    private var activity: Activity? = null

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var currentAlertId: String? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var locationCallback: LocationCallback? = null

    companion object {
        val instance = AndroidLocationService()

        private const val LOCATION_UPDATE_INTERVAL = 10_000L // 10 secondes
        private const val LOCATION_FASTEST_INTERVAL = 5_000L // 5 secondes
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    /**
     * Initialise le service avec le contexte Android
     */
    fun initialize(context: Context, activity: Activity? = null) {
        this.context = context.applicationContext
        this.activity = activity
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    override suspend fun startLocationTracking(alertId: String) {
        val client = fusedLocationClient
            ?: throw IllegalStateException("LocationService not initialized. Call initialize() first.")

        if (!hasLocationPermission()) {
            throw SecurityException("Location permission not granted")
        }

        currentAlertId = alertId

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_UPDATE_INTERVAL
        ).apply {
            setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL)
            setWaitForAccurateLocation(false)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    // Les mises à jour sont disponibles via observeLocation()
                    println("📍 Location update: ${location.latitude}, ${location.longitude}")
                }
            }
        }

        client.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    override fun stopLocationTracking() {
        val client = fusedLocationClient ?: return

        locationCallback?.let {
            client.removeLocationUpdates(it)
        }
        locationCallback = null
        currentAlertId = null
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): LocationData? {
        val client = fusedLocationClient ?: return null

        if (!hasLocationPermission()) {
            return null
        }

        return suspendCancellableCoroutine { continuation ->
            client.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        continuation.resume(
                            LocationData(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                accuracy = location.accuracy,
                                timestamp = location.time
                            )
                        ) {}
                    } else {
                        requestSingleLocation(client, continuation)
                    }
                }
                .addOnFailureListener { _ ->
                    continuation.resume(null) {}
                }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestSingleLocation(
        client: FusedLocationProviderClient,
        continuation: CancellableContinuation<LocationData?>
    ) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000L
        ).build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    continuation.resume(
                        LocationData(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            accuracy = location.accuracy,
                            timestamp = location.time
                        )
                    ) {}
                } ?: continuation.resume(null) {}

                client.removeLocationUpdates(this)
            }
        }

        client.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        )

        continuation.invokeOnCancellation {
            client.removeLocationUpdates(callback)
        }
    }

    @SuppressLint("MissingPermission")
    override fun observeLocation(): Flow<LocationData> = callbackFlow {
        val client = fusedLocationClient
        if (client == null) {
            close(IllegalStateException("FusedLocationClient not initialized"))
            return@callbackFlow
        }

        if (!hasLocationPermission()) {
            close(SecurityException("Location permission not granted"))
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_UPDATE_INTERVAL
        ).build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    trySend(
                        LocationData(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            accuracy = location.accuracy,
                            timestamp = location.time
                        )
                    )
                }
            }
        }

        client.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        )

        awaitClose {
            client.removeLocationUpdates(callback)
        }
    }

    override suspend fun hasLocationPermission(): Boolean {
        val ctx = context ?: return false

        val fineLocation = ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    override suspend fun requestLocationPermission() {
        val act = activity ?: return

        ActivityCompat.requestPermissions(
            act,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERMISSION_REQUEST_CODE
        )
    }
}

/**
 * Extension pour initialiser le service depuis l'Application
 */
fun Context.initializeLocationService(activity: Activity? = null) {
    AndroidLocationService.instance.initialize(this, activity)
}