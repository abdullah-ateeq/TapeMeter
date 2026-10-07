package com.example.tapemeter.data.model

import java.math.BigDecimal

enum class AppTheme(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark");

    companion object {
        fun fromName(name: String?): AppTheme {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: SYSTEM
        }
    }
}

data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val dynamicColor: Boolean = true,
    val defaultPattern: TapePattern = TapePattern.TOP_BOTTOM,
    val defaultUnit: MeasurementUnit = MeasurementUnit.CENTIMETER,
    val defaultOverlapMm: BigDecimal = BigDecimal("100"), // 10 cm in mm
    val decimalPlaces: Int = 2,
    val defaultWastePercent: BigDecimal = BigDecimal("10"),
    val showDiagram: Boolean = true,
    val showFormula: Boolean = true,
    val showTapeBreakdown: Boolean = true,
    val showRollCalculator: Boolean = false,
    val hapticFeedback: Boolean = true,
    val confirmDelete: Boolean = true
)
