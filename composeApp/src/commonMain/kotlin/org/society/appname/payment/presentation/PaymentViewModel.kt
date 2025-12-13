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
            is PaymentUiEvent.UpdatePublishableKey -> _uiState.update { it.copy(publishableKey = event.value) }
            is PaymentUiEvent.UpdateClientSecret -> _uiState.update { it.copy(clientSecret = event.value) }
            PaymentUiEvent.Submit -> submitPayment()
            PaymentUiEvent.ClearMessage -> _uiState.update { it.copy(resultMessage = null, error = null) }
        }
    }

    private fun submitPayment() {
        val current = _uiState.value
        val amount = current.amountInput.toLongOrNull()

        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(error = "Montant invalide", resultMessage = null) }
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
