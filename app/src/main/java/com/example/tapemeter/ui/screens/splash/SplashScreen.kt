package com.example.tapemeter.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tapemeter.ui.theme.KraftAmber
import com.example.tapemeter.ui.theme.TapeHighlight
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1350, easing = LinearEasing)
        )
        delay(150)
        onSplashFinished()
    }

    val p = progress.value
    val outlineAlpha = (p / 0.35f).coerceIn(0f, 1f)
    val topSeamProgress = ((p - 0.35f) / 0.30f).coerceIn(0f, 1f)
    val bottomSeamProgress = ((p - 0.65f) / 0.20f).coerceIn(0f, 1f)
    val titleAlpha = ((p - 0.80f) / 0.20f).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated technical packaging graphic
            Canvas(modifier = Modifier.size(160.dp)) {
                val w = size.width
                val h = size.height

                val cx = w * 0.5f
                val cy = h * 0.5f
                val lx = w * 0.32f
                val ly = h * 0.16f
                val wx = w * 0.32f
                val wy = h * 0.16f
                val boxH = h * 0.36f

                val topBack = Offset(cx, cy - boxH * 0.5f - ly)
                val topLeft = Offset(cx - lx, cy - boxH * 0.5f)
                val topRight = Offset(cx + wx, cy - boxH * 0.5f)
                val topFront = Offset(cx - lx + wx, cy - boxH * 0.5f + ly)

                val bottomLeft = Offset(topLeft.x, topLeft.y + boxH)
                val bottomRight = Offset(topRight.x, topRight.y + boxH)
                val bottomFront = Offset(topFront.x, topFront.y + boxH)

                val boxStrokeColor = Color(0xFF64748B).copy(alpha = outlineAlpha)

                if (outlineAlpha > 0f) {
                    // Box Outline
                    drawLine(boxStrokeColor, topBack, topRight, strokeWidth = 3f)
                    drawLine(boxStrokeColor, topBack, topLeft, strokeWidth = 3f)
                    drawLine(boxStrokeColor, topLeft, topFront, strokeWidth = 3f)
                    drawLine(boxStrokeColor, topRight, topFront, strokeWidth = 3f)

                    drawLine(boxStrokeColor, topLeft, bottomLeft, strokeWidth = 3f)
                    drawLine(boxStrokeColor, topFront, bottomFront, strokeWidth = 3f)
                    drawLine(boxStrokeColor, topRight, bottomRight, strokeWidth = 3f)

                    drawLine(boxStrokeColor, bottomLeft, bottomFront, strokeWidth = 3f)
                    drawLine(boxStrokeColor, bottomRight, bottomFront, strokeWidth = 3f)
                }

                // Top tape path drawing across
                if (topSeamProgress > 0f) {
                    val seamStart = Offset((topBack.x + topRight.x) / 2f, (topBack.y + topRight.y) / 2f)
                    val seamEnd = Offset((topLeft.x + topFront.x) / 2f, (topLeft.y + topFront.y) / 2f)

                    val currentEnd = Offset(
                        seamStart.x + (seamEnd.x - seamStart.x) * topSeamProgress,
                        seamStart.y + (seamEnd.y - seamStart.y) * topSeamProgress
                    )

                    drawLine(
                        color = KraftAmber,
                        start = seamStart,
                        end = currentEnd,
                        strokeWidth = 7f,
                        cap = StrokeCap.Round
                    )
                }

                // Bottom seam
                if (bottomSeamProgress > 0f) {
                    val bStart = Offset((cx + wx * 0.5f), cy + boxH * 0.5f - ly)
                    val bEnd = Offset(bottomFront.x - lx * 0.5f, bottomFront.y)
                    val curB = Offset(
                        bStart.x + (bEnd.x - bStart.x) * bottomSeamProgress,
                        bStart.y + (bEnd.y - bStart.y) * bottomSeamProgress
                    )
                    drawLine(
                        color = TapeHighlight.copy(alpha = 0.8f),
                        start = bStart,
                        end = curB,
                        strokeWidth = 5f,
                        cap = StrokeCap.Round
                    )
                }

                // Measurement line
                if (bottomSeamProgress > 0f) {
                    val mLineY = bottomFront.y + 12f
                    drawLine(
                        color = Color(0xFF94A3B8).copy(alpha = bottomSeamProgress),
                        start = Offset(bottomLeft.x, mLineY),
                        end = Offset(bottomLeft.x + (bottomFront.x - bottomLeft.x) * bottomSeamProgress, mLineY),
                        strokeWidth = 2f
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Title and Subtitle fading in
            Text(
                text = "TAPEMETER",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Black
                ),
                color = MaterialTheme.colorScheme.primary.copy(alpha = titleAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Estimate packing tape requirements",
                style = MaterialTheme.typography.bodyMedium.copy(
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = titleAlpha)
            )
        }
    }
}
