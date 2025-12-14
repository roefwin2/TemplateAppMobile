package org.society.appname.payment.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.society.appname.payment.domain.CardBrand
import org.society.appname.payment.domain.PaymentRequest
import org.society.appname.payment.domain.PaymentResult
import org.society.appname.payment.domain.ProcessPaymentUseCase
import org.society.appname.payment.domain.ValidationResult

class PaymentViewModel(
    private val processPayment: ProcessPaymentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    // One-time events (navigation, snackbar, etc.)
    private val _sideEffects = MutableSharedFlow<PaymentSideEffect>()
    val sideEffects = _sideEffects.asSharedFlow()

    fun onEvent(event: PaymentUiEvent) {
        when (event) {
            is PaymentUiEvent.UpdateAmount -> {
                val filtered = event.value.filter { it.isDigit() }
                _uiState.update {
                    it.copy(
                        amountInput = filtered,
                        amountError = null,
                        error = null
                    )
                }
            }
            is PaymentUiEvent.UpdateCurrency -> {
                _uiState.update { it.copy(currency = event.value.lowercase().take(3)) }
            }
            is PaymentUiEvent.UpdateDescription -> {
                _uiState.update { it.copy(description = event.value) }
            }
            is PaymentUiEvent.UpdateEmail -> {
                _uiState.update { it.copy(customerEmail = event.value) }
            }
            is PaymentUiEvent.UpdateCardNumber -> {
                val cleaned = event.value.filter { it.isDigit() }.take(19)
                val formatted = formatCardNumber(cleaned)
                val brand = detectCardBrand(cleaned)
                _uiState.update {
                    it.copy(
                        cardNumber = cleaned,
                        cardNumberFormatted = formatted,
                        cardBrand = brand,
                        cardNumberError = null,
                        error = null
                    )
                }
            }
            is PaymentUiEvent.UpdateExpiryMonth -> {
                val filtered = event.value.filter { it.isDigit() }.take(2)
                _uiState.update {
                    it.copy(
                        expiryMonth = filtered,
                        expiryError = null,
                        error = null
                    )
                }
            }
            is PaymentUiEvent.UpdateExpiryYear -> {
                val filtered = event.value.filter { it.isDigit() }.take(4)
                _uiState.update {
                    it.copy(
                        expiryYear = filtered,
                        expiryError = null,
                        error = null
                    )
                }
            }
            is PaymentUiEvent.UpdateCvc -> {
                val filtered = event.value.filter { it.isDigit() }.take(4)
                _uiState.update {
                    it.copy(
                        cvc = filtered,
                        cvcError = null,
                        error = null
                    )
                }
            }
            is PaymentUiEvent.UpdateCardholderName -> {
                _uiState.update { it.copy(cardholderName = event.value) }
            }
            is PaymentUiEvent.UpdatePublishableKey -> {
                _uiState.update { it.copy(publishableKey = event.value.trim()) }
            }
            is PaymentUiEvent.UpdateClientSecret -> {
                _uiState.update { it.copy(clientSecret = event.value.trim()) }
            }
            PaymentUiEvent.Submit -> submitPayment()
            PaymentUiEvent.ClearMessage -> {
                _uiState.update { it.copy(resultMessage = null) }
            }
            PaymentUiEvent.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }
            PaymentUiEvent.Reset -> {
                _uiState.update {
                    PaymentUiState(
                        publishableKey = it.publishableKey,
                        clientSecret = it.clientSecret
                    )
                }
            }
        }
    }

    private fun submitPayment() {
        val current = _uiState.value
        val amount = current.amountInput.toLongOrNull()
        val expMonth = current.expiryMonth.toIntOrNull()
        val expYear = current.expiryYear.toIntOrNull()

        // Validate amount
        if (amount == null || amount <= 0) {
            _uiState.update {
                it.copy(
                    amountError = "Montant invalide",
                    error = "Veuillez entrer un montant valide"
                )
            }
            return
        }

        // Validate card number
        if (current.cardNumber.length < 13 || current.cardNumber.length > 19) {
            _uiState.update {
                it.copy(
                    cardNumberError = "Numéro de carte invalide",
                    error = "Le numéro de carte doit contenir entre 13 et 19 chiffres"
                )
            }
            return
        }

        // Validate CVC
        if (current.cvc.length < 3 || current.cvc.length > 4) {
            _uiState.update {
                it.copy(
                    cvcError = "CVC invalide",
                    error = "Le CVC doit contenir 3 ou 4 chiffres"
                )
            }
            return
        }

        // Validate expiry
        if (expMonth == null || expMonth !in 1..12 || expYear == null || expYear < 2024) {
            _uiState.update {
                it.copy(
                    expiryError = "Date invalide",
                    error = "Date d'expiration invalide"
                )
            }
            return
        }

        // Validate Stripe config
        if (current.publishableKey.isBlank()) {
            _uiState.update { it.copy(error = "Clé publique Stripe manquante") }
            return
        }

        if (current.clientSecret.isBlank()) {
            _uiState.update { it.copy(error = "Client secret Stripe manquant") }
            return
        }

        // Start processing
        _uiState.update {
            it.copy(
                isProcessing = true,
                error = null,
                resultMessage = null,
                cardNumberError = null,
                expiryError = null,
                cvcError = null,
                amountError = null
            )
        }

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
                clientSecret = current.clientSecret,
                cardholderName = current.cardholderName.ifBlank { null }
            )

            // Validate (skip Luhn - Stripe validera côté serveur)
            when (val validation = request.validate(skipLuhnCheck = true)) {
                is ValidationResult.Invalid -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            error = validation.firstError
                        )
                    }
                    return@launch
                }
                is ValidationResult.Valid -> { /* Continue */ }
            }

            // Process payment
            when (val result = processPayment(request)) {
                is PaymentResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            resultMessage = result.message,
                            error = null
                        )
                    }
                    _sideEffects.emit(PaymentSideEffect.PaymentSuccess(result))
                }

                is PaymentResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            error = result.userFriendlyMessage,
                            resultMessage = null
                        )
                    }
                    _sideEffects.emit(PaymentSideEffect.PaymentFailed(result))
                }

                is PaymentResult.RequiresAction -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            error = "Authentification 3D Secure requise",
                            resultMessage = null
                        )
                    }
                    _sideEffects.emit(PaymentSideEffect.RequiresAuthentication(result))
                }

                PaymentResult.Cancelled -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            error = "Paiement annulé"
                        )
                    }
                    _sideEffects.emit(PaymentSideEffect.PaymentCancelled)
                }
            }
        }
    }

    private fun formatCardNumber(number: String): String {
        return number.chunked(4).joinToString(" ")
    }

    private fun detectCardBrand(number: String): CardBrand {
        return when {
            number.isEmpty() -> CardBrand.UNKNOWN
            number.startsWith("4") -> CardBrand.VISA
            number.startsWith("51") || number.startsWith("52") ||
                    number.startsWith("53") || number.startsWith("54") ||
                    number.startsWith("55") -> CardBrand.MASTERCARD
            number.startsWith("34") || number.startsWith("37") -> CardBrand.AMEX
            number.startsWith("6011") || number.startsWith("65") -> CardBrand.DISCOVER
            number.startsWith("36") || number.startsWith("38") -> CardBrand.DINERS
            number.startsWith("35") -> CardBrand.JCB
            number.startsWith("62") -> CardBrand.UNIONPAY
            number.length >= 2 && number.startsWith("2") -> {
                val prefix = number.take(2).toIntOrNull()
                if (prefix != null && prefix in 22..27) CardBrand.MASTERCARD
                else CardBrand.UNKNOWN
            }
            else -> CardBrand.UNKNOWN
        }
    }
}

/**
 * One-time side effects for the payment screen
 */
sealed class PaymentSideEffect {
    data class PaymentSuccess(val result: PaymentResult.Success) : PaymentSideEffect()
    data class PaymentFailed(val result: PaymentResult.Error) : PaymentSideEffect()
    data class RequiresAuthentication(val result: PaymentResult.RequiresAction) : PaymentSideEffect()
    data object PaymentCancelled : PaymentSideEffect()
}