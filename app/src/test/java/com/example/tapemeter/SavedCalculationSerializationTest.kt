package com.example.tapemeter

import com.example.tapemeter.data.model.AppSettings
import com.example.tapemeter.data.model.AppTheme
import com.example.tapemeter.data.model.MeasurementUnit
import com.example.tapemeter.data.model.SavedCalculation
import com.example.tapemeter.data.model.TapePattern
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class SavedCalculationSerializationTest {

    // 23. History serialization & deserialization
    @Test
    fun testHistorySerializationAndDeserialization() {
        val original = SavedCalculation(
            id = "test-uuid-1234",
            projectName = "Online Orders & \"Glass\" Panels",
            lengthMm = "400.5",
            widthMm = "300.25",
            heightMm = "250",
            dimensionUnit = "cm",
            boxCount = 25,
            tapePattern = TapePattern.TOP_BOTTOM_SIDE.name,
            overlapMm = "100",
            wastePercent = "15",
            useRollCalculator = true,
            rollLengthMm = "50000",
            rollUnit = "m",
            tapePerBoxMm = "1150.5",
            baseTapeMm = "28762.5",
            totalTapeMm = "33076.875",
            rollsRequired = 1,
            timestampMillis = 1700000000000L,
            isFavorite = true
        )

        val json = SavedCalculation.toJson(original)
        assertNotNull(json)
        assertTrue(json.contains("test-uuid-1234"))
        assertTrue(json.contains("Online Orders & \\\"Glass\\\" Panels"))

        val parsed = SavedCalculation.fromJsonObject(json)
        assertNotNull(parsed)
        assertEquals(original.id, parsed?.id)
        assertEquals(original.projectName, parsed?.projectName)
        assertEquals(original.lengthMm, parsed?.lengthMm)
        assertEquals(original.widthMm, parsed?.widthMm)
        assertEquals(original.heightMm, parsed?.heightMm)
        assertEquals(original.dimensionUnit, parsed?.dimensionUnit)
        assertEquals(original.boxCount, parsed?.boxCount)
        assertEquals(original.tapePattern, parsed?.tapePattern)
        assertEquals(original.overlapMm, parsed?.overlapMm)
        assertEquals(original.wastePercent, parsed?.wastePercent)
        assertEquals(original.useRollCalculator, parsed?.useRollCalculator)
        assertEquals(original.rollLengthMm, parsed?.rollLengthMm)
        assertEquals(original.rollUnit, parsed?.rollUnit)
        assertEquals(original.rollsRequired, parsed?.rollsRequired)
        assertEquals(original.timestampMillis, parsed?.timestampMillis)
        assertEquals(original.isFavorite, parsed?.isFavorite)

        // Test list serialization & deserialization
        val list = listOf(original, original.copy(id = "item-2", isFavorite = false, projectName = "Second Box"))
        val listJson = SavedCalculation.listToJson(list)
        val deserializedList = SavedCalculation.listFromJson(listJson)
        assertEquals(2, deserializedList.size)
        assertEquals("test-uuid-1234", deserializedList[0].id)
        assertEquals("item-2", deserializedList[1].id)
    }

    // 24. Settings defaults & persistence representation
    @Test
    fun testSettingsDefaults() {
        val settings = AppSettings()
        assertEquals(AppTheme.SYSTEM, settings.theme)
        assertTrue(settings.dynamicColor)
        assertEquals(TapePattern.TOP_BOTTOM, settings.defaultPattern)
        assertEquals(MeasurementUnit.CENTIMETER, settings.defaultUnit)
        assertEquals(0, BigDecimal("100").compareTo(settings.defaultOverlapMm))
        assertEquals(2, settings.decimalPlaces)
        assertEquals(0, BigDecimal("10").compareTo(settings.defaultWastePercent))
        assertTrue(settings.showDiagram)
        assertTrue(settings.showFormula)
        assertTrue(settings.showTapeBreakdown)
        assertFalse(settings.showRollCalculator)
        assertTrue(settings.hapticFeedback)
        assertTrue(settings.confirmDelete)
    }

    // 25. Favorites sorting
    @Test
    fun testFavoritesAppearFirst() {
        val item1 = SavedCalculation(
            id = "1",
            projectName = "Regular Item 1",
            lengthMm = "400",
            widthMm = "300",
            heightMm = "250",
            boxCount = 10,
            tapePattern = "TOP_BOTTOM",
            overlapMm = "100",
            wastePercent = "10",
            useRollCalculator = false,
            rollLengthMm = null,
            tapePerBoxMm = "800",
            baseTapeMm = "8000",
            totalTapeMm = "8800",
            rollsRequired = null,
            timestampMillis = 1000L,
            isFavorite = false
        )

        val item2 = SavedCalculation(
            id = "2",
            projectName = "Favorite Item",
            lengthMm = "400",
            widthMm = "300",
            heightMm = "250",
            boxCount = 10,
            tapePattern = "TOP_BOTTOM",
            overlapMm = "100",
            wastePercent = "10",
            useRollCalculator = false,
            rollLengthMm = null,
            tapePerBoxMm = "800",
            baseTapeMm = "8000",
            totalTapeMm = "8800",
            rollsRequired = null,
            timestampMillis = 500L,
            isFavorite = true
        )

        val item3 = SavedCalculation(
            id = "3",
            projectName = "Regular Item 2 (Newer)",
            lengthMm = "400",
            widthMm = "300",
            heightMm = "250",
            boxCount = 10,
            tapePattern = "TOP_BOTTOM",
            overlapMm = "100",
            wastePercent = "10",
            useRollCalculator = false,
            rollLengthMm = null,
            tapePerBoxMm = "800",
            baseTapeMm = "8000",
            totalTapeMm = "8800",
            rollsRequired = null,
            timestampMillis = 2000L,
            isFavorite = false
        )

        val unsortedList = listOf(item1, item2, item3)
        val sortedList = unsortedList.sortedWith(
            compareByDescending<SavedCalculation> { it.isFavorite }
                .thenByDescending { it.timestampMillis }
        )

        // Favorite item should be first even though it has older timestamp
        assertEquals("2", sortedList[0].id)
        assertTrue(sortedList[0].isFavorite)

        // Then newer non-favorite item
        assertEquals("3", sortedList[1].id)

        // Then older non-favorite item
        assertEquals("1", sortedList[2].id)
    }
}
