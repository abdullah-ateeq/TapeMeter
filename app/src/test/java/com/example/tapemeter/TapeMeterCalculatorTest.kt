package com.example.tapemeter

import com.example.tapemeter.data.model.MeasurementUnit
import com.example.tapemeter.data.model.TapeMeterInput
import com.example.tapemeter.data.model.TapePattern
import com.example.tapemeter.domain.calculator.TapeMeterCalculator
import com.example.tapemeter.domain.util.UnitConverter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

class TapeMeterCalculatorTest {

    // 1. Top-only pattern
    @Test
    fun testTopOnlyPattern() {
        // Width: 30 cm (300 mm), Overlap: 10 cm (100 mm), Boxes: 10
        val widthMm = BigDecimal("300")
        val heightMm = BigDecimal("250")
        val overlapMm = BigDecimal("100")

        val segments = TapeMeterCalculator.calculateSegments(widthMm, heightMm, overlapMm, TapePattern.TOP_ONLY)
        // Tape per box: 30 + 10 = 40 cm (400 mm)
        assertEquals(0, BigDecimal("400").compareTo(segments.totalPerBoxMm))
        assertEquals(0, BigDecimal("400").compareTo(segments.topTapeMm))
        assertEquals(0, BigDecimal.ZERO.compareTo(segments.bottomTapeMm))
        assertEquals(0, BigDecimal.ZERO.compareTo(segments.sideTapeMm))

        val totalTape = TapeMeterCalculator.calculateTotalTape(segments.totalPerBoxMm, 10)
        // Base tape: 400 * 10 = 4000 mm = 4 m
        assertEquals(0, BigDecimal("4000").compareTo(totalTape))
    }

    // 2. Top + bottom pattern
    @Test
    fun testTopBottomPattern() {
        // Width: 30 cm (300 mm), Overlap: 10 cm (100 mm), Boxes: 10
        val widthMm = BigDecimal("300")
        val heightMm = BigDecimal("250")
        val overlapMm = BigDecimal("100")

        val segments = TapeMeterCalculator.calculateSegments(widthMm, heightMm, overlapMm, TapePattern.TOP_BOTTOM)
        // Tape per box: 2 * (30 + 10) = 80 cm (800 mm)
        assertEquals(0, BigDecimal("800").compareTo(segments.totalPerBoxMm))
        assertEquals(0, BigDecimal("400").compareTo(segments.topTapeMm))
        assertEquals(0, BigDecimal("400").compareTo(segments.bottomTapeMm))
        assertEquals(0, BigDecimal.ZERO.compareTo(segments.sideTapeMm))

        val totalTape = TapeMeterCalculator.calculateTotalTape(segments.totalPerBoxMm, 10)
        // Base tape: 800 * 10 = 8000 mm = 8 m
        assertEquals(0, BigDecimal("8000").compareTo(totalTape))
    }

    // 3. Top + bottom + side pattern
    @Test
    fun testTopBottomSidePattern() {
        // Width: 30 cm (300 mm), Height: 25 cm (250 mm), Overlap: 10 cm (100 mm), Boxes: 10
        val widthMm = BigDecimal("300")
        val heightMm = BigDecimal("250")
        val overlapMm = BigDecimal("100")

        val segments = TapeMeterCalculator.calculateSegments(widthMm, heightMm, overlapMm, TapePattern.TOP_BOTTOM_SIDE)
        // Top: 40 cm (400 mm), Bottom: 40 cm (400 mm), Side: 35 cm (350 mm)
        assertEquals(0, BigDecimal("400").compareTo(segments.topTapeMm))
        assertEquals(0, BigDecimal("400").compareTo(segments.bottomTapeMm))
        assertEquals(0, BigDecimal("350").compareTo(segments.sideTapeMm))
        // Tape per box: 400 + 400 + 350 = 1150 mm = 115 cm
        assertEquals(0, BigDecimal("1150").compareTo(segments.totalPerBoxMm))

        val totalTape = TapeMeterCalculator.calculateTotalTape(segments.totalPerBoxMm, 10)
        // Base tape: 1150 * 10 = 11500 mm = 11.5 m
        assertEquals(0, BigDecimal("11500").compareTo(totalTape))
    }

    // 4. Tape per box calculation
    @Test
    fun testTapePerBoxCalculation() {
        val widthMm = BigDecimal("500")
        val heightMm = BigDecimal("400")
        val overlapMm = BigDecimal("50")

        val perBoxTop = TapeMeterCalculator.calculateTapePerBox(widthMm, heightMm, overlapMm, TapePattern.TOP_ONLY)
        assertEquals(0, BigDecimal("550").compareTo(perBoxTop))

        val perBoxTopBottom = TapeMeterCalculator.calculateTapePerBox(widthMm, heightMm, overlapMm, TapePattern.TOP_BOTTOM)
        assertEquals(0, BigDecimal("1100").compareTo(perBoxTopBottom))

        val perBoxAll = TapeMeterCalculator.calculateTapePerBox(widthMm, heightMm, overlapMm, TapePattern.TOP_BOTTOM_SIDE)
        assertEquals(0, BigDecimal("1550").compareTo(perBoxAll))
    }

    // 5. Multiple boxes calculation
    @Test
    fun testMultipleBoxesCalculation() {
        val tapePerBoxMm = BigDecimal("800")
        assertEquals(0, BigDecimal("800").compareTo(TapeMeterCalculator.calculateTotalTape(tapePerBoxMm, 1)))
        assertEquals(0, BigDecimal("40000").compareTo(TapeMeterCalculator.calculateTotalTape(tapePerBoxMm, 50)))
        assertEquals(0, BigDecimal("800000").compareTo(TapeMeterCalculator.calculateTotalTape(tapePerBoxMm, 1000)))
    }

    // 6. Zero waste calculation
    @Test
    fun testZeroWaste() {
        val baseTapeMm = BigDecimal("8000")
        val wasteMm = TapeMeterCalculator.calculateWaste(baseTapeMm, BigDecimal.ZERO)
        assertEquals(0, BigDecimal.ZERO.compareTo(wasteMm))

        val totalWithWaste = TapeMeterCalculator.calculateTotalWithWaste(baseTapeMm, wasteMm)
        assertEquals(0, baseTapeMm.compareTo(totalWithWaste))
    }

    // 7. 10% waste calculation
    @Test
    fun testTenPercentWaste() {
        // Base tape: 8 m (8000 mm), 10% waste -> Extra: 800 mm = 0.80 m, Total: 8800 mm = 8.80 m
        val baseTapeMm = BigDecimal("8000")
        val wastePercent = BigDecimal("10")
        val wasteMm = TapeMeterCalculator.calculateWaste(baseTapeMm, wastePercent)
        assertEquals(0, BigDecimal("800").compareTo(wasteMm))

        val totalMm = TapeMeterCalculator.calculateTotalWithWaste(baseTapeMm, wasteMm)
        assertEquals(0, BigDecimal("8800").compareTo(totalMm))
    }

    // 8. 20% waste calculation
    @Test
    fun testTwentyPercentWaste() {
        val baseTapeMm = BigDecimal("10000")
        val wasteMm = TapeMeterCalculator.calculateWaste(baseTapeMm, BigDecimal("20"))
        assertEquals(0, BigDecimal("2000").compareTo(wasteMm))

        val totalMm = TapeMeterCalculator.calculateTotalWithWaste(baseTapeMm, wasteMm)
        assertEquals(0, BigDecimal("12000").compareTo(totalMm))
    }

    // 9. Roll calculation
    @Test
    fun testRollCalculation() {
        // Total: 8.80 m (8800 mm), Roll: 50 m (50000 mm) -> 1 roll
        val totalMm = BigDecimal("8800")
        val rollMm = BigDecimal("50000")
        val rolls = TapeMeterCalculator.calculateRollsRequired(totalMm, rollMm)
        assertEquals(1, rolls)
    }

    // 10. Exact roll count ceiling
    @Test
    fun testExactRollCount() {
        // Total: 100 m (100000 mm), Roll: 50 m (50000 mm) -> exactly 2 rolls
        val totalMm = BigDecimal("100000")
        val rollMm = BigDecimal("50000")
        val rolls = TapeMeterCalculator.calculateRollsRequired(totalMm, rollMm)
        assertEquals(2, rolls)

        val leftover = TapeMeterCalculator.calculateLeftoverTape(rolls, rollMm, totalMm)
        assertEquals(0, BigDecimal.ZERO.compareTo(leftover))
    }

    // 11. Partial roll requirement ceiling
    @Test
    fun testPartialRollRequirement() {
        // Total: 100.01 m (100010 mm), Roll: 50 m (50000 mm) -> 3 rolls (ceiling)
        val totalMm = BigDecimal("100010")
        val rollMm = BigDecimal("50000")
        val rolls = TapeMeterCalculator.calculateRollsRequired(totalMm, rollMm)
        assertEquals(3, rolls)

        // Multiple rolls example from spec: Total: 120 m (120000 mm), Roll: 50 m (50000 mm) -> 3 rolls
        val specTotal = BigDecimal("120000")
        val specRolls = TapeMeterCalculator.calculateRollsRequired(specTotal, rollMm)
        assertEquals(3, specRolls)
    }

    // 12. Leftover calculation
    @Test
    fun testLeftoverCalculation() {
        // Total: 8.80 m (8800 mm), Roll: 50 m (50000 mm), 1 roll -> Leftover: 41.20 m (41200 mm)
        val totalMm = BigDecimal("8800")
        val rollMm = BigDecimal("50000")
        val rolls = 1
        val leftover = TapeMeterCalculator.calculateLeftoverTape(rolls, rollMm, totalMm)
        assertEquals(0, BigDecimal("41200").compareTo(leftover))

        // Multiple Rolls example from spec: Total: 120 m, Roll: 50 m -> 3 rolls, leftover: 30 m (30000 mm)
        val total120 = BigDecimal("120000")
        val rolls3 = 3
        val leftover30 = TapeMeterCalculator.calculateLeftoverTape(rolls3, rollMm, total120)
        assertEquals(0, BigDecimal("30000").compareTo(leftover30))
    }

    // 13. Unit conversion
    @Test
    fun testUnitConversion() {
        // 1 mm = 1 mm
        assertEquals(0, BigDecimal("1").compareTo(UnitConverter.toMillimeters(BigDecimal("1"), MeasurementUnit.MILLIMETER)))
        // 1 cm = 10 mm
        assertEquals(0, BigDecimal("10").compareTo(UnitConverter.toMillimeters(BigDecimal("1"), MeasurementUnit.CENTIMETER)))
        // 1 m = 1000 mm
        assertEquals(0, BigDecimal("1000").compareTo(UnitConverter.toMillimeters(BigDecimal("1"), MeasurementUnit.METER)))
        // 1 in = 25.4 mm
        assertEquals(0, BigDecimal("25.4").compareTo(UnitConverter.toMillimeters(BigDecimal("1"), MeasurementUnit.INCH)))
        // 1 ft = 304.8 mm
        assertEquals(0, BigDecimal("304.8").compareTo(UnitConverter.toMillimeters(BigDecimal("1"), MeasurementUnit.FOOT)))

        // 30 cm -> 0.30 m
        val converted = UnitConverter.convert(BigDecimal("30"), MeasurementUnit.CENTIMETER, MeasurementUnit.METER, 2)
        assertEquals(0, BigDecimal("0.30").compareTo(converted))
    }

    // 14. Decimal dimensions
    @Test
    fun testDecimalDimensions() {
        val widthMm = BigDecimal("305.5")
        val heightMm = BigDecimal("254.2")
        val overlapMm = BigDecimal("76.2")

        val segments = TapeMeterCalculator.calculateSegments(widthMm, heightMm, overlapMm, TapePattern.TOP_BOTTOM)
        val expectedPerBox = BigDecimal("305.5").add(BigDecimal("76.2")).multiply(BigDecimal("2"))
        assertEquals(0, expectedPerBox.compareTo(segments.totalPerBoxMm))
    }

    // 15. Zero dimensions
    @Test
    fun testZeroDimensions() {
        val total = TapeMeterCalculator.calculateTotalTape(BigDecimal.ZERO, 10)
        assertEquals(0, BigDecimal.ZERO.compareTo(total))

        val rolls = TapeMeterCalculator.calculateRollsRequired(BigDecimal.ZERO, BigDecimal("50000"))
        assertEquals(0, rolls)
    }

    // 16. Negative dimensions handling
    @Test
    fun testNegativeDimensions() {
        // Calculator segment logic clamps negative overlap or negative lengths to ZERO
        val segments = TapeMeterCalculator.calculateSegments(BigDecimal("-10"), BigDecimal("-20"), BigDecimal("-5"), TapePattern.TOP_ONLY)
        assertEquals(0, BigDecimal.ZERO.compareTo(segments.totalPerBoxMm))

        val leftover = TapeMeterCalculator.calculateLeftoverTape(0, BigDecimal("50000"), BigDecimal("10000"))
        assertEquals(0, BigDecimal.ZERO.compareTo(leftover))
    }

    // 17. Zero overlap
    @Test
    fun testZeroOverlap() {
        val widthMm = BigDecimal("300")
        val heightMm = BigDecimal("200")
        val overlapMm = BigDecimal.ZERO

        val segments = TapeMeterCalculator.calculateSegments(widthMm, heightMm, overlapMm, TapePattern.TOP_BOTTOM)
        assertEquals(0, BigDecimal("600").compareTo(segments.totalPerBoxMm))
    }

    // 18. Invalid waste percentage
    @Test
    fun testInvalidWastePercentageClamping() {
        val baseTape = BigDecimal("1000")
        val negativeWaste = TapeMeterCalculator.calculateWaste(baseTape, BigDecimal("-5"))
        assertEquals(0, BigDecimal.ZERO.compareTo(negativeWaste))
    }

    // 19. Invalid box count
    @Test
    fun testInvalidBoxCount() {
        val perBox = BigDecimal("800")
        val zeroBoxes = TapeMeterCalculator.calculateTotalTape(perBox, 0)
        assertEquals(0, BigDecimal.ZERO.compareTo(zeroBoxes))

        val negativeBoxes = TapeMeterCalculator.calculateTotalTape(perBox, -5)
        assertEquals(0, BigDecimal.ZERO.compareTo(negativeBoxes))
    }

    // 20. Invalid roll length
    @Test
    fun testInvalidRollLength() {
        val totalTape = BigDecimal("10000")
        val zeroRoll = TapeMeterCalculator.calculateRollsRequired(totalTape, BigDecimal.ZERO)
        assertEquals(0, zeroRoll)

        val negativeRoll = TapeMeterCalculator.calculateRollsRequired(totalTape, BigDecimal("-100"))
        assertEquals(0, negativeRoll)
    }

    // 21. Large box count
    @Test
    fun testLargeBoxCount() {
        val perBoxMm = BigDecimal("800")
        val largeCount = 100000
        val total = TapeMeterCalculator.calculateTotalTape(perBoxMm, largeCount)
        assertEquals(0, BigDecimal("80000000").compareTo(total)) // 80,000 meters
    }

    // 22. BigDecimal precision without premature rounding
    @Test
    fun testBigDecimalPrecision() {
        val input = TapeMeterInput(
            projectName = "Precision Test",
            lengthMm = BigDecimal("400.123456"),
            widthMm = BigDecimal("300.654321"),
            heightMm = BigDecimal("250.987654"),
            boxCount = 7,
            tapePattern = TapePattern.TOP_BOTTOM,
            overlapMm = BigDecimal("100.111111"),
            wastePercent = BigDecimal("12.3456"),
            useRollCalculator = true,
            rollLengthMm = BigDecimal("50000")
        )

        val result = TapeMeterCalculator.calculate(input)
        assertNotNull(result)
        // Check intermediate precision is retained
        assertTrue(result.baseTapeMm.scale() >= 6)
        assertTrue(result.wasteMm.scale() >= 6)
    }
}
