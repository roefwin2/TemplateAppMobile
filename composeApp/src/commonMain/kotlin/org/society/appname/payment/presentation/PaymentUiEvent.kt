package org.society.appname.payment.presentation

sealed class PaymentUiEvent {
    // Payment info
    data class UpdateAmount(val value: String) : PaymentUiEvent()
    data class UpdateCurrency(val value: String) : PaymentUiEvent()
    data class UpdateDescription(val value: String) : PaymentUiEvent()
    data class UpdateEmail(val value: String) : PaymentUiEvent()

    // Card details
    data class UpdateCardNumber(val value: String) : PaymentUiEvent()
    data class UpdateExpiryMonth(val value: String) : PaymentUiEvent()
    data class UpdateExpiryYear(val value: String) : PaymentUiEvent()
    data class UpdateCvc(val value: String) : PaymentUiEvent()
    data class UpdateCardholderName(val value: String) : PaymentUiEvent()

    // Stripe config
    data class UpdatePublishableKey(val value: String) : PaymentUiEvent()
    data class UpdateClientSecret(val value: String) : PaymentUiEvent()

    // Actions
    data object Submit : PaymentUiEvent()
    data object ClearMessage : PaymentUiEvent()
    data object ClearError : PaymentUiEvent()
    data object Reset : PaymentUiEvent()
}