package org.society.appname

import androidx.compose.ui.window.ComposeUIViewController
import org.society.appname.module.authentication.di.initKoin
import org.society.appname.module.authentication.presentation.navigation.AppNavHost

fun MainViewController() = ComposeUIViewController {
    initKoin()
    AppNavHost()
}