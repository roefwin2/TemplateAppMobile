package org.society.appname.module.authentication.presentation.navigation

sealed class BottomNavItem(val route: String, val title: String) {
    object Home : BottomNavItem("home", "Accueil")
    object Profile : BottomNavItem("profile", "Profil")
    object Settings : BottomNavItem("settings", "Paramètres")
}