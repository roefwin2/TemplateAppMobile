package org.society.appname.geolocation.presentation

import android.os.Bundle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView as GoogleMapView
import com.google.android.gms.maps.model.*

@Composable
actual fun MapView(
    modifier: Modifier,
    cameraPosition: CameraPosition,
    markers: List<MapMarker>,
    polylinePoints: List<MapPosition>,
    onMapClick: (MapPosition) -> Unit,
    onMarkerClick: (MapMarker) -> Unit
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    var map by remember { mutableStateOf<GoogleMap?>(null) }
    val mapView = remember { GoogleMapView(context) }

    // Gestion du lifecycle de la MapView
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.apply {
                getMapAsync { googleMap ->
                    map = googleMap

                    googleMap.uiSettings.apply {
                        isZoomControlsEnabled = true
                        isZoomGesturesEnabled = true
                        isScrollGesturesEnabled = true
                        isRotateGesturesEnabled = true
                        isTiltGesturesEnabled = true
                        isMyLocationButtonEnabled = false
                        isCompassEnabled = true
                    }

                    val latLng = LatLng(
                        cameraPosition.target.latitude,
                        cameraPosition.target.longitude
                    )
                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(latLng, cameraPosition.zoom)
                    )

                    googleMap.setOnMapClickListener { latLng ->
                        onMapClick(MapPosition(latLng.latitude, latLng.longitude))
                    }
                }
            }
        },
        update = { view ->
            map?.let { googleMap ->
                val latLng = LatLng(
                    cameraPosition.target.latitude,
                    cameraPosition.target.longitude
                )
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(latLng, cameraPosition.zoom)
                )

                googleMap.clear()

                val markerMap = mutableMapOf<com.google.android.gms.maps.model.Marker, MapMarker>()
                markers.forEach { marker ->
                    val markerLatLng = LatLng(
                        marker.position.latitude,
                        marker.position.longitude
                    )

                    val markerOptions = MarkerOptions()
                        .position(markerLatLng)
                        .title(marker.title)
                        .snippet(marker.snippet)

                    when (marker.icon) {
                        MarkerIcon.SOS -> {
                            markerOptions.icon(
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                            )
                        }
                        MarkerIcon.USER -> {
                            markerOptions.icon(
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                            )
                        }
                        MarkerIcon.LOCATION_UPDATE -> {
                            markerOptions.icon(
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                            )
                        }
                        MarkerIcon.DEFAULT -> {}
                    }

                    googleMap.addMarker(markerOptions)?.let { googleMarker ->
                        markerMap[googleMarker] = marker
                    }
                }

                googleMap.setOnMarkerClickListener { clickedMarker ->
                    markerMap[clickedMarker]?.let { marker ->
                        onMarkerClick(marker)
                    }
                    true
                }

                if (polylinePoints.isNotEmpty()) {
                    val polylineOptions = PolylineOptions()
                        .addAll(polylinePoints.map { point ->
                            LatLng(point.latitude, point.longitude)
                        })
                        .color(android.graphics.Color.parseColor("#DC2626"))
                        .width(10f)
                        .geodesic(true)

                    googleMap.addPolyline(polylineOptions)
                }

                if (markers.isNotEmpty() && polylinePoints.isEmpty()) {
                    val boundsBuilder = LatLngBounds.Builder()
                    markers.forEach { marker ->
                        boundsBuilder.include(
                            LatLng(marker.position.latitude, marker.position.longitude)
                        )
                    }
                    try {
                        val bounds = boundsBuilder.build()
                        googleMap.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(bounds, 100)
                        )
                    } catch (e: Exception) { }
                }
            }
        }
    )
}