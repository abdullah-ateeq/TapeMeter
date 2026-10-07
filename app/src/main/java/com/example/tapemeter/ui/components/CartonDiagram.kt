package com.example.tapemeter.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tapemeter.data.model.MeasurementUnit
import com.example.tapemeter.data.model.TapePattern
import com.example.tapemeter.ui.theme.KraftAmber
import com.example.tapemeter.ui.theme.TapePathLight
import java.math.BigDecimal

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CartonDiagram(
    pattern: TapePattern,
    lengthStr: String,
    widthStr: String,
    heightStr: String,
    unit: MeasurementUnit,
    modifier: Modifier = Modifier
) {
    val outlineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val hiddenEdgeColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    val faceColorTop = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    val faceColorLeft = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val faceColorRight = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    val tapeColor = KraftAmber
    val secondaryTapeColor = TapePathLight

    val diagramDescription = "Technical carton diagram for $pattern sealing. Dimensions: Length $lengthStr ${unit.symbol}, Width $widthStr ${unit.symbol}, Height $heightStr ${unit.symbol}."

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = diagramDescription },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CARTON SCHEMATIC",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = pattern.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Technical Canvas Diagram
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // Center reference point for axonometric projection
                    val cx = w * 0.48f
                    val cy = h * 0.52f

                    // Projection vectors
                    val lx = w * 0.30f  // Length vector along left-down axis
                    val ly = h * 0.16f

                    val wx = w * 0.32f  // Width vector along right-down axis
                    val wy = h * 0.16f

                    val boxHeight = h * 0.38f // Height vector straight down

                    // 8 Vertices of rectangular box
                    // Top face vertices
                    val topBack = Offset(cx, cy - boxHeight * 0.5f - ly)
                    val topLeft = Offset(cx - lx, cy - boxHeight * 0.5f)
                    val topRight = Offset(cx + wx, cy - boxHeight * 0.5f)
                    val topFront = Offset(cx - lx + wx, cy - boxHeight * 0.5f + ly)

                    // Bottom face vertices
                    val bottomBack = Offset(topBack.x, topBack.y + boxHeight)
                    val bottomLeft = Offset(topLeft.x, topLeft.y + boxHeight)
                    val bottomRight = Offset(topRight.x, topRight.y + boxHeight)
                    val bottomFront = Offset(topFront.x, topFront.y + boxHeight)

                    // Draw Faces (Filled)
                    // Top face
                    val topPath = Path().apply {
                        moveTo(topBack.x, topBack.y)
                        lineTo(topRight.x, topRight.y)
                        lineTo(topFront.x, topFront.y)
                        lineTo(topLeft.x, topLeft.y)
                        close()
                    }
                    drawPath(topPath, color = faceColorTop)

                    // Left face (Length x Height)
                    val leftPath = Path().apply {
                        moveTo(topLeft.x, topLeft.y)
                        lineTo(topFront.x, topFront.y)
                        lineTo(bottomFront.x, bottomFront.y)
                        lineTo(bottomLeft.x, bottomLeft.y)
                        close()
                    }
                    drawPath(leftPath, color = faceColorLeft)

                    // Right face (Width x Height)
                    val rightPath = Path().apply {
                        moveTo(topFront.x, topFront.y)
                        lineTo(topRight.x, topRight.y)
                        lineTo(bottomRight.x, bottomRight.y)
                        lineTo(bottomFront.x, bottomFront.y)
                        close()
                    }
                    drawPath(rightPath, color = faceColorRight)

                    // Draw Hidden Back Edges (dashed)
                    val dashedStroke = Stroke(
                        width = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                    drawLine(hiddenEdgeColor, topBack, bottomBack, strokeWidth = 1.5f, pathEffect = dashedStroke.pathEffect)
                    drawLine(hiddenEdgeColor, bottomLeft, bottomBack, strokeWidth = 1.5f, pathEffect = dashedStroke.pathEffect)
                    drawLine(hiddenEdgeColor, bottomRight, bottomBack, strokeWidth = 1.5f, pathEffect = dashedStroke.pathEffect)

                    // Draw Visible Outer Edges
                    val solidStroke = Stroke(width = 2.5f, cap = StrokeCap.Round)
                    // Top face edges
                    drawLine(outlineColor, topBack, topRight, strokeWidth = 2f)
                    drawLine(outlineColor, topBack, topLeft, strokeWidth = 2f)
                    drawLine(outlineColor, topLeft, topFront, strokeWidth = 2.5f)
                    drawLine(outlineColor, topRight, topFront, strokeWidth = 2.5f)

                    // Vertical edges
                    drawLine(outlineColor, topLeft, bottomLeft, strokeWidth = 2.5f)
                    drawLine(outlineColor, topFront, bottomFront, strokeWidth = 2.5f)
                    drawLine(outlineColor, topRight, bottomRight, strokeWidth = 2.5f)

                    // Bottom edges
                    drawLine(outlineColor, bottomLeft, bottomFront, strokeWidth = 2.5f)
                    drawLine(outlineColor, bottomRight, bottomFront, strokeWidth = 2.5f)

                    // --- DRAW TAPE PATHS ---
                    // Center seam runs across Width (between the midpoints of the Length edges: (topLeft->topFront) and (topBack->topRight))
                    val topSeamStart = Offset((topBack.x + topRight.x) / 2f, (topBack.y + topRight.y) / 2f)
                    val topSeamEnd = Offset((topLeft.x + topFront.x) / 2f, (topLeft.y + topFront.y) / 2f)

                    // Top center seam (active in all patterns)
                    drawLine(
                        color = tapeColor,
                        start = topSeamStart,
                        end = topSeamEnd,
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )

                    // Bottom center seam (active in TOP_BOTTOM and TOP_BOTTOM_SIDE)
                    if (pattern == TapePattern.TOP_BOTTOM || pattern == TapePattern.TOP_BOTTOM_SIDE) {
                        val bottomSeamStart = Offset((bottomBack.x + bottomRight.x) / 2f, (bottomBack.y + bottomRight.y) / 2f)
                        val bottomSeamEnd = Offset((bottomLeft.x + bottomFront.x) / 2f, (bottomLeft.y + bottomFront.y) / 2f)
                        drawLine(
                            color = tapeColor,
                            start = bottomSeamStart,
                            end = bottomSeamEnd,
                            strokeWidth = 5f,
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )
                    }

                    // Side seam (active in TOP_BOTTOM_SIDE: along height)
                    if (pattern == TapePattern.TOP_BOTTOM_SIDE) {
                        // Highlight side seam down the right-front face or front vertical seam
                        val sideSeamTop = topFront
                        val sideSeamBottom = bottomFront
                        drawLine(
                            color = secondaryTapeColor,
                            start = sideSeamTop,
                            end = sideSeamBottom,
                            strokeWidth = 6f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Schematic Legend and Dimensions Summary
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Dimensions chip
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "DIMENSIONS: ",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "L $lengthStr × W $widthStr × H $heightStr ${unit.symbol}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Top tape path indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(tapeColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (pattern == TapePattern.TOP_ONLY) "Top Seam (Across W)" else "Top & Bottom Seams (W)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (pattern == TapePattern.TOP_BOTTOM_SIDE) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(secondaryTapeColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Side Seam (Along H)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
