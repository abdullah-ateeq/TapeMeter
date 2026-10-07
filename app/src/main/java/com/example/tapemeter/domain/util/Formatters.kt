package com.example.tapemeter.domain.util

import com.example.tapemeter.data.model.MeasurementUnit
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

object Formatters {

    fun formatDecimal(
        value: BigDecimal,
        decimalPlaces: Int,
        stripZerosIfWhole: Boolean = false
    ): String {
        val rounded = value.setScale(decimalPlaces, RoundingMode.HALF_UP)
        if (stripZerosIfWhole && rounded.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            return rounded.toBigInteger().toString()
        }
        if (decimalPlaces == 0) {
            return rounded.toBigInteger().toString()
        }
        return rounded.toPlainString()
    }

    fun formatDimension(
        valueMm: BigDecimal,
        unit: MeasurementUnit,
        decimalPlaces: Int = 2
    ): String {
        val inUnit = UnitConverter.fromMillimeters(valueMm, unit, 8)
        val formatted = formatDecimal(inUnit, decimalPlaces, stripZerosIfWhole = true)
        return "$formatted ${unit.symbol}"
    }

    fun formatTapeLength(
        valueMm: BigDecimal,
        unit: MeasurementUnit,
        decimalPlaces: Int = 2
    ): String {
        val inUnit = UnitConverter.fromMillimeters(valueMm, unit, 8)
        val formatted = formatDecimal(inUnit, decimalPlaces, stripZerosIfWhole = false)
        return "$formatted ${unit.symbol}"
    }

    fun formatNumber(number: Number): String {
        return NumberFormat.getIntegerInstance(Locale.US).format(number)
    }

    fun formatPercentage(percent: BigDecimal): String {
        val plain = percent.setScale(1, RoundingMode.HALF_UP)
        val display = if (plain.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            plain.toBigInteger().toString()
        } else {
            plain.toPlainString()
        }
        return "$display%"
    }

    fun formatRolls(rolls: Int): String {
        return if (rolls == 1) "1 roll" else "$rolls rolls"
    }
}
