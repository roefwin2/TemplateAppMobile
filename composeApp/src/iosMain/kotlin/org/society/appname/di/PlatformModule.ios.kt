package org.society.appname.di

import org.koin.core.module.Module
import org.society.appname.authentication.providers.SocialAuthManager
import org.society.appname.authentication.providers.SocialAuthManagerIOS
import org.society.appname.payment.StripePaymentClient
import org.society.appname.payment.StripePlatformConfig

actual val platformModule: Module =
    org.koin.dsl.module {
        single<SocialAuthManager> { SocialAuthManagerIOS() }
        single { StripePlatformConfig() }
        single { StripePaymentClient(get()) }
    }