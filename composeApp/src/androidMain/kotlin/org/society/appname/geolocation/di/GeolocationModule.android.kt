package org.society.appname.geolocation.di

import org.society.appname.geolocation.AndroidLocationService
import org.society.appname.geolocation.LocationService
import org.society.appname.geolocation.createLocationService
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val geolocationModule: Module = module {
    includes(geolocationCommonModule)

    single<LocationService> {
        createLocationService().also { service ->
            if (service is AndroidLocationService) {
                service.initialize(context = androidContext())
            }
        }
    }
}