package com.example.tapemeter.data.model

import java.math.BigDecimal

data class TapeMeterResult(
    val tapePerBoxMm: BigDecimal,
    val baseTapeMm: BigDecimal,
    val wasteMm: BigDecimal,
    val totalTapeMm: BigDecimal,
    val rollsRequired: Int?,
    val leftoverTapeMm: BigDecimal?,
    val topTapeMm: BigDecimal,
    val bottomTapeMm: BigDecimal,
    val sideTapeMm: BigDecimal
)
