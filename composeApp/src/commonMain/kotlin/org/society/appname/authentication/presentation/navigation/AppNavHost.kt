package org.society.appname.authentication.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import org.koin.compose.viewmodel.koinViewModel
import org.society.appname.authentication.presentation.MainViewModel
import org.society.appname.authentication.presentation.SessionState

@Composable
fun AppNavHost(
    mainViewModel: MainViewModel = koinViewModel()
) {
    val sessionState by mainViewModel.sessionState.collectAsState()
    val navController = rememberNavController()

    when (sessionState) {
        is SessionState.Loading -> {
            // Écran de chargement
        }

        is SessionState.Unauthenticated -> {
            AuthNavHost(
                navController = navController,
                onAuthComplete = { /* La session sera automatiquement mise à jour */ }
            )
        }

        is SessionState.Authenticated -> {
            MainNavHost(
                navController = navController,
                onLogout = { /* La déconnexion est gérée par le ViewModel */ }
            )
        }
    }
}