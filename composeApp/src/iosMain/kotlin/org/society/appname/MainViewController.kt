package org.society.appname

import androidx.compose.ui.window.ComposeUIViewController
import org.society.appname.di.initKoin
import org.society.appname.authentication.presentation.navigation.AppNavHost

fun MainViewController() = ComposeUIViewController {
    initKoin()
    AppNavHost()
}