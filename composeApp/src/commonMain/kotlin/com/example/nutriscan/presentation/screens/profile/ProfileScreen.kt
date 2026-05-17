package com.example.nutriscan.presentation.screens.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.example.nutriscan.domain.model.UserProfile
import com.example.nutriscan.presentation.components.ErrorMessage
import com.example.nutriscan.presentation.components.LoadingIndicator
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val uiState     by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.Error) {
            snackbarHost.showSnackbar((uiState as ProfileUiState.Error).message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Saya") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (uiState is ProfileUiState.Viewing) {
                        IconButton(onClick = viewModel::startEditing) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit Profil")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { padding ->
        when (val state = uiState) {
            is ProfileUiState.Loading,
            is ProfileUiState.Saving  -> LoadingIndicator()

            is ProfileUiState.Viewing -> ProfileViewContent(
                profile  = state.profile,
                modifier = Modifier.padding(padding)
            )

            is ProfileUiState.Editing -> ProfileEditContent(
                form           = state.form,
                onNameChange   = viewModel::onNameChange,
                onAgeChange    = viewModel::onAgeChange,
                onWeightChange = viewModel::onWeightChange,
                onHeightChange = viewModel::onHeightChange,
                onDiseaseToggled = viewModel::onDiseaseToggled,
                onSave         = viewModel::saveProfile,
                onCancel       = viewModel::cancelEditing,
                modifier       = Modifier.padding(padding)
            )

            is ProfileUiState.Error   -> ErrorMessage(
                message = state.message,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

// ── View mode ─────────────────────────────────────────────────────────────────

@Composable
private fun ProfileViewContent(
    profile: UserProfile,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Info cards
        InfoCard(label = "Nama",          value = profile.name)
        InfoCard(label = "Usia",          value = "${profile.age} tahun")
        InfoCard(label = "Berat Badan",   value = "${profile.weight} kg")
        InfoCard(label = "Tinggi Badan",  value = "${profile.height} cm")

        // BMI card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors   = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Body Mass Index (BMI)", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = "%.1f — ${profile.bmiCategory}".format(profile.bmi),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // Conditions
        if (profile.healthConditions.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Riwayat Penyakit", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(8.dp))
                    profile.healthConditions.forEach {
                        Text("• ${it.displayName}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            InfoCard(label = "Riwayat Penyakit", value = "Tidak ada")
        }

        // Daily calorie estimate
        InfoCard(
            label = "Estimasi Kebutuhan Kalori Harian",
            value = "${profile.dailyCalorieNeed.toInt()} kcal"
        )
    }
}

@Composable
private fun InfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

// ── Edit mode ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileEditContent(
    form: ProfileFormState,
    onNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onDiseaseToggled: (Disease) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value          = form.name,
            onValueChange  = onNameChange,
            label          = { Text("Nama Lengkap") },
            isError        = form.nameError != null,
            supportingText = form.nameError?.let { { Text(it) } },
            modifier       = Modifier.fillMaxWidth(),
            singleLine     = true
        )

        OutlinedTextField(
            value          = form.age,
            onValueChange  = onAgeChange,
            label          = { Text("Usia (tahun)") },
            isError        = form.ageError != null,
            supportingText = form.ageError?.let { { Text(it) } },
            modifier       = Modifier.fillMaxWidth(),
            singleLine     = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value          = form.weight,
                onValueChange  = onWeightChange,
                label          = { Text("Berat (kg)") },
                isError        = form.weightError != null,
                supportingText = form.weightError?.let { { Text(it) } },
                modifier       = Modifier.weight(1f),
                singleLine     = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value          = form.height,
                onValueChange  = onHeightChange,
                label          = { Text("Tinggi (cm)") },
                isError        = form.heightError != null,
                supportingText = form.heightError?.let { { Text(it) } },
                modifier       = Modifier.weight(1f),
                singleLine     = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }

        Text("Riwayat Penyakit", style = MaterialTheme.typography.titleSmall)

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Disease.entries.forEach { disease ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked         = disease in form.selectedDiseases,
                        onCheckedChange = { onDiseaseToggled(disease) }
                    )
                    Text(disease.displayName, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick  = onSave,
            enabled  = form.isValid,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Simpan Perubahan")
        }

        OutlinedButton(
            onClick  = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Batal")
        }
    }
}
