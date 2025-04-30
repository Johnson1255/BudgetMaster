package com.senlin.budgetmaster.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Define the DataStore instance at the top level
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserSettingsRepository(private val context: Context) {

    // Define the key for the language preference
    private object PreferencesKeys {
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
    }

    // Flow to observe the language preference
    val languagePreference: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LANGUAGE_CODE]
        }

    // Function to save the language preference
    suspend fun saveLanguagePreference(languageCode: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.LANGUAGE_CODE] = languageCode
        }
    }

    // Companion object to potentially hold constants or factory methods if needed later
    companion object {
        // Example: Default language if none is set
        // const val DEFAULT_LANGUAGE = "en"
    }
}
