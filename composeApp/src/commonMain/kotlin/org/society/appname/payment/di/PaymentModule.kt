package org.society.appname.payment.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.society.appname.payment.data.StripePaymentRepository
import org.society.appname.payment.domain.PaymentRepository
import org.society.appname.payment.domain.ProcessPaymentUseCase
import org.society.appname.payment.presentation.PaymentViewModel

val paymentCommonModule = module {
    factoryOf(::StripePaymentRepository) bind PaymentRepository::class
    factoryOf(::ProcessPaymentUseCase)
    viewModelOf(::PaymentViewModel)
}

expect val paymentModule: Module
