package com.lee.timely.util

import android.content.Context
import android.provider.Settings
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

class EnhancedLicenseManager(private val context: Context) {
    private val prefs by lazy {
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "secure_prefs",
            masterKey,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val deviceId: String by lazy {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown_device"
    }

    suspend fun checkActivationStatus(): ActivationStatus = withContext(Dispatchers.IO) {
        val key = prefs.getString("license_key", null)
        if (key.isNullOrBlank()) return@withContext ActivationStatus.NotActivated
        return@withContext try {
            val doc = db.collection("activationKeys").document(key.trim().uppercase()).get().await()
            val used = doc.getBoolean("used") ?: false
            val storedDevice = doc.getString("device")
            val isActive = doc.getBoolean("isActive") ?: true
            if (isActive && used && storedDevice == deviceId) ActivationStatus.Activated else ActivationStatus.NotActivated
        } catch (e: Exception) {
            ActivationStatus.NotActivated
        }
    }

    fun getActivationInfo(): String {
        val key = prefs.getString("license_key", null)
        return if (!key.isNullOrBlank()) "Activated with key: $key" else "Not activated."
    }

    fun getDeviceInfo(): String {
        return "Device ID: $deviceId\nModel: ${android.os.Build.MODEL ?: "Unknown Device"}"
    }

    suspend fun forceRefreshActivationStatus(): ActivationStatus = checkActivationStatus()

    suspend fun deactivateKey(): Boolean = withContext(Dispatchers.IO) {
        val key = prefs.getString("license_key", null)
        if (!key.isNullOrBlank()) {
            try {
                val docRef = db.collection("activationKeys").document(key.trim().uppercase())
                docRef.update(mapOf("used" to false, "device" to null)).await()
            } catch (_: Exception) {}
        }
        prefs.edit().remove("license_key").apply()
        true
    }

    suspend fun activateKey(licenseKey: String): ActivationResult = withContext(Dispatchers.IO) {
        val docRef = db.collection("activationKeys").document(licenseKey.trim().uppercase())
        try {
            val doc = docRef.get().await()
            if (!doc.exists()) {
                ActivationResult.Error("Key does not exist")
            } else {
                val used = doc.getBoolean("used") ?: false
                val storedDevice = doc.getString("device")
                val isActive = doc.getBoolean("isActive") ?: true
                if (!isActive) {
                    ActivationResult.Error("Key is not active")
                } else if (!used) {
                    // Mark as used by this device
                    docRef.update(mapOf("used" to true, "device" to deviceId)).await()
                    prefs.edit().putString("license_key", licenseKey.trim()).apply()
                    ActivationResult.Success("Activation successful!")
                } else if (storedDevice == deviceId) {
                    // Already activated on this device
                    prefs.edit().putString("license_key", licenseKey.trim()).apply()
                    ActivationResult.Success("Already activated on this device")
                } else {
                    ActivationResult.Error("Key already used on another device")
                }
            }
        } catch (e: Exception) {
            ActivationResult.Error("Activation failed: ${e.message}")
        }
    }

    suspend fun shouldShowActivationScreen(): Boolean = withContext(Dispatchers.IO) {
        val key = prefs.getString("license_key", null)
        if (key.isNullOrBlank()) return@withContext true
        return@withContext try {
            val doc = db.collection("activationKeys").document(key.trim().uppercase()).get().await()
            val used = doc.getBoolean("used") ?: false
            val storedDevice = doc.getString("device")
            val isActive = doc.getBoolean("isActive") ?: true
            !(isActive && used && storedDevice == deviceId)
        } catch (e: Exception) {
            true
        }
    }

    suspend fun performLicenseValidation(): LicenseValidationResult = withContext(Dispatchers.IO) {
        val key = prefs.getString("license_key", null)
        if (key.isNullOrBlank()) return@withContext LicenseValidationResult.Invalid("No key stored")
        return@withContext try {
            val doc = db.collection("activationKeys").document(key.trim().uppercase()).get().await()
            val used = doc.getBoolean("used") ?: false
            val storedDevice = doc.getString("device")
            val isActive = doc.getBoolean("isActive") ?: true
            if (isActive && used && storedDevice == deviceId) {
                LicenseValidationResult.Valid
            } else {
                LicenseValidationResult.Invalid("Key not valid for this device")
            }
        } catch (e: Exception) {
            LicenseValidationResult.Invalid("Validation failed: ${e.message}")
        }
    }
} 