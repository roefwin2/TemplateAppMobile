# 📍 Configuration Module Géolocalisation CMP

## 📦 Architecture

```
geolocation/
├── LocationService.kt              # Interface expect + LocationData
├── domain/
│   ├── LocationRepository.kt       # Interface du repository
│   ├── GetCurrentLocationUseCase.kt
│   ├── ObserveLocationUseCase.kt
│   ├── StartLocationTrackingUseCase.kt
│   ├── StopLocationTrackingUseCase.kt
│   ├── CheckLocationPermissionUseCase.kt
│   └── RequestLocationPermissionUseCase.kt
├── data/
│   └── LocationRepositoryImpl.kt   # Implémentation du repository
├── presentation/
│   ├── MapViewModel.kt             # ViewModel principal
│   ├── MapUiState.kt               # État UI + Events
│   ├── MapView.kt                  # Composable expect (carte)
│   ├── MapScreen.kt                # Écran complet avec contrôles
│   └── MapNavigator.kt             # Navigation externe
└── di/
    └── GeolocationModule.kt        # Module Koin
```

## 🎯 MapViewModel

Le `MapViewModel` est le composant principal pour gérer la carte. Il fournit :

### État (MapUiState)
```kotlin
data class MapUiState(
    val currentLocation: LocationData?,  // Position actuelle
    val isLoading: Boolean,              // Chargement en cours
    val isTracking: Boolean,             // Tracking actif
    val hasPermission: Boolean,          // Permission accordée
    val permissionDenied: Boolean,       // Permission refusée
    val error: String?,                  // Message d'erreur
    val markers: List<MapMarker>,        // Marqueurs sur la carte
    val polylinePoints: List<MapPosition> // Points du tracé
)
```

### Events (MapUiEvent)
```kotlin
sealed interface MapUiEvent {
    data object RequestPermission : MapUiEvent
    data object StartTracking : MapUiEvent
    data object StopTracking : MapUiEvent
    data object RefreshLocation : MapUiEvent
    data class OnMapClick(val position: MapPosition) : MapUiEvent
    data class OnMarkerClick(val marker: MapMarker) : MapUiEvent
    data class AddMarker(val marker: MapMarker) : MapUiEvent
    data class RemoveMarker(val marker: MapMarker) : MapUiEvent
    data object ClearMarkers : MapUiEvent
    data object ClearError : MapUiEvent
}
```

### Utilisation simple
```kotlin
@Composable
fun MyScreen() {
    // Utiliser l'écran complet prêt à l'emploi
    MapScreen()
}
```

### Utilisation personnalisée
```kotlin
@Composable
fun CustomMapScreen(viewModel: MapViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column {
        // Votre UI personnalisée
        Text("Position: ${uiState.currentLocation?.latitude}, ${uiState.currentLocation?.longitude}")
        
        Button(onClick = { viewModel.onEvent(MapUiEvent.RefreshLocation) }) {
            Text("Rafraîchir")
        }

        Button(onClick = { viewModel.onEvent(MapUiEvent.StartTracking) }) {
            Text(if (uiState.isTracking) "Arrêter" else "Démarrer le tracking")
        }

        // La carte
        MapView(
            cameraPosition = uiState.cameraPosition,
            markers = uiState.markers,
            polylinePoints = uiState.polylinePoints,
            onMapClick = { viewModel.onEvent(MapUiEvent.OnMapClick(it)) },
            onMarkerClick = { viewModel.onEvent(MapUiEvent.OnMarkerClick(it)) }
        )
    }
}
```

### Fonctionnalités SOS
```kotlin
// Démarrer un tracking SOS avec marqueur
viewModel.startSOSTracking("alert-123")

// Ajouter un marqueur SOS manuellement
viewModel.addSOSMarker(48.8566, 2.3522, "Urgence!")
```

## 📦 Fichiers générés

### commonMain
- `org.society.appname.geolocation.presentation.MapViewModel` : ViewModel principal
- `org.society.appname.geolocation.presentation.MapUiState` : État + Events
- `org.society.appname.geolocation.presentation.MapScreen` : Écran complet
- `org.society.appname.geolocation.presentation.MapView` : Composable expect
- `org.society.appname.geolocation.domain.*UseCase` : Use cases (Clean Architecture)
- `org.society.appname.geolocation.data.LocationRepositoryImpl` : Implémentation du repository
- `org.society.appname.geolocation.di.GeolocationModule` : Module Koin avec ViewModel

### androidMain
- `org.society.appname.geolocation.LocationService.android.kt` : Impl avec FusedLocationProvider
- `org.society.appname.geolocation.presentation.MapView.android.kt` : Google Maps


### iosMain
- `org.society.appname.geolocation.LocationService.ios.kt` : Impl avec CoreLocation
- `org.society.appname.geolocation.presentation.MapView.ios.kt` : MapKit


## 🔧 Fichiers modifiés


### initKoin (Module Koin)
Le fichier contenant `initKoin` a été mis à jour automatiquement :
```kotlin
import org.society.appname.geolocation.di.geolocationModule

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(authModule, geolocationModule) // ← geolocationModule ajouté
    }
```


### AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="AIzaSyDEh8JhHWuNCGytKmkskUN_3foFBZGDzTo" />
```


## 🏗️ Injection Koin

Le module Koin fournit automatiquement :

```kotlin
// Services
single<LocationService> { ... }

// Repository
factory<LocationRepository> { LocationRepositoryImpl(get()) }

// Use Cases
factoryOf(::GetCurrentLocationUseCase)
factoryOf(::ObserveLocationUseCase)
factoryOf(::StartLocationTrackingUseCase)
factoryOf(::StopLocationTrackingUseCase)
factoryOf(::CheckLocationPermissionUseCase)
factoryOf(::RequestLocationPermissionUseCase)

// ViewModel
viewModelOf(::MapViewModel)

// Presentation
single { MapNavigator(...) }
```

## 🔧 Configuration requise

### Android

#### Initialisation dans Application.kt
```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        initializeLocationService(
            sosRepository = get(), // via Koin
            activity = null
        )
    }
}
```

### iOS

#### Info.plist
```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>L'application a besoin de votre position pour...</string>
<key>NSLocationAlwaysAndWhenInUseUsageDescription</key>
<string>L'application a besoin de votre position en arrière-plan pour...</string>
<key>UIBackgroundModes</key>
<array>
    <string>location</string>
</array>
```

## 📚 Documentation

- [Google Maps Android SDK](https://developers.google.com/maps/documentation/android-sdk/overview)
- [Apple MapKit](https://developer.apple.com/documentation/mapkit)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Koin](https://insert-koin.io/)