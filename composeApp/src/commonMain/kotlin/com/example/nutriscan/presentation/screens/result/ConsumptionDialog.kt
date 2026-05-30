package com.example.nutriscan.presentation.screens.result

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.nutriscan.core.util.format1
import com.example.nutriscan.domain.model.ConsumptionEntry
import com.example.nutriscan.domain.model.Product
import com.example.nutriscan.domain.model.ServingUnit
import com.example.nutriscan.presentation.components.GradientButton
import com.example.nutriscan.presentation.components.SelectableChip
import com.example.nutriscan.presentation.theme.NutrientCalories
import com.example.nutriscan.presentation.theme.NutrientFat
import com.example.nutriscan.presentation.theme.NutrientProtein
import com.example.nutriscan.presentation.theme.NutrientSodium
import com.example.nutriscan.presentation.theme.NutrientSugar

/**
 * Bottom-anchored dialog letting the user pick a serving unit + quantity, with
 * a live preview of the resulting nutrition, before logging the consumption.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConsumptionDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (ConsumptionEntry) -> Unit
) {
    var unit by remember { mutableStateOf(ServingUnit.KEMASAN) }
    var qtyText by remember { mutableStateOf("1") }

    val qty = qtyText.replace(',', '.').toFloatOrNull() ?: 0f
    val grams = (unit.gramsPerUnit(product.servingSize) * qty).coerceAtLeast(0f)

    // Scale the per-100g nutriments to the chosen grams.
    val factor = grams / 100f
    val n = product.nutriments
    val cal = n.calories * factor
    val sugar = n.sugar * factor
    val sodium = n.sodium * factor
    val fat = n.fat * factor
    val protein = n.protein * factor
    val carbs = n.carbs * factor

    val amountLabel = "${qty.format1()} ${unit.displayName} (${grams.format1()} g)"
    val valid = qty > 0f

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(22.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Catat Konsumsi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        product.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Unit picker
            Text("Satuan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ServingUnit.entries.forEach { u ->
                    SelectableChip(
                        text = u.displayName,
                        selected = u == unit,
                        onClick = { unit = u },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Quantity stepper + free input
            Text("Jumlah", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StepperButton(icon = Icons.Filled.Remove) {
                    val v = (qty - stepFor(unit)).coerceAtLeast(0f)
                    qtyText = v.format1()
                }
                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                StepperButton(icon = Icons.Filled.Add) {
                    qtyText = (qty + stepFor(unit)).format1()
                }
            }

            Spacer(Modifier.height(18.dp))

            // Live preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total kalori", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${cal.format1()} kkal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NutrientCalories
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MacroPreview("Gula", sugar, "g", NutrientSugar)
                        MacroPreview("Garam", sodium, "mg", NutrientSodium)
                        MacroPreview("Lemak", fat, "g", NutrientFat)
                        MacroPreview("Protein", protein, "g", NutrientProtein)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        amountLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            GradientButton(
                text = "Tambah ke Konsumsi Harian",
                onClick = {
                    onConfirm(
                        ConsumptionEntry(
                            barcode = product.barcode,
                            productName = product.displayName,
                            amountLabel = amountLabel,
                            grams = grams,
                            calories = cal,
                            fat = fat,
                            sugar = sugar,
                            sodium = sodium,
                            protein = protein,
                            carbs = carbs
                        )
                    )
                },
                enabled = valid,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Batal",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onDismiss() }
                    .padding(12.dp)
            )
        }
    }
}

private fun stepFor(unit: ServingUnit): Float =
    if (unit == ServingUnit.GRAM) 10f else 1f

@Composable
private fun StepperButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun MacroPreview(label: String, value: Float, unit: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            "$label ${value.format1()}$unit",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
