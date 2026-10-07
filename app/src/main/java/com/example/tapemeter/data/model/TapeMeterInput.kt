package com.example.tapemeter.data.model

import java.math.BigDecimal

data class TapeMeterInput(
    val projectName: String = "Untitled Packaging",
    val lengthMm: BigDecimal,
    val widthMm: BigDecimal,
    val heightMm: BigDecimal,
    val boxCount: Int,
    val tapePattern: TapePattern,
    val overlapMm: BigDecimal,
    val wastePercent: BigDecimal,
    val useRollCalculator: Boolean,
    val rollLengthMm: BigDecimal?
)
