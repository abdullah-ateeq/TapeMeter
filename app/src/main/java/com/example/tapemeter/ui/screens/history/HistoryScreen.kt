package com.example.tapemeter.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.tapemeter.data.model.MeasurementUnit
import com.example.tapemeter.data.model.SavedCalculation
import com.example.tapemeter.data.model.TapePattern
import com.example.tapemeter.domain.util.Formatters
import com.example.tapemeter.domain.util.UnitConverter
import com.example.tapemeter.ui.theme.KraftAmber
import com.example.tapemeter.ui.viewmodel.TapeMeterViewModel
import com.example.tapemeter.util.HapticHelper
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: TapeMeterViewModel,
    onOpenCalculation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val savedList by viewModel.savedCalculations.collectAsState()
    val searchQuery by viewModel.historySearchQuery.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val haptic = LocalHapticFeedback.current

    var itemToDelete by remember { mutableStateOf<SavedCalculation?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CALCULATION HISTORY",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${savedList.size} saved calculations (max 50)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (savedList.isNotEmpty()) {
                IconButton(
                    onClick = {
                        if (settings.confirmDelete) {
                            showClearAllDialog = true
                        } else {
                            viewModel.clearAllHistory()
                            HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteSweep,
                        contentDescription = "Clear all history",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onHistorySearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by project name or pattern...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onHistorySearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Content List / Empty State
        if (savedList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isEmpty()) "No saved calculations yet" else "No matching calculations found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (searchQuery.isEmpty()) "Save your carton estimates from the Calculator screen to access them anytime offline." else "Try adjusting your search terms.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = savedList,
                    key = { it.id }
                ) { item ->
                    HistoryItemCard(
                        item = item,
                        decimalPlaces = settings.decimalPlaces,
                        onOpen = {
                            viewModel.restoreSavedCalculation(item, onOpenCalculation)
                            HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                        },
                        onDuplicate = {
                            viewModel.duplicateSavedCalculation(item.id)
                            HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                        },
                        onToggleFavorite = {
                            viewModel.toggleFavorite(item.id)
                            HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                        },
                        onDelete = {
                            if (settings.confirmDelete) {
                                itemToDelete = item
                            } else {
                                viewModel.deleteSavedCalculation(item.id)
                                HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                            }
                        }
                    )
                }
            }
        }
    }

    // Confirmation Dialogs
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Calculation") },
            text = { Text("Are you sure you want to delete \"${item.projectName}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSavedCalculation(item.id)
                        itemToDelete = null
                        HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear All History") },
            text = { Text("Are you sure you want to delete all saved calculations? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearAllDialog = false
                        HapticHelper.performLightTick(haptic, settings.hapticFeedback)
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun HistoryItemCard(
    item: SavedCalculation,
    decimalPlaces: Int,
    onOpen: () -> Unit,
    onDuplicate: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val dimUnit = MeasurementUnit.fromSymbol(item.dimensionUnit)
    val length = UnitConverter.fromMillimeters(BigDecimal(item.lengthMm), dimUnit, 2)
    val width = UnitConverter.fromMillimeters(BigDecimal(item.widthMm), dimUnit, 2)
    val height = UnitConverter.fromMillimeters(BigDecimal(item.heightMm), dimUnit, 2)
    val pattern = TapePattern.fromName(item.tapePattern)

    val tapePerBox = Formatters.formatTapeLength(BigDecimal(item.tapePerBoxMm), dimUnit, decimalPlaces)
    val totalTape = Formatters.formatTapeLength(BigDecimal(item.totalTapeMm), MeasurementUnit.METER, decimalPlaces)

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }
    val dateStr = remember(item.timestampMillis) { dateFormat.format(Date(item.timestampMillis)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isFavorite) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Project Name, Date, Favorite Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.projectName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (item.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (item.isFavorite) KraftAmber else MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Box Dimensions & Pattern Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${Formatters.formatDecimal(length, 2, stripZerosIfWhole = true)} × ${Formatters.formatDecimal(width, 2, stripZerosIfWhole = true)} × ${Formatters.formatDecimal(height, 2, stripZerosIfWhole = true)} ${dimUnit.symbol}",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${item.boxCount} boxes",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metric Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = pattern.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$tapePerBox / box",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TOTAL WITH WASTE",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = totalTape,
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (item.useRollCalculator && item.rollsRequired != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Requires ${Formatters.formatRolls(item.rollsRequired)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Card Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete calculation",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }

                IconButton(onClick = onDuplicate) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate calculation",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                FilledTonalButton(
                    onClick = onOpen,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open")
                }
            }
        }
    }
}
