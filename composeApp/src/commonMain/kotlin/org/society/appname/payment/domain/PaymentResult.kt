package org.society.appname.payment.domain

sealed class PaymentResult {
    data class Success(val message: String) : PaymentResult()
    data class Error(val reason: String) : PaymentResult()
    data object Cancelled : PaymentResult()
}
