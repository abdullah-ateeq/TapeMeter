package com.example.tapemeter.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.tapemeter.data.model.MeasurementUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitSelector(
    selectedUnit: MeasurementUnit,
    units: List<MeasurementUnit> = MeasurementUnit.dimensionUnits,
    onUnitSelected: (MeasurementUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth()
    ) {
        units.forEachIndexed { index, unit ->
            val isSelected = unit == selectedUnit
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = units.size),
                onClick = { onUnitSelected(unit) },
                selected = isSelected,
                label = {
                    Text(
                        text = unit.symbol,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}
