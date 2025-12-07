package org.society.appname.module.authentication.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.society.appname.geolocation.di.geolocationModule

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(authModule, geolocationModule)
    }