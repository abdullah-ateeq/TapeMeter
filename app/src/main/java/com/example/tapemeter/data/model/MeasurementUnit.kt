package com.example.tapemeter.data.model

import java.math.BigDecimal

enum class MeasurementUnit(
    val symbol: String,
    val displayName: String,
    val toMmFactor: BigDecimal
) {
    MILLIMETER("mm", "Millimeters", BigDecimal("1")),
    CENTIMETER("cm", "Centimeters", BigDecimal("10")),
    METER("m", "Meters", BigDecimal("1000")),
    INCH("in", "Inches", BigDecimal("25.4")),
    FOOT("ft", "Feet", BigDecimal("304.8"));

    companion object {
        val dimensionUnits = listOf(MILLIMETER, CENTIMETER, METER, INCH, FOOT)
        val rollUnits = listOf(METER, FOOT)

        fun fromSymbol(symbol: String?): MeasurementUnit {
            return entries.firstOrNull { it.symbol.equals(symbol, ignoreCase = true) } ?: CENTIMETER
        }

        fun fromName(name: String?): MeasurementUnit {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: CENTIMETER
        }
    }
}
