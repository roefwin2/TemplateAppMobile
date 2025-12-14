package org.society.appname.di

import org.koin.android.ext.koin.androidContext
import org.society.appname.authentication.providers.SocialAuthManagerAndroid
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.society.appname.authentication.providers.SocialAuthManager
import org.society.appname.payment.StripePaymentClient
import org.society.appname.payment.StripePlatformConfig

actual val platformModule = module {
    singleOf(::SocialAuthManagerAndroid).bind(SocialAuthManager::class)
    single { StripePlatformConfig(androidContext()) }
    single { StripePaymentClient(get()) }
}