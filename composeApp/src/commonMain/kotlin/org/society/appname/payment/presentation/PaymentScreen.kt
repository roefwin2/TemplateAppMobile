package org.society.appname.payment.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PaymentScreen(
    modifier: Modifier = Modifier,
    viewModel: PaymentViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Paiement Stripe",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = uiState.amountInput,
            onValueChange = { viewModel.onEvent(PaymentUiEvent.UpdateAmount(it)) },
            label = { Text("Montant (centimes)") },
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.currency,
            onValueChange = { viewModel.onEvent(PaymentUiEvent.UpdateCurrency(it)) },
            label = { Text("Devise (ex: eur)") },
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.description,
            onValueChange = { viewModel.onEvent(PaymentUiEvent.UpdateDescription(it)) },
            label = { Text("Description") }
        )

        OutlinedTextField(
            value = uiState.customerEmail,
            onValueChange = { viewModel.onEvent(PaymentUiEvent.UpdateEmail(it)) },
            label = { Text("Email client (optionnel)") }
        )

        OutlinedTextField(
            value = uiState.cardNumber,
            onValueChange = { viewModel.onEvent(PaymentUiEvent.UpdateCardNumber(it)) },
            label = { Text("Numéro de carte") },
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.expiryMonth,
            onValueChange = { viewModel.onEvent(PaymentUiEvent.UpdateExpiryMonth(it)) },
            label = { Text("Mois d'expiration (MM)") },
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.expiryYear,
            onValueChange = { viewModel.onEvent(PaymentUiEvent.UpdateExpiryYear(it)) },
            label = { Text("Année d'expiration (AAAA)") },
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.cvc,
            onValueChange = { viewModel.onEvent(PaymentUiEvent.UpdateCvc(it)) },
            label = { Text("CVC") },
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.publishableKey,
            onValueChange = { viewModel.onEvent(PaymentUiEvent.UpdatePublishableKey(it)) },
            label = { Text("Clé publique Stripe") }
        )

        OutlinedTextField(
            value = uiState.clientSecret,
            onValueChange = { viewModel.onEvent(PaymentUiEvent.UpdateClientSecret(it)) },
            label = { Text("Client secret PaymentIntent") }
        )

        Button(
            onClick = { viewModel.onEvent(PaymentUiEvent.Submit) },
            enabled = !uiState.isProcessing
        ) {
            if (uiState.isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.height(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Lancer le paiement")
            }
        }

        uiState.resultMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary
            )
        }

        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
