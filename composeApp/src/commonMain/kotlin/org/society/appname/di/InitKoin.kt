package org.society.appname.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.society.appname.authentication.di.authModule
import org.society.appname.geolocation.di.geolocationModule
import org.society.appname.payment.di.paymentModule

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(authModule, geolocationModule, paymentModule, platformModule)
    }