package com.example.tapemeter.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal

data class BoxPreset(
    val name: String,
    val lengthCm: BigDecimal,
    val widthCm: BigDecimal,
    val heightCm: BigDecimal
)

@Composable
fun PresetsBar(
    onPresetSelected: (BigDecimal, BigDecimal, BigDecimal) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(
        BoxPreset("Small", BigDecimal("30"), BigDecimal("20"), BigDecimal("15")),
        BoxPreset("Medium", BigDecimal("40"), BigDecimal("30"), BigDecimal("25")),
        BoxPreset("Large", BigDecimal("60"), BigDecimal("40"), BigDecimal("40"))
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SAMPLE SIZES",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Example only",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { preset ->
                FilterChip(
                    selected = false,
                    onClick = {
                        onPresetSelected(preset.lengthCm, preset.widthCm, preset.heightCm)
                    },
                    label = {
                        Text(
                            text = "${preset.name} (${preset.lengthCm.toPlainString()}×${preset.widthCm.toPlainString()}×${preset.heightCm.toPlainString()} cm)",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}
