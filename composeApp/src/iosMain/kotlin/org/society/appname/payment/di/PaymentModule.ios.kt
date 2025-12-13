package org.society.appname.payment.di

import org.koin.dsl.module
import org.society.appname.payment.StripePaymentClient
import org.society.appname.payment.StripePlatformConfig

val paymentModule = module {
    includes(paymentCommonModule)
    single { StripePlatformConfig() }
    single { StripePaymentClient(get()) }
}
