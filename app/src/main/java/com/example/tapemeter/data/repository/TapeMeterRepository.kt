package com.example.tapemeter.data.repository

import com.example.tapemeter.data.datastore.PreferencesManager
import com.example.tapemeter.data.model.AppSettings
import com.example.tapemeter.data.model.SavedCalculation
import kotlinx.coroutines.flow.Flow

class TapeMeterRepository(private val preferencesManager: PreferencesManager) {

    val settings: Flow<AppSettings> = preferencesManager.settingsFlow
    val savedCalculations: Flow<List<SavedCalculation>> = preferencesManager.savedCalculationsFlow

    suspend fun updateSettings(settings: AppSettings) {
        preferencesManager.updateSettings(settings)
    }

    suspend fun resetSettings() {
        preferencesManager.resetSettings()
    }

    suspend fun saveCalculation(calculation: SavedCalculation) {
        preferencesManager.saveCalculation(calculation)
    }

    suspend fun deleteCalculation(id: String) {
        preferencesManager.deleteCalculation(id)
    }

    suspend fun toggleFavorite(id: String) {
        preferencesManager.toggleFavorite(id)
    }

    suspend fun duplicateCalculation(id: String) {
        preferencesManager.duplicateCalculation(id)
    }

    suspend fun clearHistory() {
        preferencesManager.clearHistory()
    }
}
