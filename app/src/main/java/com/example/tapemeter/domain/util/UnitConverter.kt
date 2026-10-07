package com.example.tapemeter.domain.util

import com.example.tapemeter.data.model.MeasurementUnit
import java.math.BigDecimal
import java.math.RoundingMode

object UnitConverter {

    /**
     * Converts a linear measurement from the specified unit to millimeters.
     */
    fun toMillimeters(value: BigDecimal, fromUnit: MeasurementUnit): BigDecimal {
        return value.multiply(fromUnit.toMmFactor)
    }

    /**
     * Converts a linear measurement from millimeters to the specified unit.
     */
    fun fromMillimeters(valueMm: BigDecimal, toUnit: MeasurementUnit, scale: Int = 8): BigDecimal {
        if (toUnit.toMmFactor.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
        return valueMm.divide(toUnit.toMmFactor, scale, RoundingMode.HALF_UP)
    }

    /**
     * Converts directly from one measurement unit to another.
     */
    fun convert(
        value: BigDecimal,
        fromUnit: MeasurementUnit,
        toUnit: MeasurementUnit,
        scale: Int = 8
    ): BigDecimal {
        if (fromUnit == toUnit) return value
        val inMm = toMillimeters(value, fromUnit)
        return fromMillimeters(inMm, toUnit, scale)
    }
}
