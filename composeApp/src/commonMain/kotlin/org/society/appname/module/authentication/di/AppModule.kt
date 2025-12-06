package org.society.appname.module.authentication.di

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import org.society.appname.module.authentication.data.AuthRepositoryFirebase
import org.society.appname.module.authentication.domain.repository.AuthRepository
import org.society.appname.module.authentication.domain.usecase.LoginUseCase
import org.society.appname.module.authentication.domain.usecase.LogoutUseCase
import org.society.appname.module.authentication.domain.usecase.RegisterUseCase
import org.society.appname.module.authentication.domain.usecase.SaveTokenUseCase
import org.society.appname.module.authentication.firebase.FirebaseProviders
import org.society.appname.module.authentication.presentation.MainViewModel
import org.society.appname.module.authentication.presentation.login.LoginViewModel
import org.society.appname.module.authentication.presentation.register.RegisterViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {
    // Firebase
    single<FirebaseAuth> { FirebaseProviders.auth }
    single<FirebaseFirestore> { Firebase.firestore }

    // Data
    single<AuthRepository> { AuthRepositoryFirebase(get(), get()) }

    // Domain
    factory { LoginUseCase(get(), get()) }
    factory { RegisterUseCase(get(), get()) }
    factory { LogoutUseCase(get()) }
    factory { SaveTokenUseCase(get()) }

    // ViewModels
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::MainViewModel)
}