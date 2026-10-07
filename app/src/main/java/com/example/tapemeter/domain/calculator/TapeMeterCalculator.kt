package com.example.tapemeter.domain.calculator

import com.example.tapemeter.data.model.TapeMeterInput
import com.example.tapemeter.data.model.TapeMeterResult
import com.example.tapemeter.data.model.TapePattern
import java.math.BigDecimal
import java.math.RoundingMode

object TapeMeterCalculator {

    data class TapeSegments(
        val topTapeMm: BigDecimal,
        val bottomTapeMm: BigDecimal,
        val sideTapeMm: BigDecimal
    ) {
        val totalPerBoxMm: BigDecimal
            get() = topTapeMm.add(bottomTapeMm).add(sideTapeMm)
    }

    /**
     * Calculates the individual tape segments for a single box.
     */
    fun calculateSegments(
        widthMm: BigDecimal,
        heightMm: BigDecimal,
        overlapMm: BigDecimal,
        pattern: TapePattern
    ): TapeSegments {
        val topTape = widthMm.add(overlapMm).max(BigDecimal.ZERO)
        return when (pattern) {
            TapePattern.TOP_ONLY -> {
                TapeSegments(
                    topTapeMm = topTape,
                    bottomTapeMm = BigDecimal.ZERO,
                    sideTapeMm = BigDecimal.ZERO
                )
            }
            TapePattern.TOP_BOTTOM -> {
                val bottomTape = widthMm.add(overlapMm).max(BigDecimal.ZERO)
                TapeSegments(
                    topTapeMm = topTape,
                    bottomTapeMm = bottomTape,
                    sideTapeMm = BigDecimal.ZERO
                )
            }
            TapePattern.TOP_BOTTOM_SIDE -> {
                val bottomTape = widthMm.add(overlapMm).max(BigDecimal.ZERO)
                val sideTape = heightMm.add(overlapMm).max(BigDecimal.ZERO)
                TapeSegments(
                    topTapeMm = topTape,
                    bottomTapeMm = bottomTape,
                    sideTapeMm = sideTape
                )
            }
        }
    }

    /**
     * Calculates total tape per box for the given pattern.
     */
    fun calculateTapePerBox(
        widthMm: BigDecimal,
        heightMm: BigDecimal,
        overlapMm: BigDecimal,
        pattern: TapePattern
    ): BigDecimal {
        return calculateSegments(widthMm, heightMm, overlapMm, pattern).totalPerBoxMm
    }

    /**
     * Calculates total base tape across all boxes.
     */
    fun calculateTotalTape(
        tapePerBoxMm: BigDecimal,
        boxCount: Int
    ): BigDecimal {
        if (boxCount <= 0) return BigDecimal.ZERO
        return tapePerBoxMm.multiply(BigDecimal(boxCount))
    }

    /**
     * Calculates extra/waste tape amount based on base tape and waste percentage.
     */
    fun calculateWaste(
        baseTapeMm: BigDecimal,
        wastePercent: BigDecimal
    ): BigDecimal {
        if (wastePercent.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO
        return baseTapeMm.multiply(wastePercent).divide(BigDecimal("100"), 10, RoundingMode.HALF_UP)
    }

    /**
     * Calculates total tape required including waste.
     */
    fun calculateTotalWithWaste(
        baseTapeMm: BigDecimal,
        wasteMm: BigDecimal
    ): BigDecimal {
        return baseTapeMm.add(wasteMm)
    }

    /**
     * Calculates number of rolls required using ceiling division.
     * If totalTape == 0 -> 0.
     * Otherwise -> max(1, ceiling(totalTape / rollLength)).
     */
    fun calculateRollsRequired(
        totalTapeMm: BigDecimal,
        rollLengthMm: BigDecimal
    ): Int {
        if (totalTapeMm.compareTo(BigDecimal.ZERO) <= 0 || rollLengthMm.compareTo(BigDecimal.ZERO) <= 0) {
            return 0
        }
        val rolls = totalTapeMm.divide(rollLengthMm, 0, RoundingMode.CEILING).toInt()
        return maxOf(1, rolls)
    }

    /**
     * Calculates estimated unused / leftover tape on the final roll.
     * Formula: (rollsRequired * rollLength) - totalTape
     * Never returns negative.
     */
    fun calculateLeftoverTape(
        rollsRequired: Int,
        rollLengthMm: BigDecimal,
        totalTapeMm: BigDecimal
    ): BigDecimal {
        if (rollsRequired <= 0 || rollLengthMm.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO
        }
        val totalCapacityMm = BigDecimal(rollsRequired).multiply(rollLengthMm)
        val leftover = totalCapacityMm.subtract(totalTapeMm)
        return leftover.max(BigDecimal.ZERO)
    }

    /**
     * Performs end-to-end calculation from TapeMeterInput to TapeMeterResult.
     */
    fun calculate(input: TapeMeterInput): TapeMeterResult {
        val segments = calculateSegments(
            input.widthMm,
            input.heightMm,
            input.overlapMm,
            input.tapePattern
        )
        val tapePerBoxMm = segments.totalPerBoxMm
        val baseTapeMm = calculateTotalTape(tapePerBoxMm, input.boxCount)
        val wasteMm = calculateWaste(baseTapeMm, input.wastePercent)
        val totalTapeMm = calculateTotalWithWaste(baseTapeMm, wasteMm)

        val rollsRequired = if (input.useRollCalculator && input.rollLengthMm != null && input.rollLengthMm > BigDecimal.ZERO) {
            calculateRollsRequired(totalTapeMm, input.rollLengthMm)
        } else {
            null
        }

        val leftoverTapeMm = if (rollsRequired != null && input.rollLengthMm != null) {
            calculateLeftoverTape(rollsRequired, input.rollLengthMm, totalTapeMm)
        } else {
            null
        }

        return TapeMeterResult(
            tapePerBoxMm = tapePerBoxMm,
            baseTapeMm = baseTapeMm,
            wasteMm = wasteMm,
            totalTapeMm = totalTapeMm,
            rollsRequired = rollsRequired,
            leftoverTapeMm = leftoverTapeMm,
            topTapeMm = segments.topTapeMm,
            bottomTapeMm = segments.bottomTapeMm,
            sideTapeMm = segments.sideTapeMm
        )
    }
}
