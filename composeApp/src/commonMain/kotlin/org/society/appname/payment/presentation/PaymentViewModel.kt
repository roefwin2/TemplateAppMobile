package org.society.appname.payment.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.society.appname.payment.domain.PaymentRequest
import org.society.appname.payment.domain.PaymentResult
import org.society.appname.payment.domain.ProcessPaymentUseCase

class PaymentViewModel(
    private val processPayment: ProcessPaymentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    fun onEvent(event: PaymentUiEvent) {
        when (event) {
            is PaymentUiEvent.UpdateAmount -> _uiState.update { it.copy(amountInput = event.value) }
            is PaymentUiEvent.UpdateCurrency -> _uiState.update { it.copy(currency = event.value.lowercase()) }
            is PaymentUiEvent.UpdateDescription -> _uiState.update { it.copy(description = event.value) }
            is PaymentUiEvent.UpdateEmail -> _uiState.update { it.copy(customerEmail = event.value) }
            is PaymentUiEvent.UpdateCardNumber -> _uiState.update { it.copy(cardNumber = event.value.filterNot { ch -> ch.isWhitespace() }) }
            is PaymentUiEvent.UpdateExpiryMonth -> _uiState.update { it.copy(expiryMonth = event.value.filter { ch -> ch.isDigit() }.take(2)) }
            is PaymentUiEvent.UpdateExpiryYear -> _uiState.update { it.copy(expiryYear = event.value.filter { ch -> ch.isDigit() }.take(4)) }
            is PaymentUiEvent.UpdateCvc -> _uiState.update { it.copy(cvc = event.value.filter { ch -> ch.isDigit() }.take(4)) }
            is PaymentUiEvent.UpdatePublishableKey -> _uiState.update { it.copy(publishableKey = event.value) }
            is PaymentUiEvent.UpdateClientSecret -> _uiState.update { it.copy(clientSecret = event.value) }
            PaymentUiEvent.Submit -> submitPayment()
            PaymentUiEvent.ClearMessage -> _uiState.update { it.copy(resultMessage = null, error = null) }
        }
    }

    private fun submitPayment() {
        val current = _uiState.value
        val amount = current.amountInput.toLongOrNull()
        val expMonth = current.expiryMonth.toIntOrNull()
        val expYear = current.expiryYear.toIntOrNull()

        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(error = "Montant invalide", resultMessage = null) }
            return
        }

        if (current.cardNumber.length !in 13..19 || current.cvc.length !in 3..4) {
            _uiState.update { it.copy(error = "Informations de carte invalides", resultMessage = null) }
            return
        }

        if (expMonth == null || expMonth !in 1..12 || expYear == null || expYear < 2024) {
            _uiState.update { it.copy(error = "Date d'expiration invalide", resultMessage = null) }
            return
        }

        if (current.publishableKey.isBlank() || current.clientSecret.isBlank()) {
            _uiState.update {
                it.copy(error = "Clé publique ou client secret manquant pour Stripe", resultMessage = null)
            }
            return
        }

        _uiState.update { it.copy(isProcessing = true, error = null, resultMessage = null) }

        viewModelScope.launch {
            val request = PaymentRequest(
                amount = amount,
                currency = current.currency.ifBlank { "eur" },
                description = current.description.ifBlank { null },
                customerEmail = current.customerEmail.ifBlank { null },
                cardNumber = current.cardNumber,
                expiryMonth = expMonth,
                expiryYear = expYear,
                cvc = current.cvc,
                publishableKey = current.publishableKey,
                clientSecret = current.clientSecret
            )

            when (val result = processPayment(request)) {
                is PaymentResult.Success -> _uiState.update {
                    it.copy(
                        isProcessing = false,
                        resultMessage = result.message,
                        error = null
                    )
                }

                is PaymentResult.Error -> _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = result.reason
                    )
                }

                PaymentResult.Cancelled -> _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = "Paiement annulé"
                    )
                }
            }
        }
    }
}
