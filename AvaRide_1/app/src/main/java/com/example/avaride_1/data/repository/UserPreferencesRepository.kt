package com.example.avaride_1.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Stores user PII using EncryptedSharedPreferences (AES256-GCM / AES256-SIV).
 * The backing file is excluded from cloud and device backups (see backup_rules.xml).
 *
 * PB-11: Local data encryption requirement.
 */
class UserPreferencesRepository(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val _phoneNumberFlow = MutableStateFlow<String?>(null)

    /**
     * Emits the current phone number and any future changes made via [saveUser] / [clearUser].
     * Backed by EncryptedSharedPreferences — the value on disk is always encrypted.
     */
    val userPhoneNumber: Flow<String?> = _phoneNumberFlow.asStateFlow()

    init {
        // Seed the in-memory flow from encrypted storage.
        // EncryptedSharedPreferences.create() accesses the Android Keystore on first call;
        // subsequent reads are fast after the key is loaded.
        _phoneNumberFlow.value = prefs.getString(KEY_PHONE_NUMBER, null)

        // Remove any legacy plaintext DataStore file that may exist from a previous build.
        deleteLegacyPlaintextStore()
    }

    suspend fun saveUser(phoneNumber: String) {
        withContext(Dispatchers.IO) {
            prefs.edit().putString(KEY_PHONE_NUMBER, phoneNumber).apply()
        }
        _phoneNumberFlow.value = phoneNumber
    }

    suspend fun clearUser() {
        withContext(Dispatchers.IO) {
            prefs.edit().remove(KEY_PHONE_NUMBER).apply()
        }
        _phoneNumberFlow.value = null
    }

    /**
     * Deletes the unencrypted DataStore file written by the previous implementation.
     * Safe to call on every init — no-ops silently if the file does not exist.
     */
    private fun deleteLegacyPlaintextStore() {
        try {
            val dsDir = File(context.filesDir, "datastore")
            dsDir.listFiles()
                ?.filter { it.name.startsWith("user_prefs") }
                ?.forEach { it.delete() }
        } catch (_: Exception) {
            // Non-fatal: if deletion fails the old file is simply left on disk
            // but the app will never write to it again.
        }
    }

    companion object {
        private const val PREFS_FILE_NAME = "user_secure_prefs"
        private const val KEY_PHONE_NUMBER = "phone_number"
    }
}
