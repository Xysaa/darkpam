package com.example.nutriscan.presentation.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FoodBank
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nutriscan.domain.model.Disease
import com.example.nutriscan.presentation.components.LoadingIndicator
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onProfileSaved: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val form    by viewModel.form.collectAsStateWithLifecycle()

    val snackbarHost = remember { SnackbarHostState() }

    // Navigate away on success
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is OnboardingUiState.Success -> onProfileSaved()
            is OnboardingUiState.Error   -> {
                snackbarHost.showSnackbar(state.message)
                viewModel.resetError()
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { padding ->

        if (uiState is OnboardingUiState.Loading) {
            LoadingIndicator()
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Header ──────────────────────────────────────────────────────
            Icon(
                imageVector        = Icons.Outlined.FoodBank,
                contentDescription = null,
                modifier           = Modifier.size(72.dp),
                tint               = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(8.dp))

            Text("Selamat Datang di NutriScan", style = MaterialTheme.typography.headlineSmall)
            Text(
                text  = "Lengkapi profil Anda untuk mendapatkan analisis nutrisi yang dipersonalisasi.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            // ── Name ─────────────────────────────────────────────────────────
            OutlinedTextField(
                value         = form.name,
                onValueChange = viewModel::onNameChange,
                label         = { Text("Nama Lengkap") },
                isError       = form.nameError != null,
                supportingText = form.nameError?.let { { Text(it) } },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true
            )

            Spacer(Modifier.height(12.dp))

            // ── Age ──────────────────────────────────────────────────────────
            OutlinedTextField(
                value          = form.age,
                onValueChange  = viewModel::onAgeChange,
                label          = { Text("Usia (tahun)") },
                isError        = form.ageError != null,
                supportingText = form.ageError?.let { { Text(it) } },
                modifier       = Modifier.fillMaxWidth(),
                singleLine     = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(Modifier.height(12.dp))

            // ── Weight + Height (side by side) ───────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value          = form.weight,
                    onValueChange  = viewModel::onWeightChange,
                    label          = { Text("Berat (kg)") },
                    isError        = form.weightError != null,
                    supportingText = form.weightError?.let { { Text(it) } },
                    modifier       = Modifier.weight(1f),
                    singleLine     = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                OutlinedTextField(
                    value          = form.height,
                    onValueChange  = viewModel::onHeightChange,
                    label          = { Text("Tinggi (cm)") },
                    isError        = form.heightError != null,
                    supportingText = form.heightError?.let { { Text(it) } },
                    modifier       = Modifier.weight(1f),
                    singleLine     = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Health Conditions ────────────────────────────────────────────
            Text(
                text     = "Riwayat Penyakit (opsional)",
                style    = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            FlowRow(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Disease.entries.forEach { disease ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked         = disease in form.selectedDiseases,
                            onCheckedChange = { viewModel.onDiseaseToggled(disease) }
                        )
                        Text(disease.displayName, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Submit ───────────────────────────────────────────────────────
            Button(
                onClick  = viewModel::saveProfile,
                enabled  = form.isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Mulai Pakai NutriScan", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
