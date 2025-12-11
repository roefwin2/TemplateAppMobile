package org.society.appname.di

import org.koin.core.module.Module
import org.society.appname.authentication.providers.SocialAuthManager
import org.society.appname.authentication.providers.SocialAuthManagerIOS

actual val platformModule: Module =
    org.koin.dsl.module {
        single<SocialAuthManager> { SocialAuthManagerIOS() }
    }