package com.example.tapemeter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tapemeter.data.model.AppSettings
import com.example.tapemeter.data.model.MeasurementUnit
import com.example.tapemeter.data.model.SavedCalculation
import com.example.tapemeter.data.model.TapeMeterInput
import com.example.tapemeter.data.model.TapeMeterResult
import com.example.tapemeter.data.model.TapePattern
import com.example.tapemeter.data.repository.TapeMeterRepository
import com.example.tapemeter.domain.calculator.TapeMeterCalculator
import com.example.tapemeter.domain.util.Formatters
import com.example.tapemeter.domain.util.UnitConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

data class CalculatorUiState(
    val projectName: String = "",
    val lengthInput: String = "40",
    val widthInput: String = "30",
    val heightInput: String = "25",
    val dimensionUnit: MeasurementUnit = MeasurementUnit.CENTIMETER,
    val boxCountInput: String = "10",
    val tapePattern: TapePattern = TapePattern.TOP_BOTTOM,
    val overlapInput: String = "10",
    val wastePercentInput: String = "10",
    val useRollCalculator: Boolean = false,
    val rollLengthInput: String = "50",
    val rollUnit: MeasurementUnit = MeasurementUnit.METER,
    // Validation
    val lengthError: String? = null,
    val widthError: String? = null,
    val heightError: String? = null,
    val boxCountError: String? = null,
    val overlapError: String? = null,
    val wasteError: String? = null,
    val rollLengthError: String? = null,
    val overlapWarning: String? = null,
    // Results
    val result: TapeMeterResult? = null,
    val isValid: Boolean = true
)

class TapeMeterViewModel(
    private val repository: TapeMeterRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    private val _calculatorState = MutableStateFlow(CalculatorUiState())
    val calculatorState: StateFlow<CalculatorUiState> = _calculatorState.asStateFlow()

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    val savedCalculations: StateFlow<List<SavedCalculation>> = combine(
        repository.savedCalculations,
        _historySearchQuery
    ) { list, query ->
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            list
        } else {
            list.filter { item ->
                item.projectName.contains(trimmed, ignoreCase = true) ||
                        item.tapePattern.contains(trimmed, ignoreCase = true) ||
                        TapePattern.fromName(item.tapePattern).title.contains(trimmed, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Initial calculation with default state
        calculateNow()

        // Sync initial settings into state if needed
        viewModelScope.launch {
            repository.settings.collect { currentSettings ->
                // Keep default units/patterns aligned if state is pristine
                _calculatorState.update { current ->
                    if (current.projectName.isEmpty() && current.result == null) {
                        current.copy(
                            tapePattern = currentSettings.defaultPattern,
                            dimensionUnit = currentSettings.defaultUnit,
                            wastePercentInput = currentSettings.defaultWastePercent.stripTrailingZeros().toPlainString(),
                            overlapInput = UnitConverter.fromMillimeters(currentSettings.defaultOverlapMm, currentSettings.defaultUnit, 2)
                                .stripTrailingZeros().toPlainString()
                        )
                    } else {
                        current
                    }
                }
                calculateNow()
            }
        }
    }

    // --- Calculator Input Handlers ---

    fun onProjectNameChange(name: String) {
        _calculatorState.update { it.copy(projectName = name) }
    }

    fun onLengthChange(value: String) {
        _calculatorState.update { it.copy(lengthInput = value) }
        calculateNow()
    }

    fun onWidthChange(value: String) {
        _calculatorState.update { it.copy(widthInput = value) }
        calculateNow()
    }

    fun onHeightChange(value: String) {
        _calculatorState.update { it.copy(heightInput = value) }
        calculateNow()
    }

    fun onDimensionUnitChange(newUnit: MeasurementUnit) {
        _calculatorState.update { current ->
            val oldUnit = current.dimensionUnit
            if (oldUnit == newUnit) return@update current

            fun convertStr(str: String): String {
                val bd = parseBigDecimalSafe(str) ?: return str
                val converted = UnitConverter.convert(bd, oldUnit, newUnit, 4)
                return Formatters.formatDecimal(converted, 2, stripZerosIfWhole = true)
            }

            current.copy(
                dimensionUnit = newUnit,
                lengthInput = convertStr(current.lengthInput),
                widthInput = convertStr(current.widthInput),
                heightInput = convertStr(current.heightInput),
                overlapInput = convertStr(current.overlapInput)
            )
        }
        calculateNow()
    }

    fun onBoxCountChange(value: String) {
        _calculatorState.update { it.copy(boxCountInput = value) }
        calculateNow()
    }

    fun incrementBoxCount(amount: Int = 1) {
        val currentCount = _calculatorState.value.boxCountInput.toIntOrNull() ?: 10
        val newCount = (currentCount + amount).coerceIn(1, 100000)
        onBoxCountChange(newCount.toString())
    }

    fun decrementBoxCount(amount: Int = 1) {
        val currentCount = _calculatorState.value.boxCountInput.toIntOrNull() ?: 10
        val newCount = (currentCount - amount).coerceIn(1, 100000)
        onBoxCountChange(newCount.toString())
    }

    fun onTapePatternChange(pattern: TapePattern) {
        _calculatorState.update { it.copy(tapePattern = pattern) }
        calculateNow()
    }

    fun onOverlapChange(value: String) {
        _calculatorState.update { it.copy(overlapInput = value) }
        calculateNow()
    }

    fun setOverlapPreset(overlapCm: BigDecimal) {
        val inCurrentUnit = UnitConverter.convert(overlapCm, MeasurementUnit.CENTIMETER, _calculatorState.value.dimensionUnit, 4)
        val formatted = Formatters.formatDecimal(inCurrentUnit, 2, stripZerosIfWhole = true)
        onOverlapChange(formatted)
    }

    fun onWastePercentChange(value: String) {
        _calculatorState.update { it.copy(wastePercentInput = value) }
        calculateNow()
    }

    fun setWastePreset(presetPercent: BigDecimal) {
        onWastePercentChange(presetPercent.stripTrailingZeros().toPlainString())
    }

    fun onUseRollCalculatorChange(enabled: Boolean) {
        _calculatorState.update { it.copy(useRollCalculator = enabled) }
        calculateNow()
    }

    fun onRollLengthChange(value: String) {
        _calculatorState.update { it.copy(rollLengthInput = value) }
        calculateNow()
    }

    fun onRollUnitChange(newUnit: MeasurementUnit) {
        _calculatorState.update { current ->
            val oldUnit = current.rollUnit
            if (oldUnit == newUnit) return@update current
            val bd = parseBigDecimalSafe(current.rollLengthInput)
            val newRollInput = if (bd != null) {
                val converted = UnitConverter.convert(bd, oldUnit, newUnit, 4)
                Formatters.formatDecimal(converted, 2, stripZerosIfWhole = true)
            } else current.rollLengthInput

            current.copy(
                rollUnit = newUnit,
                rollLengthInput = newRollInput
            )
        }
        calculateNow()
    }

    fun setRollLengthPreset(lengthMeters: BigDecimal) {
        val inCurrentUnit = UnitConverter.convert(lengthMeters, MeasurementUnit.METER, _calculatorState.value.rollUnit, 4)
        val formatted = Formatters.formatDecimal(inCurrentUnit, 2, stripZerosIfWhole = true)
        onRollLengthChange(formatted)
    }

    fun applyBoxPreset(lengthCm: BigDecimal, widthCm: BigDecimal, heightCm: BigDecimal) {
        val unit = _calculatorState.value.dimensionUnit
        val l = UnitConverter.convert(lengthCm, MeasurementUnit.CENTIMETER, unit, 4)
        val w = UnitConverter.convert(widthCm, MeasurementUnit.CENTIMETER, unit, 4)
        val h = UnitConverter.convert(heightCm, MeasurementUnit.CENTIMETER, unit, 4)

        _calculatorState.update {
            it.copy(
                lengthInput = Formatters.formatDecimal(l, 2, stripZerosIfWhole = true),
                widthInput = Formatters.formatDecimal(w, 2, stripZerosIfWhole = true),
                heightInput = Formatters.formatDecimal(h, 2, stripZerosIfWhole = true)
            )
        }
        calculateNow()
    }

    fun resetCalculator() {
        val currentSettings = settings.value
        val defaultOverlapUnit = UnitConverter.fromMillimeters(currentSettings.defaultOverlapMm, currentSettings.defaultUnit, 2)
        _calculatorState.value = CalculatorUiState(
            projectName = "",
            lengthInput = "40",
            widthInput = "30",
            heightInput = "25",
            dimensionUnit = currentSettings.defaultUnit,
            boxCountInput = "10",
            tapePattern = currentSettings.defaultPattern,
            overlapInput = Formatters.formatDecimal(defaultOverlapUnit, 2, stripZerosIfWhole = true),
            wastePercentInput = currentSettings.defaultWastePercent.stripTrailingZeros().toPlainString(),
            useRollCalculator = false,
            rollLengthInput = "50",
            rollUnit = MeasurementUnit.METER
        )
        calculateNow()
    }

    // --- Validation and Live Calculation ---

    private fun parseBigDecimalSafe(str: String): BigDecimal? {
        val clean = str.trim().replace(',', '.')
        return try {
            if (clean.isEmpty()) null else BigDecimal(clean)
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateNow() {
        val state = _calculatorState.value

        var lengthError: String? = null
        var widthError: String? = null
        var heightError: String? = null
        var boxCountError: String? = null
        var overlapError: String? = null
        var wasteError: String? = null
        var rollLengthError: String? = null
        var overlapWarning: String? = null

        val lengthVal = parseBigDecimalSafe(state.lengthInput)
        if (lengthVal == null || lengthVal <= BigDecimal.ZERO) {
            lengthError = "Length must be greater than zero."
        }

        val widthVal = parseBigDecimalSafe(state.widthInput)
        if (widthVal == null || widthVal <= BigDecimal.ZERO) {
            widthError = "Width must be greater than zero."
        }

        val heightVal = parseBigDecimalSafe(state.heightInput)
        if (heightVal == null || heightVal <= BigDecimal.ZERO) {
            heightError = "Height must be greater than zero."
        }

        val boxCountVal = state.boxCountInput.trim().toIntOrNull()
        if (boxCountVal == null) {
            boxCountError = "Enter a whole number of boxes from 1 to 100,000."
        } else if (boxCountVal < 1) {
            boxCountError = "Enter at least 1 box."
        } else if (boxCountVal > 100000) {
            boxCountError = "Box count cannot exceed 100,000."
        }

        val overlapVal = parseBigDecimalSafe(state.overlapInput)
        if (overlapVal == null || overlapVal < BigDecimal.ZERO) {
            overlapError = "Overlap cannot be negative."
        }

        val wasteVal = parseBigDecimalSafe(state.wastePercentInput)
        if (wasteVal == null || wasteVal < BigDecimal.ZERO || wasteVal > BigDecimal("100")) {
            wasteError = "Waste must be between 0% and 100%."
        }

        val rollLengthVal = if (state.useRollCalculator) {
            val rVal = parseBigDecimalSafe(state.rollLengthInput)
            if (rVal == null || rVal <= BigDecimal.ZERO) {
                rollLengthError = "Roll length must be greater than zero."
                null
            } else {
                rVal
            }
        } else null

        val isValid = lengthError == null &&
                widthError == null &&
                heightError == null &&
                boxCountError == null &&
                overlapError == null &&
                wasteError == null &&
                rollLengthError == null

        val result = if (isValid && lengthVal != null && widthVal != null && heightVal != null && boxCountVal != null && overlapVal != null && wasteVal != null) {
            // Check non-blocking overlap warning
            val seamVal = when (state.tapePattern) {
                TapePattern.TOP_ONLY, TapePattern.TOP_BOTTOM -> widthVal
                TapePattern.TOP_BOTTOM_SIDE -> widthVal.max(heightVal)
            }
            if (overlapVal > seamVal) {
                overlapWarning = "Overlap is longer than the seam. Check the value."
            }

            val lengthMm = UnitConverter.toMillimeters(lengthVal, state.dimensionUnit)
            val widthMm = UnitConverter.toMillimeters(widthVal, state.dimensionUnit)
            val heightMm = UnitConverter.toMillimeters(heightVal, state.dimensionUnit)
            val overlapMm = UnitConverter.toMillimeters(overlapVal, state.dimensionUnit)
            val rollLengthMm = rollLengthVal?.let { UnitConverter.toMillimeters(it, state.rollUnit) }

            val input = TapeMeterInput(
                projectName = state.projectName.ifBlank { "Untitled Packaging" },
                lengthMm = lengthMm,
                widthMm = widthMm,
                heightMm = heightMm,
                boxCount = boxCountVal,
                tapePattern = state.tapePattern,
                overlapMm = overlapMm,
                wastePercent = wasteVal,
                useRollCalculator = state.useRollCalculator,
                rollLengthMm = rollLengthMm
            )
            TapeMeterCalculator.calculate(input)
        } else {
            null
        }

        _calculatorState.update {
            it.copy(
                lengthError = lengthError,
                widthError = widthError,
                heightError = heightError,
                boxCountError = boxCountError,
                overlapError = overlapError,
                wasteError = wasteError,
                rollLengthError = rollLengthError,
                overlapWarning = overlapWarning,
                isValid = isValid,
                result = result
            )
        }
    }

    // --- Share / Copy Result Generation ---

    fun generateShareableSummary(): String {
        val state = _calculatorState.value
        val res = state.result ?: return ""
        val decimalPlaces = settings.value.decimalPlaces

        val lengthVal = parseBigDecimalSafe(state.lengthInput) ?: BigDecimal.ZERO
        val widthVal = parseBigDecimalSafe(state.widthInput) ?: BigDecimal.ZERO
        val heightVal = parseBigDecimalSafe(state.heightInput) ?: BigDecimal.ZERO

        val lengthStr = Formatters.formatDecimal(lengthVal, decimalPlaces, stripZerosIfWhole = true)
        val widthStr = Formatters.formatDecimal(widthVal, decimalPlaces, stripZerosIfWhole = true)
        val heightStr = Formatters.formatDecimal(heightVal, decimalPlaces, stripZerosIfWhole = true)

        val overlapVal = parseBigDecimalSafe(state.overlapInput) ?: BigDecimal.ZERO
        val overlapStr = Formatters.formatDecimal(overlapVal, decimalPlaces, stripZerosIfWhole = true)

        val tapePerBoxFormatted = Formatters.formatTapeLength(res.tapePerBoxMm, state.dimensionUnit, decimalPlaces)
        val baseTapeFormatted = Formatters.formatTapeLength(res.baseTapeMm, MeasurementUnit.METER, decimalPlaces)
        val extraFormatted = Formatters.formatTapeLength(res.wasteMm, MeasurementUnit.METER, decimalPlaces)
        val totalFormatted = Formatters.formatTapeLength(res.totalTapeMm, MeasurementUnit.METER, decimalPlaces)

        return buildString {
            appendLine("TapeMeter")
            if (state.projectName.isNotBlank()) {
                appendLine("Project: ${state.projectName.trim()}")
            }
            appendLine("Box Size: $lengthStr × $widthStr × $heightStr ${state.dimensionUnit.symbol}")
            appendLine("Boxes: ${state.boxCountInput}")
            appendLine("Pattern: ${state.tapePattern.title}")
            appendLine("Overlap: $overlapStr ${state.dimensionUnit.symbol}")
            appendLine("Tape Per Box: $tapePerBoxFormatted")
            appendLine("Base Tape: $baseTapeFormatted")
            appendLine("Waste: ${state.wastePercentInput}%")
            appendLine("Extra: $extraFormatted")
            appendLine("Total Tape: $totalFormatted")
            if (state.useRollCalculator && res.rollsRequired != null) {
                appendLine("Rolls Required: ${res.rollsRequired}")
                if (res.leftoverTapeMm != null) {
                    val leftoverFormatted = Formatters.formatTapeLength(res.leftoverTapeMm, state.rollUnit, decimalPlaces)
                    appendLine("Unused Tape: $leftoverFormatted")
                }
            }
        }.trimEnd()
    }

    // --- Save to History ---

    fun saveCurrentCalculation(onSaved: () -> Unit) {
        val state = _calculatorState.value
        val res = state.result ?: return
        if (!state.isValid) return

        val lengthVal = parseBigDecimalSafe(state.lengthInput) ?: return
        val widthVal = parseBigDecimalSafe(state.widthInput) ?: return
        val heightVal = parseBigDecimalSafe(state.heightInput) ?: return
        val overlapVal = parseBigDecimalSafe(state.overlapInput) ?: return
        val wasteVal = parseBigDecimalSafe(state.wastePercentInput) ?: return
        val boxCount = state.boxCountInput.trim().toIntOrNull() ?: 10

        val lengthMm = UnitConverter.toMillimeters(lengthVal, state.dimensionUnit)
        val widthMm = UnitConverter.toMillimeters(widthVal, state.dimensionUnit)
        val heightMm = UnitConverter.toMillimeters(heightVal, state.dimensionUnit)
        val overlapMm = UnitConverter.toMillimeters(overlapVal, state.dimensionUnit)

        val rollLengthVal = parseBigDecimalSafe(state.rollLengthInput)
        val rollLengthMm = if (state.useRollCalculator && rollLengthVal != null) {
            UnitConverter.toMillimeters(rollLengthVal, state.rollUnit)
        } else null

        val saved = SavedCalculation(
            id = UUID.randomUUID().toString(),
            projectName = state.projectName.ifBlank { "Untitled Packaging" },
            lengthMm = lengthMm.toPlainString(),
            widthMm = widthMm.toPlainString(),
            heightMm = heightMm.toPlainString(),
            dimensionUnit = state.dimensionUnit.symbol,
            boxCount = boxCount,
            tapePattern = state.tapePattern.name,
            overlapMm = overlapMm.toPlainString(),
            wastePercent = wasteVal.toPlainString(),
            useRollCalculator = state.useRollCalculator,
            rollLengthMm = rollLengthMm?.toPlainString(),
            rollUnit = state.rollUnit.symbol,
            tapePerBoxMm = res.tapePerBoxMm.toPlainString(),
            baseTapeMm = res.baseTapeMm.toPlainString(),
            totalTapeMm = res.totalTapeMm.toPlainString(),
            rollsRequired = res.rollsRequired,
            timestampMillis = System.currentTimeMillis(),
            isFavorite = false
        )

        viewModelScope.launch {
            repository.saveCalculation(saved)
            onSaved()
        }
    }

    // --- History Actions ---

    fun onHistorySearchChange(query: String) {
        _historySearchQuery.value = query
    }

    fun restoreSavedCalculation(item: SavedCalculation, onRestored: () -> Unit) {
        val unit = MeasurementUnit.fromSymbol(item.dimensionUnit)
        val rollUnit = MeasurementUnit.fromSymbol(item.rollUnit)

        val l = UnitConverter.fromMillimeters(BigDecimal(item.lengthMm), unit, 4)
        val w = UnitConverter.fromMillimeters(BigDecimal(item.widthMm), unit, 4)
        val h = UnitConverter.fromMillimeters(BigDecimal(item.heightMm), unit, 4)
        val overlap = UnitConverter.fromMillimeters(BigDecimal(item.overlapMm), unit, 4)
        val rollLen = item.rollLengthMm?.let {
            UnitConverter.fromMillimeters(BigDecimal(it), rollUnit, 4)
        }

        _calculatorState.value = CalculatorUiState(
            projectName = if (item.projectName == "Untitled Packaging") "" else item.projectName,
            lengthInput = Formatters.formatDecimal(l, 2, stripZerosIfWhole = true),
            widthInput = Formatters.formatDecimal(w, 2, stripZerosIfWhole = true),
            heightInput = Formatters.formatDecimal(h, 2, stripZerosIfWhole = true),
            dimensionUnit = unit,
            boxCountInput = item.boxCount.toString(),
            tapePattern = TapePattern.fromName(item.tapePattern),
            overlapInput = Formatters.formatDecimal(overlap, 2, stripZerosIfWhole = true),
            wastePercentInput = BigDecimal(item.wastePercent).stripTrailingZeros().toPlainString(),
            useRollCalculator = item.useRollCalculator,
            rollLengthInput = rollLen?.let { Formatters.formatDecimal(it, 2, stripZerosIfWhole = true) } ?: "50",
            rollUnit = rollUnit
        )
        calculateNow()
        onRestored()
    }

    fun duplicateSavedCalculation(id: String) {
        viewModelScope.launch {
            repository.duplicateCalculation(id)
        }
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            repository.toggleFavorite(id)
        }
    }

    fun deleteSavedCalculation(id: String) {
        viewModelScope.launch {
            repository.deleteCalculation(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // --- Settings Actions ---

    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            repository.updateSettings(newSettings)
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            repository.resetSettings()
        }
    }
}

class TapeMeterViewModelFactory(
    private val repository: TapeMeterRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TapeMeterViewModel::class.java)) {
            return TapeMeterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
