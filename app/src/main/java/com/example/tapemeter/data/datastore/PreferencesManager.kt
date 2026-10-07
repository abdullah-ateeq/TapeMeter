package com.example.tapemeter.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.tapemeter.data.model.AppSettings
import com.example.tapemeter.data.model.AppTheme
import com.example.tapemeter.data.model.MeasurementUnit
import com.example.tapemeter.data.model.SavedCalculation
import com.example.tapemeter.data.model.TapePattern
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.math.BigDecimal

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tapemeter_preferences")

class PreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val THEME = stringPreferencesKey("theme")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val DEFAULT_PATTERN = stringPreferencesKey("default_pattern")
        val DEFAULT_UNIT = stringPreferencesKey("default_unit")
        val DEFAULT_OVERLAP_MM = stringPreferencesKey("default_overlap_mm")
        val DECIMAL_PLACES = intPreferencesKey("decimal_places")
        val DEFAULT_WASTE_PERCENT = stringPreferencesKey("default_waste_percent")
        val SHOW_DIAGRAM = booleanPreferencesKey("show_diagram")
        val SHOW_FORMULA = booleanPreferencesKey("show_formula")
        val SHOW_TAPE_BREAKDOWN = booleanPreferencesKey("show_tape_breakdown")
        val SHOW_ROLL_CALCULATOR = booleanPreferencesKey("show_roll_calculator")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val CONFIRM_DELETE = booleanPreferencesKey("confirm_delete")
        val SAVED_CALCULATIONS_JSON = stringPreferencesKey("saved_calculations_json")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeStr = preferences[PreferencesKeys.THEME] ?: AppTheme.SYSTEM.name
            val dynamicColor = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true
            val patternStr = preferences[PreferencesKeys.DEFAULT_PATTERN] ?: TapePattern.TOP_BOTTOM.name
            val unitStr = preferences[PreferencesKeys.DEFAULT_UNIT] ?: MeasurementUnit.CENTIMETER.symbol
            val overlapMmStr = preferences[PreferencesKeys.DEFAULT_OVERLAP_MM] ?: "100"
            val decimalPlaces = preferences[PreferencesKeys.DECIMAL_PLACES] ?: 2
            val wasteStr = preferences[PreferencesKeys.DEFAULT_WASTE_PERCENT] ?: "10"
            val showDiagram = preferences[PreferencesKeys.SHOW_DIAGRAM] ?: true
            val showFormula = preferences[PreferencesKeys.SHOW_FORMULA] ?: true
            val showTapeBreakdown = preferences[PreferencesKeys.SHOW_TAPE_BREAKDOWN] ?: true
            val showRollCalculator = preferences[PreferencesKeys.SHOW_ROLL_CALCULATOR] ?: false
            val haptic = preferences[PreferencesKeys.HAPTIC_FEEDBACK] ?: true
            val confirmDelete = preferences[PreferencesKeys.CONFIRM_DELETE] ?: true

            AppSettings(
                theme = AppTheme.fromName(themeStr),
                dynamicColor = dynamicColor,
                defaultPattern = TapePattern.fromName(patternStr),
                defaultUnit = MeasurementUnit.fromSymbol(unitStr),
                defaultOverlapMm = BigDecimal(overlapMmStr),
                decimalPlaces = decimalPlaces.coerceIn(0, 4),
                defaultWastePercent = BigDecimal(wasteStr),
                showDiagram = showDiagram,
                showFormula = showFormula,
                showTapeBreakdown = showTapeBreakdown,
                showRollCalculator = showRollCalculator,
                hapticFeedback = haptic,
                confirmDelete = confirmDelete
            )
        }

    val savedCalculationsFlow: Flow<List<SavedCalculation>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val json = preferences[PreferencesKeys.SAVED_CALCULATIONS_JSON] ?: "[]"
            val list = SavedCalculation.listFromJson(json)
            // Sort favorites first, then by timestamp descending
            list.sortedWith(
                compareByDescending<SavedCalculation> { it.isFavorite }
                    .thenByDescending { it.timestampMillis }
            )
        }

    suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = settings.theme.name
            preferences[PreferencesKeys.DYNAMIC_COLOR] = settings.dynamicColor
            preferences[PreferencesKeys.DEFAULT_PATTERN] = settings.defaultPattern.name
            preferences[PreferencesKeys.DEFAULT_UNIT] = settings.defaultUnit.symbol
            preferences[PreferencesKeys.DEFAULT_OVERLAP_MM] = settings.defaultOverlapMm.toPlainString()
            preferences[PreferencesKeys.DECIMAL_PLACES] = settings.decimalPlaces
            preferences[PreferencesKeys.DEFAULT_WASTE_PERCENT] = settings.defaultWastePercent.toPlainString()
            preferences[PreferencesKeys.SHOW_DIAGRAM] = settings.showDiagram
            preferences[PreferencesKeys.SHOW_FORMULA] = settings.showFormula
            preferences[PreferencesKeys.SHOW_TAPE_BREAKDOWN] = settings.showTapeBreakdown
            preferences[PreferencesKeys.SHOW_ROLL_CALCULATOR] = settings.showRollCalculator
            preferences[PreferencesKeys.HAPTIC_FEEDBACK] = settings.hapticFeedback
            preferences[PreferencesKeys.CONFIRM_DELETE] = settings.confirmDelete
        }
    }

    suspend fun resetSettings() {
        updateSettings(AppSettings())
    }

    suspend fun saveCalculation(calculation: SavedCalculation) {
        context.dataStore.edit { preferences ->
            val json = preferences[PreferencesKeys.SAVED_CALCULATIONS_JSON] ?: "[]"
            val currentList = SavedCalculation.listFromJson(json).toMutableList()
            // Add at beginning
            currentList.add(0, calculation)
            // Limit to 50 items (oldest removed)
            val trimmedList = if (currentList.size > 50) currentList.take(50) else currentList
            preferences[PreferencesKeys.SAVED_CALCULATIONS_JSON] = SavedCalculation.listToJson(trimmedList)
        }
    }

    suspend fun deleteCalculation(id: String) {
        context.dataStore.edit { preferences ->
            val json = preferences[PreferencesKeys.SAVED_CALCULATIONS_JSON] ?: "[]"
            val currentList = SavedCalculation.listFromJson(json).toMutableList()
            currentList.removeAll { it.id == id }
            preferences[PreferencesKeys.SAVED_CALCULATIONS_JSON] = SavedCalculation.listToJson(currentList)
        }
    }

    suspend fun toggleFavorite(id: String) {
        context.dataStore.edit { preferences ->
            val json = preferences[PreferencesKeys.SAVED_CALCULATIONS_JSON] ?: "[]"
            val currentList = SavedCalculation.listFromJson(json).map { item ->
                if (item.id == id) {
                    item.copy(isFavorite = !item.isFavorite)
                } else {
                    item
                }
            }
            preferences[PreferencesKeys.SAVED_CALCULATIONS_JSON] = SavedCalculation.listToJson(currentList)
        }
    }

    suspend fun duplicateCalculation(id: String) {
        context.dataStore.edit { preferences ->
            val json = preferences[PreferencesKeys.SAVED_CALCULATIONS_JSON] ?: "[]"
            val currentList = SavedCalculation.listFromJson(json).toMutableList()
            val target = currentList.firstOrNull { it.id == id }
            if (target != null) {
                val duplicated = target.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    projectName = "Copy of ${target.projectName}",
                    timestampMillis = System.currentTimeMillis(),
                    isFavorite = false
                )
                currentList.add(0, duplicated)
                val trimmedList = if (currentList.size > 50) currentList.take(50) else currentList
                preferences[PreferencesKeys.SAVED_CALCULATIONS_JSON] = SavedCalculation.listToJson(trimmedList)
            }
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SAVED_CALCULATIONS_JSON] = "[]"
        }
    }
}
