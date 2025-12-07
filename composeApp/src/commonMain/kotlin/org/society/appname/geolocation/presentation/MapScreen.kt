package org.society.appname.geolocation.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

/**
 * Écran de carte complet avec contrôles
 */
@Composable
fun MapScreen(
    viewModel: MapViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        // Carte
        if (uiState.hasPermission) {
            MapView(
                modifier = Modifier.fillMaxSize(),
                cameraPosition = uiState.cameraPosition,
                markers = uiState.markers,
                polylinePoints = uiState.polylinePoints,
                onMapClick = { position ->
                    viewModel.onEvent(MapUiEvent.OnMapClick(position))
                },
                onMarkerClick = { marker ->
                    viewModel.onEvent(MapUiEvent.OnMarkerClick(marker))
                }
            )
        } else {
            // Permission non accordée
            PermissionRequest(
                onRequestPermission = {
                    viewModel.onEvent(MapUiEvent.RequestPermission)
                },
                permissionDenied = uiState.permissionDenied
            )
        }

        // Contrôles flottants
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Bouton de rafraîchissement
            FloatingActionButton(
                onClick = { viewModel.onEvent(MapUiEvent.RefreshLocation) },
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.MyLocation, contentDescription = "Ma position")
                }
            }

            // Bouton de tracking
            FloatingActionButton(
                onClick = {
                    if (uiState.isTracking) {
                        viewModel.onEvent(MapUiEvent.StopTracking)
                    } else {
                        viewModel.onEvent(MapUiEvent.StartTracking)
                    }
                },
                containerColor = if (uiState.isTracking) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = if (uiState.isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (uiState.isTracking) "Arrêter" else "Démarrer"
                )
            }
        }

        // Indicateur de tracking
        if (uiState.isTracking) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        "Tracking actif",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        // Affichage des erreurs
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.onEvent(MapUiEvent.ClearError) }) {
                        Text("OK")
                    }
                }
            ) {
                Text(error)
            }
        }

        // Informations de position
        uiState.currentLocation?.let { location ->
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                shape = MaterialTheme.shapes.small
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        "Lat: ${location.latitude}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "Lng: ${location.longitude}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    location.accuracy?.let { acc ->
                        Text(
                            "Précision: ${acc.toInt()}m",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionRequest(
    onRequestPermission: () -> Unit,
    permissionDenied: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.LocationOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (permissionDenied) 
                "Permission de localisation refusée" 
            else 
                "Permission de localisation requise",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (permissionDenied)
                "Veuillez activer la localisation dans les paramètres de l'application"
            else
                "L'application a besoin d'accéder à votre position pour afficher la carte",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRequestPermission) {
            Icon(Icons.Default.LocationOn, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Autoriser la localisation")
        }
    }
}