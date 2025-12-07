@file:OptIn(ExperimentalForeignApi::class)

package org.society.appname.geolocation.presentation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.*
import kotlin.math.pow
import platform.CoreLocation.*
import platform.MapKit.*
import platform.UIKit.*
import platform.Foundation.*
import platform.darwin.NSObject

@Composable
actual fun MapView(
    modifier: Modifier,
    cameraPosition: CameraPosition,
    markers: List<MapMarker>,
    polylinePoints: List<MapPosition>,
    onMapClick: (MapPosition) -> Unit,
    onMarkerClick: (MapMarker) -> Unit
) {
    val mapView = remember { MKMapView() }
    val mapDelegate = remember { MapViewDelegate(onMapClick, onMarkerClick) }

    DisposableEffect(Unit) {
        mapView.delegate = mapDelegate
        onDispose { mapView.delegate = null }
    }

    UIKitView(
        modifier = modifier,
        factory = {
            mapView.apply {
                showsUserLocation = false
                showsCompass = true
                showsScale = true
                zoomEnabled = true
                scrollEnabled = true
                rotateEnabled = true
                pitchEnabled = true

                val coordinate = CLLocationCoordinate2DMake(
                    cameraPosition.target.latitude,
                    cameraPosition.target.longitude
                )
                val region = MKCoordinateRegionMakeWithDistance(
                    coordinate,
                    getDistanceFromZoom(cameraPosition.zoom),
                    getDistanceFromZoom(cameraPosition.zoom)
                )
                setRegion(region, animated = false)

                val tapGesture = UITapGestureRecognizer(
                    target = mapDelegate,
                    action = NSSelectorFromString("handleMapTap:")
                )
                addGestureRecognizer(tapGesture)
            }
        },
        update = { mkMapView ->
            val coordinate = CLLocationCoordinate2DMake(
                cameraPosition.target.latitude,
                cameraPosition.target.longitude
            )
            val region = MKCoordinateRegionMakeWithDistance(
                coordinate,
                getDistanceFromZoom(cameraPosition.zoom),
                getDistanceFromZoom(cameraPosition.zoom)
            )
            mkMapView.setRegion(region, animated = true)

            mkMapView.annotations?.let { mkMapView.removeAnnotations(it as List<*>) }
            mkMapView.overlays?.let { mkMapView.removeOverlays(it as List<*>) }

            mapDelegate.markers = markers

            markers.forEach { marker ->
                val annotation = MapAnnotation(
                    coordinate = CLLocationCoordinate2DMake(
                        marker.position.latitude,
                        marker.position.longitude
                    ),
                    title = marker.title,
                    subtitle = marker.snippet,
                    icon = marker.icon
                )
                mkMapView.addAnnotation(annotation)
            }

            if (polylinePoints.isNotEmpty()) {
                val coordinatesArray = polylinePoints.map { point ->
                    CLLocationCoordinate2DMake(point.latitude, point.longitude)
                }.toTypedArray()

                memScoped {
                    val coordinatesPtr = allocArray<CLLocationCoordinate2D>(coordinatesArray.size)
                    coordinatesArray.forEachIndexed { index, coordinate ->
                        coordinate.useContents {
                            coordinatesPtr[index].latitude = this.latitude
                            coordinatesPtr[index].longitude = this.longitude
                        }
                    }

                    val polyline = MKPolyline.polylineWithCoordinates(
                        coords = coordinatesPtr,
                        count = coordinatesArray.size.toULong()
                    )
                    mkMapView.addOverlay(polyline)
                }
            }
        }
    )
}

private class MapAnnotation(
    coordinate: CValue<CLLocationCoordinate2D>,
    title: String?,
    subtitle: String?,
    val icon: MarkerIcon
) : NSObject(), MKAnnotationProtocol {
    @OverrideInit
    constructor() : this(CLLocationCoordinate2DMake(0.0, 0.0), null, null, MarkerIcon.DEFAULT)

    override fun coordinate(): CValue<CLLocationCoordinate2D> = _coordinate
    private val _coordinate = coordinate

    override fun title(): String? = _title
    private val _title = title

    override fun subtitle(): String? = _subtitle
    private val _subtitle = subtitle
}

private class MapViewDelegate(
    private val onMapClick: (MapPosition) -> Unit,
    private val onMarkerClick: (MapMarker) -> Unit
) : NSObject(), MKMapViewDelegateProtocol {

    var markers: List<MapMarker> = emptyList()

    @ObjCAction
    fun handleMapTap(recognizer: UITapGestureRecognizer) {
        val mapView = recognizer.view as? MKMapView ?: return
        val point = recognizer.locationInView(mapView)
        val coordinate = mapView.convertPoint(point, toCoordinateFromView = mapView)
        onMapClick(MapPosition(coordinate.useContents { latitude }, coordinate.useContents { longitude }))
    }

    override fun mapView(mapView: MKMapView, viewForAnnotation: MKAnnotationProtocol): MKAnnotationView? {
        val annotation = viewForAnnotation as? MapAnnotation ?: return null

        val identifier = "MarkerAnnotation"
        var annotationView = mapView.dequeueReusableAnnotationViewWithIdentifier(identifier) as? MKMarkerAnnotationView

        if (annotationView == null) {
            annotationView = MKMarkerAnnotationView(annotation = annotation, reuseIdentifier = identifier)
            annotationView.canShowCallout = true
        } else {
            annotationView.annotation = annotation
        }

        when (annotation.icon) {
            MarkerIcon.SOS -> {
                annotationView.markerTintColor = UIColor.redColor
                annotationView.glyphText = "🆘"
            }
            MarkerIcon.USER -> {
                annotationView.markerTintColor = UIColor.blueColor
                annotationView.glyphText = "👤"
            }
            MarkerIcon.LOCATION_UPDATE -> {
                annotationView.markerTintColor = UIColor.orangeColor
                annotationView.glyphText = "📍"
            }
            MarkerIcon.DEFAULT -> annotationView.markerTintColor = UIColor.redColor
        }

        return annotationView
    }

    override fun mapView(mapView: MKMapView, didSelectAnnotationView: MKAnnotationView) {
        val annotation = didSelectAnnotationView.annotation as? MapAnnotation ?: return
        val coordinate = annotation.coordinate().useContents { MapPosition(latitude, longitude) }

        markers.find {
            it.position.latitude == coordinate.latitude && it.position.longitude == coordinate.longitude
        }?.let { onMarkerClick(it) }
    }

    override fun mapView(mapView: MKMapView, rendererForOverlay: MKOverlayProtocol): MKOverlayRenderer {
        if (rendererForOverlay is MKPolyline) {
            val renderer = MKPolylineRenderer(polyline = rendererForOverlay)
            renderer.strokeColor = UIColor(red = 0.863, green = 0.149, blue = 0.149, alpha = 1.0)
            renderer.lineWidth = 5.0
            return renderer
        }
        return MKOverlayRenderer(overlay = rendererForOverlay)
    }
}

private fun getDistanceFromZoom(zoom: Float): Double {
    return 40075000.0 * kotlin.math.cos(0.0) / 2.0.pow(zoom.toDouble() + 1)
}