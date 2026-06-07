package com.example.nutriscan.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nutriscan.domain.model.Disease
import com.example.nutriscan.domain.model.UserProfile
import com.example.nutriscan.domain.model.UserRole
import com.example.nutriscan.presentation.components.GradientButton
import com.example.nutriscan.presentation.components.GradientHeader
import com.example.nutriscan.presentation.components.InitialsAvatar
import com.example.nutriscan.presentation.components.LoadingIndicator
import com.example.nutriscan.presentation.components.Pill
import com.example.nutriscan.presentation.components.SelectableChip
import com.example.nutriscan.presentation.components.SoftCard
import com.example.nutriscan.presentation.theme.AppGradients
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        GradientHeader(
            title = "Profil Saya",
            subtitle = state.role.displayName
        )

        if (state.loading) {
            LoadingIndicator()
            return@Column
        }

        val editing = state.editForm
        if (editing != null) {
            EditContent(
                form = editing,
                onNameChange = viewModel::onNameChange,
                onAgeChange = viewModel::onAgeChange,
                onWeightChange = viewModel::onWeightChange,
                onHeightChange = viewModel::onHeightChange,
                onDiseaseToggled = viewModel::onDiseaseToggled,
                onSave = viewModel::saveProfile,
                onCancel = viewModel::cancelEditing
            )
        } else {
            ViewContent(
                state = state,
                onEdit = viewModel::startEditing,
                onToggleDark = viewModel::toggleDarkMode,
                onTopUp = { viewModel.topUp() },
                onLogout = viewModel::logout
            )
        }
    }
}

@Composable
private fun ViewContent(
    state: ProfileUiState,
    onEdit: () -> Unit,
    onToggleDark: (Boolean) -> Unit,
    onTopUp: () -> Unit,
    onLogout: () -> Unit
) {
    val profile = state.profile
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Identity card
        SoftCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                InitialsAvatar(
                    initials = (state.userName.firstOrNull()?.uppercase() ?: "U"),
                    size = 60.dp,
                    brush = if (state.role == UserRole.NUTRITIONIST) AppGradients.energy else AppGradients.brand
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        state.userName.ifBlank { state.role.displayName },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Pill(
                        text = state.role.displayName,
                        icon = if (state.role == UserRole.NUTRITIONIST) Icons.Filled.MedicalServices else null
                    )
                }
            }
        }

        // Coin wallet (USER only)
        if (state.role == UserRole.USER) {
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppGradients.coin),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = Color(0xFF6B4500))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Saldo Coin", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${state.coins} coin", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    GradientButton(
                        text = "Top Up",
                        onClick = onTopUp,
                        leadingIcon = Icons.Filled.Add,
                        brush = AppGradients.coin,
                        height = 44.dp,
                        modifier = Modifier.width(130.dp)
                    )
                }
            }
        }

        // Health profile (USER with profile)
        if (state.role == UserRole.USER && profile != null) {
            HealthProfileCard(profile = profile, onEdit = onEdit)
        }

        // Settings
        SoftCard(modifier = Modifier.fillMaxWidth()) {
            Text("Pengaturan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Mode Gelap", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Text("Tampilan tema gelap", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = state.darkMode, onCheckedChange = onToggleDark)
            }
        }

        // Logout
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Keluar", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun HealthProfileCard(profile: UserProfile, onEdit: () -> Unit) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Data Kesehatan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .clickable { onEdit() }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Edit", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        // BMI highlight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Indeks Massa Tubuh", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(
                        formatOneDecimal(profile.bmi),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Pill(text = profile.bmiCategory)
            }
        }

        Spacer(Modifier.height(12.dp))
        InfoRow("Usia", "${profile.age} tahun")
        InfoRow("Berat Badan", "${formatOneDecimal(profile.weight)} kg")
        InfoRow("Tinggi Badan", "${formatOneDecimal(profile.height)} cm")
        InfoRow("Kebutuhan Kalori", "${profile.dailyCalorieNeed.roundToInt()} kkal/hari")
        InfoRow(
            "Riwayat Penyakit",
            if (profile.healthConditions.isEmpty()) "Tidak ada"
            else profile.healthConditions.joinToString(", ") { it.displayName }
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditContent(
    form: ProfileFormState,
    onNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onDiseaseToggled: (Disease) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SoftCard(modifier = Modifier.fillMaxWidth()) {
            Text("Edit Data Kesehatan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = form.name, onValueChange = onNameChange,
                label = { Text("Nama Lengkap") }, isError = form.nameError != null,
                supportingText = form.nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = form.age, onValueChange = onAgeChange,
                label = { Text("Usia (tahun)") }, isError = form.ageError != null,
                supportingText = form.ageError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = form.weight, onValueChange = onWeightChange,
                    label = { Text("Berat (kg)") }, isError = form.weightError != null,
                    supportingText = form.weightError?.let { { Text(it) } },
                    modifier = Modifier.weight(1f), singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = form.height, onValueChange = onHeightChange,
                    label = { Text("Tinggi (cm)") }, isError = form.heightError != null,
                    supportingText = form.heightError?.let { { Text(it) } },
                    modifier = Modifier.weight(1f), singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        }

        SoftCard(modifier = Modifier.fillMaxWidth()) {
            Text("Riwayat Penyakit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Disease.entries.forEach { disease ->
                    SelectableChip(
                        text = disease.displayName,
                        selected = disease in form.selectedDiseases,
                        onClick = { onDiseaseToggled(disease) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }

        GradientButton(
            text = "Simpan Perubahan",
            onClick = onSave,
            enabled = form.isValid,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Batal")
        }
        Spacer(Modifier.height(8.dp))
    }
}

private fun formatOneDecimal(value: Float): String {
    val rounded = (value * 10).roundToInt() / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}
