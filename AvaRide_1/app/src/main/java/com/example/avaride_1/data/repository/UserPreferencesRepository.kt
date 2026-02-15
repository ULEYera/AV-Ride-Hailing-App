package com.example.avaride_1.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {
    private val PHONE_NUMBER_KEY = stringPreferencesKey("phone_number")

    val userPhoneNumber: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PHONE_NUMBER_KEY]
        }

    suspend fun saveUser(phoneNumber: String) {
        context.dataStore.edit { preferences ->
            preferences[PHONE_NUMBER_KEY] = phoneNumber
        }
    }

    suspend fun clearUser() {
        println("UserPrefs: clearUser called")
        context.dataStore.edit { preferences ->
            preferences.remove(PHONE_NUMBER_KEY)
        }
        println("UserPrefs: clearUser completed")
    }
}
