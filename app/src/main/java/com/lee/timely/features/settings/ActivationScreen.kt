package com.lee.timely.features.settings

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.lee.timely.R
import com.lee.timely.ui.theme.PrimaryBlue
import org.json.JSONArray

@Composable
fun ActivationScreen(onActivated: () -> Unit) {
    val context = LocalContext.current
    var keyInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var validKeys by remember { mutableStateOf<List<String>>(emptyList()) }

    // Load keys from res/raw/keys.json
    LaunchedEffect(Unit) {
        try {
            val inputStream = context.resources.openRawResource(R.raw.keys)
            val json = inputStream.bufferedReader().use { it.readText() }
            val arr = JSONArray(json)
            validKeys = List(arr.length()) { arr.getString(it) }
        } catch (e: Exception) {
            errorMessage = context.getString(R.string.invalid_license_key)
        }
    }

    val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    val prefs = EncryptedSharedPreferences.create(
        "secure_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.enter_license_key), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = keyInput,
            onValueChange = { keyInput = it },
            label = { Text(stringResource(R.string.license_key)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        FilledTonalButton(
            onClick = {
                val trimmedKey = keyInput.trim().uppercase()
                val storedKey = prefs.getString("license_key", null)
                val storedId = prefs.getString("device_id", null)
                if (storedKey != null && storedId != null && storedKey == trimmedKey && storedId == androidId) {
                    onActivated()
                } else if (validKeys.contains(trimmedKey)) {
                    prefs.edit().putString("license_key", trimmedKey).putString("device_id", androidId).apply()
                    onActivated()
                } else {
                    errorMessage = context.getString(R.string.invalid_license_key)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = validKeys.isNotEmpty(),
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                stringResource(R.string.activate),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White // Make text white
            )
        }
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
    }
} 