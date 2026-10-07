package com.example.tapemeter.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tapemeter.data.model.AppTheme
import com.example.tapemeter.data.model.MeasurementUnit
import com.example.tapemeter.data.model.TapePattern
import com.example.tapemeter.ui.components.UnitSelector
import com.example.tapemeter.ui.viewmodel.TapeMeterViewModel
import com.example.tapemeter.util.HapticHelper
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TapeMeterViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val haptic = LocalHapticFeedback.current
    var showResetSettingsDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column {
            Text(
                text = "APP SETTINGS",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Black
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Configure packaging preferences and defaults.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Section: Appearance
        SettingsCard(title = "APPEARANCE") {
            // Theme Selector
            Column {
                Text(
                    text = "Theme Mode",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val themes = listOf(AppTheme.SYSTEM, AppTheme.LIGHT, AppTheme.DARK)
                    themes.forEachIndexed { index, theme ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = themes.size),
                            onClick = {
                                viewModel.updateSettings(settings.copy(theme = theme))
                                HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                            },
                            selected = settings.theme == theme,
                            label = { Text(theme.displayName) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic Color Switch
            SettingSwitchRow(
                title = "Dynamic Colors",
                subtitle = "Match system wallpaper tones (Android 12+)",
                checked = settings.dynamicColor,
                onCheckedChange = {
                    viewModel.updateSettings(settings.copy(dynamicColor = it))
                    HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                }
            )
        }

        // Section: Defaults
        SettingsCard(title = "PACKAGING DEFAULTS") {
            // Default Pattern
            Column {
                Text(
                    text = "Default Sealing Pattern",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                TapePattern.entries.forEach { pattern ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(pattern.title, style = MaterialTheme.typography.bodySmall)
                        FilterChip(
                            selected = settings.defaultPattern == pattern,
                            onClick = {
                                viewModel.updateSettings(settings.copy(defaultPattern = pattern))
                                HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                            },
                            label = { Text(if (settings.defaultPattern == pattern) "Selected" else "Select") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            // Default Unit
            Column {
                Text(
                    text = "Default Linear Unit",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                UnitSelector(
                    selectedUnit = settings.defaultUnit,
                    onUnitSelected = {
                        viewModel.updateSettings(settings.copy(defaultUnit = it))
                        HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            // Default Overlap (5, 10, 15, 20 cm)
            Column {
                Text(
                    text = "Default Overlap",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("50" to "5 cm", "100" to "10 cm", "150" to "15 cm", "200" to "20 cm").forEach { (mmVal, label) ->
                        val isSelected = settings.defaultOverlapMm.compareTo(BigDecimal(mmVal)) == 0
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.updateSettings(settings.copy(defaultOverlapMm = BigDecimal(mmVal)))
                                HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                            },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            // Decimal Places (0, 1, 2, 3, 4)
            Column {
                Text(
                    text = "Decimal Places: ${settings.decimalPlaces}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val decOptions = listOf(0, 1, 2, 3, 4)
                    decOptions.forEachIndexed { index, dec ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = decOptions.size),
                            onClick = {
                                viewModel.updateSettings(settings.copy(decimalPlaces = dec))
                                HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                            },
                            selected = settings.decimalPlaces == dec,
                            label = { Text("$dec") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            // Default Waste (0, 5, 10, 15, 20 %)
            Column {
                Text(
                    text = "Default Waste Allowance",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("0", "5", "10", "15", "20").forEach { pct ->
                        val isSelected = settings.defaultWastePercent.compareTo(BigDecimal(pct)) == 0
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.updateSettings(settings.copy(defaultWastePercent = BigDecimal(pct)))
                                HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                            },
                            label = { Text("$pct%") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Section: Display Components
        SettingsCard(title = "DISPLAY & VISIBILITY") {
            SettingSwitchRow(
                title = "Show Carton Diagram",
                subtitle = "Display technical perspective box visualization",
                checked = settings.showDiagram,
                onCheckedChange = {
                    viewModel.updateSettings(settings.copy(showDiagram = it))
                    HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingSwitchRow(
                title = "Show Formula Walkthrough",
                subtitle = "Display detailed step-by-step math card",
                checked = settings.showFormula,
                onCheckedChange = {
                    viewModel.updateSettings(settings.copy(showFormula = it))
                    HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingSwitchRow(
                title = "Show Tape Breakdown",
                subtitle = "Display individual seam segment details",
                checked = settings.showTapeBreakdown,
                onCheckedChange = {
                    viewModel.updateSettings(settings.copy(showTapeBreakdown = it))
                    HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingSwitchRow(
                title = "Show Roll Calculator",
                subtitle = "Enable tape roll requirements section by default",
                checked = settings.showRollCalculator,
                onCheckedChange = {
                    viewModel.updateSettings(settings.copy(showRollCalculator = it))
                    HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                }
            )
        }

        // Section: Feedback & Safety
        SettingsCard(title = "FEEDBACK & SAFETY") {
            SettingSwitchRow(
                title = "Haptic Feedback",
                subtitle = "Tactile response on taps and adjustments",
                checked = settings.hapticFeedback,
                onCheckedChange = {
                    viewModel.updateSettings(settings.copy(hapticFeedback = it))
                    HapticHelper.performLightTick(haptic, it)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingSwitchRow(
                title = "Confirm Deletion",
                subtitle = "Prompt before deleting saved calculations",
                checked = settings.confirmDelete,
                onCheckedChange = {
                    viewModel.updateSettings(settings.copy(confirmDelete = it))
                    HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                }
            )
        }

        // Section: Danger & Reset Actions
        SettingsCard(title = "ACTIONS") {
            OutlinedButton(
                onClick = { showResetSettingsDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("Reset Settings to Default")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showClearHistoryDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("Clear Calculation History")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Reset Settings Confirmation
    if (showResetSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showResetSettingsDialog = false },
            title = { Text("Reset Settings") },
            text = { Text("Restore all packaging preferences and default values back to factory defaults?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetSettings()
                        showResetSettingsDialog = false
                        HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear History Confirmation
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear History") },
            text = { Text("Delete all saved calculations? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearHistoryDialog = false
                        HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
