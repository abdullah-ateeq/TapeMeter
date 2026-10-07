package com.example.tapemeter.ui.screens.calculator

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tapemeter.data.model.MeasurementUnit
import com.example.tapemeter.data.model.TapePattern
import com.example.tapemeter.ui.components.CartonDiagram
import com.example.tapemeter.ui.components.FormulaCard
import com.example.tapemeter.ui.components.PresetsBar
import com.example.tapemeter.ui.components.ResultCard
import com.example.tapemeter.ui.components.TapeBreakdownCard
import com.example.tapemeter.ui.components.UnitSelector
import com.example.tapemeter.ui.viewmodel.TapeMeterViewModel
import com.example.tapemeter.util.HapticHelper
import kotlinx.coroutines.launch
import java.math.BigDecimal

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalculatorScreen(
    viewModel: TapeMeterViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.calculatorState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val snackbarHostState = remember { SnackbarHostStateStateWrapper() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState.state) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val isWideScreen = maxWidth >= 720.dp

            if (isWideScreen) {
                // Tablet / Landscape Two-Column Layout
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        CalculatorHeader()
                        Spacer(modifier = Modifier.height(16.dp))
                        InputsSection(
                            viewModel = viewModel,
                            state = state,
                            settings = settings,
                            onHaptic = { HapticHelper.performLightTick(haptic, settings.hapticFeedback) }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        ResultsAndDiagramSection(
                            viewModel = viewModel,
                            state = state,
                            settings = settings,
                            onSave = {
                                viewModel.saveCurrentCalculation {
                                    scope.launch { snackbarHostState.state.showSnackbar("Calculation saved") }
                                    HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                                }
                            },
                            onCopy = {
                                val text = viewModel.generateShareableSummary()
                                val clipManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                clipManager?.setPrimaryClip(android.content.ClipData.newPlainText("TapeMeter Estimate", text))
                                scope.launch { snackbarHostState.state.showSnackbar("Result copied") }
                                HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                            },
                            onShare = {
                                val text = viewModel.generateShareableSummary()
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, text)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Tape Estimate"))
                                HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                            },
                            onReset = {
                                viewModel.resetCalculator()
                                HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                            }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            } else {
                // Phone Portrait Single-Column Layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    CalculatorHeader()
                    Spacer(modifier = Modifier.height(16.dp))

                    InputsSection(
                        viewModel = viewModel,
                        state = state,
                        settings = settings,
                        onHaptic = { HapticHelper.performLightTick(haptic, settings.hapticFeedback) }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    ResultsAndDiagramSection(
                        viewModel = viewModel,
                        state = state,
                        settings = settings,
                        onSave = {
                            viewModel.saveCurrentCalculation {
                                scope.launch { snackbarHostState.state.showSnackbar("Calculation saved") }
                                HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                            }
                        },
                        onCopy = {
                            val text = viewModel.generateShareableSummary()
                            val clipManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                            clipManager?.setPrimaryClip(android.content.ClipData.newPlainText("TapeMeter Estimate", text))
                            scope.launch { snackbarHostState.state.showSnackbar("Result copied") }
                            HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                        },
                        onShare = {
                            val text = viewModel.generateShareableSummary()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, text)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Tape Estimate"))
                            HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                        },
                        onReset = {
                            viewModel.resetCalculator()
                            HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                        }
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

private class SnackbarHostStateStateWrapper {
    val state = SnackbarHostState()
}

@Composable
private fun CalculatorHeader() {
    Column {
        Text(
            text = "TAPEMETER",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Black
            ),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Estimate packing tape requirements.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InputsSection(
    viewModel: TapeMeterViewModel,
    state: com.example.tapemeter.ui.viewmodel.CalculatorUiState,
    settings: com.example.tapemeter.data.model.AppSettings,
    onHaptic: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Project Name
        OutlinedTextField(
            value = state.projectName,
            onValueChange = viewModel::onProjectNameChange,
            label = { Text("Project / Batch Name (Optional)") },
            placeholder = { Text("Untitled Packaging") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Presets Bar
        PresetsBar(
            onPresetSelected = { l, w, h ->
                viewModel.applyBoxPreset(l, w, h)
                onHaptic()
            }
        )

        // Dimension Unit Selector
        Column {
            Text(
                text = "DIMENSION UNIT",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            UnitSelector(
                selectedUnit = state.dimensionUnit,
                onUnitSelected = {
                    viewModel.onDimensionUnitChange(it)
                    onHaptic()
                }
            )
        }

        // Dimensions: Length, Width, Height
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.lengthInput,
                onValueChange = viewModel::onLengthChange,
                label = { Text("Length (${state.dimensionUnit.symbol})") },
                isError = state.lengthError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = state.widthInput,
                onValueChange = viewModel::onWidthChange,
                label = { Text("Width (${state.dimensionUnit.symbol})*") },
                isError = state.widthError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = state.heightInput,
                onValueChange = viewModel::onHeightChange,
                label = { Text("Height (${state.dimensionUnit.symbol})") },
                isError = state.heightError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        // Display individual errors if any
        state.lengthError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall) }
        state.widthError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall) }
        state.heightError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall) }

        Text(
            text = "*Seam length is based on box width for standard center-seam packaging.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )

        // Box Quantity
        Column {
            Text(
                text = "NUMBER OF BOXES",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        viewModel.decrementBoxCount(1)
                        onHaptic()
                    }
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease box count by 1")
                }

                OutlinedTextField(
                    value = state.boxCountInput,
                    onValueChange = viewModel::onBoxCountChange,
                    isError = state.boxCountError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        viewModel.incrementBoxCount(1)
                        onHaptic()
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase box count by 1")
                }
            }

            state.boxCountError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }

            // Quick box count chips
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(1, 10, 50, 100, 500).forEach { count ->
                    FilterChip(
                        selected = state.boxCountInput == count.toString(),
                        onClick = {
                            viewModel.onBoxCountChange(count.toString())
                            onHaptic()
                        },
                        label = { Text("$count") },
                        modifier = Modifier.height(32.dp)
                    )
                }
            }
        }

        // Tape Pattern Selector
        Column {
            Text(
                text = "TAPE SEALING PATTERN",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TapePattern.entries.forEach { pattern ->
                    val isSelected = state.tapePattern == pattern
                    Surface(
                        onClick = {
                            viewModel.onTapePatternChange(pattern)
                            onHaptic()
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = pattern.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = pattern.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Overlap Section
        Column {
            Text(
                text = "TAPE OVERLAP",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = state.overlapInput,
                onValueChange = viewModel::onOverlapChange,
                label = { Text("Overlap (${state.dimensionUnit.symbol})") },
                isError = state.overlapError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            state.overlapError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }

            state.overlapWarning?.let {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("5", "10", "15", "20").forEach { cmVal ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            viewModel.setOverlapPreset(BigDecimal(cmVal))
                            onHaptic()
                        },
                        label = { Text("$cmVal cm") },
                        modifier = Modifier.height(32.dp)
                    )
                }
            }
        }

        // Waste / Extra Section
        Column {
            Text(
                text = "WASTE / EXTRA ALLOWANCE",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = state.wastePercentInput,
                onValueChange = viewModel::onWastePercentChange,
                label = { Text("Extra / Waste (%)") },
                isError = state.wasteError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            state.wasteError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("0", "5", "10", "15", "20", "25").forEach { wasteVal ->
                    FilterChip(
                        selected = state.wastePercentInput == wasteVal,
                        onClick = {
                            viewModel.setWastePreset(BigDecimal(wasteVal))
                            onHaptic()
                        },
                        label = { Text("$wasteVal%") },
                        modifier = Modifier.height(32.dp)
                    )
                }
            }
        }

        // Tape Roll Calculator Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ROLL CALCULATOR",
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Calculate rolls required",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = state.useRollCalculator,
                        onCheckedChange = {
                            viewModel.onUseRollCalculatorChange(it)
                            onHaptic()
                        }
                    )
                }

                if (state.useRollCalculator) {
                    Spacer(modifier = Modifier.height(12.dp))
                    UnitSelector(
                        selectedUnit = state.rollUnit,
                        units = MeasurementUnit.rollUnits,
                        onUnitSelected = {
                            viewModel.onRollUnitChange(it)
                            onHaptic()
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.rollLengthInput,
                        onValueChange = viewModel::onRollLengthChange,
                        label = { Text("Tape Roll Length (${state.rollUnit.symbol})") },
                        isError = state.rollLengthError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    state.rollLengthError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("25", "50", "100", "150", "200").forEach { rollVal ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    viewModel.setRollLengthPreset(BigDecimal(rollVal))
                                    onHaptic()
                                },
                                label = { Text("$rollVal m") },
                                modifier = Modifier.height(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultsAndDiagramSection(
    viewModel: TapeMeterViewModel,
    state: com.example.tapemeter.ui.viewmodel.CalculatorUiState,
    settings: com.example.tapemeter.data.model.AppSettings,
    onSave: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onReset: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Diagram
        if (settings.showDiagram) {
            CartonDiagram(
                pattern = state.tapePattern,
                lengthStr = state.lengthInput,
                widthStr = state.widthInput,
                heightStr = state.heightInput,
                unit = state.dimensionUnit
            )
        }

        // Primary Results
        if (state.isValid && state.result != null) {
            val res = state.result
            val count = state.boxCountInput.toIntOrNull() ?: 10
            val waste = BigDecimal(state.wastePercentInput.ifEmpty { "0" })

            ResultCard(
                result = res,
                boxCount = count,
                dimensionUnit = state.dimensionUnit,
                rollUnit = state.rollUnit,
                decimalPlaces = settings.decimalPlaces,
                wastePercent = waste,
                useRollCalculator = state.useRollCalculator
            )

            // Segment Breakdown
            if (settings.showTapeBreakdown) {
                TapeBreakdownCard(
                    result = res,
                    pattern = state.tapePattern,
                    unit = state.dimensionUnit,
                    decimalPlaces = settings.decimalPlaces
                )
            }

            // Formula Breakdown
            if (settings.showFormula) {
                FormulaCard(
                    result = res,
                    pattern = state.tapePattern,
                    widthInput = state.widthInput,
                    heightInput = state.heightInput,
                    overlapInput = state.overlapInput,
                    boxCount = count,
                    wastePercent = waste,
                    unit = state.dimensionUnit,
                    decimalPlaces = settings.decimalPlaces
                )
            }

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save")
                }

                FilledTonalButton(
                    onClick = onCopy,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy")
                }

                FilledTonalButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share")
                }
            }

            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset Calculator")
            }
        }

        // Disclaimer
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Estimate only. Actual tape usage varies with carton construction, flap size, overlap, and application technique.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
