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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorHeart
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nutriscan.domain.model.Disease
import com.example.nutriscan.presentation.components.GradientButton
import com.example.nutriscan.presentation.components.GradientHeader
import com.example.nutriscan.presentation.components.LoadingIndicator
import com.example.nutriscan.presentation.components.SelectableChip
import com.example.nutriscan.presentation.components.SoftCard
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onProfileSaved: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val form by viewModel.form.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is OnboardingUiState.Success -> onProfileSaved()
            is OnboardingUiState.Error -> {
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

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            GradientHeader(
                title = "Lengkapi Profil",
                subtitle = "Agar analisis nutrisi terasa lebih personal"
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
                    .imePadding()
            ) {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Data Diri", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = form.name,
                        onValueChange = viewModel::onNameChange,
                        label = { Text("Nama Lengkap") },
                        isError = form.nameError != null,
                        supportingText = form.nameError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = form.age,
                        onValueChange = viewModel::onAgeChange,
                        label = { Text("Usia (tahun)") },
                        isError = form.ageError != null,
                        supportingText = form.ageError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = form.weight,
                            onValueChange = viewModel::onWeightChange,
                            label = { Text("Berat (kg)") },
                            isError = form.weightError != null,
                            supportingText = form.weightError?.let { { Text(it) } },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = form.height,
                            onValueChange = viewModel::onHeightChange,
                            label = { Text("Tinggi (cm)") },
                            isError = form.heightError != null,
                            supportingText = form.heightError?.let { { Text(it) } },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            Icons.Filled.MonitorHeart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Riwayat Penyakit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        "Opsional — pilih yang sesuai",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Disease.entries.forEach { disease ->
                            SelectableChip(
                                text = disease.displayName,
                                selected = disease in form.selectedDiseases,
                                onClick = { viewModel.onDiseaseToggled(disease) },
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                GradientButton(
                    text = "Mulai Pakai NutriScan",
                    onClick = viewModel::saveProfile,
                    enabled = form.isValid,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
