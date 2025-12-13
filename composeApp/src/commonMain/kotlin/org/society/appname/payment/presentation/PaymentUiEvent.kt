package org.society.appname.payment.presentation

sealed class PaymentUiEvent {
    data class UpdateAmount(val value: String) : PaymentUiEvent()
    data class UpdateCurrency(val value: String) : PaymentUiEvent()
    data class UpdateDescription(val value: String) : PaymentUiEvent()
    data class UpdateEmail(val value: String) : PaymentUiEvent()
    data class UpdatePublishableKey(val value: String) : PaymentUiEvent()
    data class UpdateClientSecret(val value: String) : PaymentUiEvent()
    data object Submit : PaymentUiEvent()
    data object ClearMessage : PaymentUiEvent()
}
