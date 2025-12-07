package org.society.appname.module.authentication.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.society.appname.module.authentication.presentation.mainscreen.MainScreen
import org.society.appname.fcm.NotificationListener
import org.society.appname.module.authentication.presentation.MainViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainNavHost(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    val mainViewModel: MainViewModel = koinViewModel()
    NotificationListener { token ->
        mainViewModel.saveToken(token)
    }


    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home.route
    ) {
        composable(BottomNavItem.Home.route) {
            MainScreen(onLogout = onLogout)
        }
        composable(BottomNavItem.Profile.route) {
            // TODO: ProfileScreen
        }
        composable(BottomNavItem.Settings.route) {
            // TODO: SettingsScreen
        }
    }
}