package org.society.appname.di

import org.society.appname.authentication.providers.SocialAuthManagerAndroid
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.society.appname.authentication.providers.SocialAuthManager

actual val platformModule = module {
    singleOf(::SocialAuthManagerAndroid).bind(SocialAuthManager::class)
}