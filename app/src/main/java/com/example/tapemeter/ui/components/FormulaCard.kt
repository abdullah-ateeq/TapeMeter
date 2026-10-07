package com.example.tapemeter.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tapemeter.data.model.MeasurementUnit
import com.example.tapemeter.data.model.TapeMeterResult
import com.example.tapemeter.data.model.TapePattern
import com.example.tapemeter.domain.util.Formatters
import java.math.BigDecimal

@Composable
fun FormulaCard(
    result: TapeMeterResult,
    pattern: TapePattern,
    widthInput: String,
    heightInput: String,
    overlapInput: String,
    boxCount: Int,
    wastePercent: BigDecimal,
    unit: MeasurementUnit,
    decimalPlaces: Int,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HOW IT WAS CALCULATED",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse formula" else "Expand formula",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    val seamFormatted = Formatters.formatTapeLength(result.topTapeMm, unit, decimalPlaces)
                    val tapePerBoxFormatted = Formatters.formatTapeLength(result.tapePerBoxMm, unit, decimalPlaces)
                    val baseTapeM = Formatters.formatTapeLength(result.baseTapeMm, MeasurementUnit.METER, decimalPlaces)
                    val wasteTapeM = Formatters.formatTapeLength(result.wasteMm, MeasurementUnit.METER, decimalPlaces)
                    val totalTapeM = Formatters.formatTapeLength(result.totalTapeMm, MeasurementUnit.METER, decimalPlaces)

                    // Step 1: Center seam
                    FormulaStep(
                        step = "1. Seam Length",
                        explanation = "Width ($widthInput ${unit.symbol}) + Overlap ($overlapInput ${unit.symbol}) = $seamFormatted"
                    )

                    // Step 2: Per box
                    when (pattern) {
                        TapePattern.TOP_ONLY -> {
                            FormulaStep(
                                step = "2. Tape Per Box",
                                explanation = "1 seam (Top Only) = $tapePerBoxFormatted"
                            )
                        }
                        TapePattern.TOP_BOTTOM -> {
                            FormulaStep(
                                step = "2. Tape Per Box",
                                explanation = "Top + Bottom = $seamFormatted × 2 = $tapePerBoxFormatted"
                            )
                        }
                        TapePattern.TOP_BOTTOM_SIDE -> {
                            val sideFormatted = Formatters.formatTapeLength(result.sideTapeMm, unit, decimalPlaces)
                            FormulaStep(
                                step = "2. Tape Per Box",
                                explanation = "Top & Bottom ($seamFormatted × 2) + Side Seam ($heightInput + $overlapInput = $sideFormatted) = $tapePerBoxFormatted"
                            )
                        }
                    }

                    // Step 3: Base total
                    val baseInDimensionUnit = Formatters.formatTapeLength(result.baseTapeMm, unit, decimalPlaces)
                    FormulaStep(
                        step = "3. Batch Total (${Formatters.formatNumber(boxCount)} boxes)",
                        explanation = "$tapePerBoxFormatted × ${Formatters.formatNumber(boxCount)} = $baseInDimensionUnit ($baseTapeM)"
                    )

                    // Step 4: Waste
                    val wasteRatio = wastePercent.divide(BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
                    FormulaStep(
                        step = "4. Extra Allowance (${Formatters.formatPercentage(wastePercent)})",
                        explanation = "$baseTapeM × $wasteRatio = $wasteTapeM extra"
                    )

                    // Step 5: Final Total
                    FormulaStep(
                        step = "5. Total Required",
                        explanation = "$baseTapeM + $wasteTapeM = $totalTapeM"
                    )

                    if (result.rollsRequired != null) {
                        FormulaStep(
                            step = "6. Roll Count",
                            explanation = "Ceiling($totalTapeM ÷ roll length) = ${Formatters.formatRolls(result.rollsRequired)}"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormulaStep(
    step: String,
    explanation: String
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = step,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = explanation,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
