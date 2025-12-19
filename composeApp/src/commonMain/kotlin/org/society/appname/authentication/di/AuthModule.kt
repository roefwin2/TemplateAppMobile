package org.society.appname.authentication.di

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import org.society.appname.authentication.data.AuthRepositoryFirebase
import org.society.appname.authentication.domain.repository.AuthRepository
import org.society.appname.authentication.domain.usecase.LoginUseCase
import org.society.appname.authentication.domain.usecase.LogoutUseCase
import org.society.appname.authentication.domain.usecase.RegisterUseCase
import org.society.appname.authentication.domain.usecase.SaveTokenUseCase
import org.society.appname.authentication.firebase.FirebaseProviders
import org.society.appname.authentication.presentation.MainViewModel
import org.society.appname.authentication.presentation.login.LoginViewModel
import org.society.appname.authentication.presentation.register.RegisterViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.society.appname.authentication.domain.usecase.AppleSignInUseCase
import org.society.appname.authentication.domain.usecase.GoogleSignInUseCase
import org.society.appname.authentication.presentation.password.ForgotPasswordViewModel

val authModule = module {
    // Firebase
    single<FirebaseAuth> { FirebaseProviders.auth }
    single<FirebaseFirestore> { Firebase.firestore }

    // Data
    single<AuthRepository> { AuthRepositoryFirebase(get(), get(),get()) }

    // Domain
    factory { LoginUseCase(get(), get()) }
    factory { RegisterUseCase(get(), get()) }
    factory { LogoutUseCase(get()) }
    factory { GoogleSignInUseCase(get()) }
    factory { AppleSignInUseCase(get()) }
    factory { SaveTokenUseCase(get()) }

    // ViewModels
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::MainViewModel)
}