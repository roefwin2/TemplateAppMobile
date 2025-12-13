package org.society.appname.payment.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.society.appname.payment.domain.CardBrand
import org.society.appname.payment.domain.PaymentResult

@Composable
fun PaymentScreen(
    modifier: Modifier = Modifier,
    viewModel: PaymentViewModel = koinViewModel(),
    onPaymentSuccess: ((PaymentResult.Success) -> Unit)? = null,
    onPaymentFailed: ((PaymentResult.Error) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is PaymentSideEffect.PaymentSuccess -> onPaymentSuccess?.invoke(effect.result)
                is PaymentSideEffect.PaymentFailed -> onPaymentFailed?.invoke(effect.result)
                is PaymentSideEffect.RequiresAuthentication -> {
                    // Handle 3D Secure - could open WebView or native SDK
                }
                PaymentSideEffect.PaymentCancelled -> { }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Text(
            text = "Paiement sécurisé",
            style = MaterialTheme.typography.headlineSmall
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Vos données sont protégées par Stripe",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Amount section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Montant",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.amountInput,
                        onValueChange = { viewModel.onEvent(PaymentUiEvent.UpdateAmount(it)) },
                        label = { Text("Montant (centimes)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = uiState.amountError != null,
                        supportingText = uiState.amountError?.let { { Text(it) } },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = uiState.currency,
                        onValueChange = { viewModel.onEvent(PaymentUiEvent.UpdateCurrency(it)) },
                        label = { Text("Devise") },
                        singleLine = true,
                        modifier = Modifier.width(100.dp)
                    )
                }

                uiState.formattedAmount?.let { amount ->
                    Text(
                        text = "Total: $amount",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // Card details section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Informations de carte",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Show detected card brand
                    if (uiState.cardBrand != CardBrand.UNKNOWN) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = uiState.cardBrand.displayName,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.cardNumberFormatted.ifEmpty { uiState.cardNumber },
                    onValueChange = { viewModel.onEvent(PaymentUiEvent.UpdateCardNumber(it)) },
                    label = { Text("Numéro de carte") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = uiState.cardNumberError != null,
                    supportingText = uiState.cardNumberError?.let { { Text(it) } },
                    placeholder = { Text("1234 5678 9012 3456") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.expiryMonth,
                        onValueChange = { viewModel.onEvent(PaymentUiEvent.UpdateExpiryMonth(it)) },
                        label = { Text("MM") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = uiState.expiryError != null,
                        placeholder = { Text("12") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = uiState.expiryYear,
                        onValueChange = { viewModel.onEvent(PaymentUiEvent.UpdateExpiryYear(it)) },
                        label = { Text("AAAA") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = uiState.expiryError != null,
                        placeholder = { Text("2025") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = uiState.cvc,
                        onValueChange = { viewModel.onEvent(PaymentUiEvent.UpdateCvc(it)) },
                        label = { Text("CVC") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = PasswordVisualTransformation(),
                        isError = uiState.cvcError != null,
                        placeholder = { Text("123") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (uiState.expiryError != null) {
                    Text(
                        text = uiState.expiryError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.cardholderName,
                    onValueChange = { viewModel.onEvent(PaymentUiEvent.UpdateCardholderName(it)) },
                    label = { Text("Nom du titulaire (optionnel)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Optional fields
        OutlinedTextField(
            value = uiState.description,
            onValueChange = { viewModel.onEvent(PaymentUiEvent.UpdateDescription(it)) },
            label = { Text("Description (optionnel)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.customerEmail,
            onValueChange = { viewModel.onEvent(PaymentUiEvent.UpdateEmail(it)) },
            label = { Text("Email client (optionnel)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        // Stripe config (collapsible in production)
        OutlinedTextField(
            value = uiState.publishableKey,
            onValueChange = { viewModel.onEvent(PaymentUiEvent.UpdatePublishableKey(it)) },
            label = { Text("Clé publique Stripe") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.clientSecret,
            onValueChange = { viewModel.onEvent(PaymentUiEvent.UpdateClientSecret(it)) },
            label = { Text("Client secret PaymentIntent") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Submit button
        Button(
            onClick = { viewModel.onEvent(PaymentUiEvent.Submit) },
            enabled = !uiState.isProcessing && uiState.isValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (uiState.isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.height(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = uiState.formattedAmount?.let { "Payer $it" } ?: "Payer",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // Result messages
        uiState.resultMessage?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "✓ $message",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        uiState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "✗ $error",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}