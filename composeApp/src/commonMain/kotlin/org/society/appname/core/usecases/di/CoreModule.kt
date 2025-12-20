package org.society.appname.core.usecases.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import org.society.appname.core.usecases.SaveUserDataUseCase

val coreModule = module {
    factoryOf(::SaveUserDataUseCase)
}