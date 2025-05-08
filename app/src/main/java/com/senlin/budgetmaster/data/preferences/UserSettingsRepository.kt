package com.senlin.budgetmaster.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
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
        val CURRENT_USER_ID = longPreferencesKey("current_user_id")
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

    // Flow to observe the current user ID
    val currentUserId: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.CURRENT_USER_ID]
        }

    // Function to save the current user ID
    suspend fun saveCurrentUserId(userId: Long) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.CURRENT_USER_ID] = userId
        }
    }

    // Function to clear the current user ID (logout)
    suspend fun clearCurrentUserId() {
        context.dataStore.edit { settings ->
            settings.remove(PreferencesKeys.CURRENT_USER_ID)
        }
    }

    // Companion object to potentially hold constants or factory methods if needed later
    companion object {
        // Example: Default language if none is set
        // const val DEFAULT_LANGUAGE = "en"
    }
}
