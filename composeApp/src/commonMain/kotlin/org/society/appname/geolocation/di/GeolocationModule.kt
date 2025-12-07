package org.society.appname.geolocation.di

import org.society.appname.geolocation.data.LocationRepositoryImpl
import org.society.appname.geolocation.domain.*
import org.society.appname.geolocation.presentation.MapViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Module Koin commun pour la géolocalisation
 * Les implémentations platform-specific sont dans les modules actual
 */
val geolocationCommonModule = module {
    // Repository
    factory { LocationRepositoryImpl(get()) } bind LocationRepository::class

    // Use Cases
    factoryOf(::GetCurrentLocationUseCase)
    factoryOf(::ObserveLocationUseCase)
    factoryOf(::StartLocationTrackingUseCase)
    factoryOf(::StopLocationTrackingUseCase)
    factoryOf(::CheckLocationPermissionUseCase)
    factoryOf(::RequestLocationPermissionUseCase)

    // ViewModel
    viewModelOf(::MapViewModel)
}

expect val geolocationModule: Module