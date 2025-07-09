package com.lee.timely.features.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.lee.timely.R
import com.lee.timely.db.TimelyDatabase
import com.lee.timely.model.GradeYear
import com.lee.timely.model.GroupName
import com.lee.timely.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.navigation.NavController
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.ui.graphics.RectangleShape
import com.lee.timely.ui.theme.PrimaryBlue
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SupportAgent
import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.compose.material.icons.filled.Email
import android.provider.Settings
import androidx.compose.material3.OutlinedTextField
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

fun getNowString(): String {
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
}

@Composable
fun getActivity(): Activity? {
    var context = LocalContext.current
    while (context is android.content.ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val currentLang = prefs.getString("lang", Locale.getDefault().language) ?: "en"
    var selectedLang by remember { mutableStateOf(currentLang) }
    var showRestartDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showExportSuccess by remember { mutableStateOf(false) }
    var showImportSuccess by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }

    val activity = getActivity()
    val gson = Gson()
    val timelyDao = TimelyDatabase.getInstance(context).getTimelyDao()

    // Data class for refined export structure
    data class ExportData(
        val version: Int = 1,
        val exportedAt: String = getNowString(),
        val schoolYears: List<GradeYear>,
        val groups: List<GroupName>,
        val users: List<User>
    )

    var showErrorDialog by remember { mutableStateOf<String?>(null) }
    var showVersionDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            (context as? ComponentActivity)?.lifecycleScope?.launch(Dispatchers.IO) {
                try {
                    val users = timelyDao.getAllUsers().firstOrNull() ?: emptyList()
                    val groups = timelyDao.getAllGroups().firstOrNull() ?: emptyList()
                    val years = timelyDao.getAllSchoolYears().firstOrNull() ?: emptyList()
                    val exportData = ExportData(
                        version = 1,
                        exportedAt = getNowString(),
                        schoolYears = years,
                        groups = groups,
                        users = users
                    )
                    val json = gson.toJson(exportData)
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        OutputStreamWriter(out).use { it.write(json) }
                    }
                } catch (e: Exception) {
                    showErrorDialog = e.localizedMessage ?: "Unknown error"
                }
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            (context as? ComponentActivity)?.lifecycleScope?.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        val reader = InputStreamReader(input)
                        val importData = gson.fromJson(reader, ExportData::class.java)
                        if (importData.version != 1) {
                            showVersionDialog = true
                            return@launch
                        }
                        timelyDao.deleteAllUsers()
                        timelyDao.deleteAllGroups()
                        timelyDao.deleteAllSchoolYears()
                        importData.schoolYears.forEach { timelyDao.insertSchoolYear(it) }
                        importData.groups.forEach { timelyDao.insertGroup(it) }
                        importData.users.forEach { timelyDao.insertUser(it) }
                    }
                } catch (e: Exception) {
                    showErrorDialog = e.localizedMessage ?: "Unknown error"
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Top bar
        TopAppBar(
            title = {
                Text(
                    stringResource(R.string.settings),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    navController?.popBackStack()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            }
        )
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedLang == "en",
                    onClick = {
                        if (selectedLang != "en") {
                            selectedLang = "en"
                            prefs.edit().putString("lang", "en").apply()
                            showRestartDialog = true
                        }
                    }
                )
                Text(stringResource(R.string.english), style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                RadioButton(
                    selected = selectedLang == "ar",
                    onClick = {
                        if (selectedLang != "ar") {
                            selectedLang = "ar"
                            prefs.edit().putString("lang", "ar").apply()
                            showRestartDialog = true
                        }
                    }
                )
                Text(stringResource(R.string.arabic), style = MaterialTheme.typography.bodyLarge)
            }
            Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray)
            Text(stringResource(R.string.data_management), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            FilledTonalButton(
                onClick = {
                    exportLauncher.launch("timely_export.json")
                    showExportSuccess = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp) // <-- Rounded corners with 8.dp radius
            ) {
                Icon(
                    Icons.Filled.FileUpload,
                    contentDescription = null,
                    tint = Color.White, // White icon
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = stringResource(R.string.export_data),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White // White text
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            FilledTonalButton(
                onClick = {
                    importLauncher.launch(arrayOf("application/json"))
                    showImportSuccess = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp) // <-- Rounded corners with 8.dp radius
            ) {
                Icon(
                    Icons.Filled.FileDownload,
                    contentDescription = null,
                    tint = Color.White, // Make icon white
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = stringResource(R.string.import_data),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White // Make text white
                )
            }
            Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray)
            Text(stringResource(R.string.reset_data), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            FilledTonalButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.padding(end = 8.dp), tint = MaterialTheme.colorScheme.error)
                Text(stringResource(R.string.reset_data), style = MaterialTheme.typography.titleMedium)
            }
            Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray)
            /*
            Text(stringResource(R.string.about), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            FilledTonalButton(
                onClick = { showAboutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp) // Rounded corners with 8.dp radius
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = Color.White, // Make icon white
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = stringResource(R.string.app_info),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White // Make text white
                )
            }
            Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray)*/
            Text(stringResource(R.string.contact_support), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            FilledTonalButton(
                onClick = { showContactDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.Filled.SupportAgent,
                    contentDescription = null,
                    tint = Color.White, // Make icon white
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = stringResource(R.string.contact_support),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White // Make text white
                )
            }
        }
        if (showRestartDialog) {
            AlertDialog(
                onDismissRequest = { showRestartDialog = false },
                title = { Text(stringResource(R.string.restart_required), style = MaterialTheme.typography.titleLarge) },
                text = { Text(stringResource(R.string.restart_to_apply_language), style = MaterialTheme.typography.bodyLarge) },
                confirmButton = {
                    TextButton(onClick = {
                        showRestartDialog = false
                        val pm = context.packageManager
                        val intent = pm.getLaunchIntentForPackage(context.packageName)
                        if (intent != null) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                            Runtime.getRuntime().exit(0)
                        }
                    }) {
                        Text(stringResource(R.string.restart_now), style = MaterialTheme.typography.titleMedium)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestartDialog = false }) {
                        Text(stringResource(R.string.later), style = MaterialTheme.typography.titleMedium)
                    }
                }
            )
        }
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text(stringResource(R.string.reset_data), style = MaterialTheme.typography.titleLarge) },
                text = { Text(stringResource(R.string.reset_data_confirm), style = MaterialTheme.typography.bodyLarge) },
                confirmButton = {
                    TextButton(onClick = {
                        resetData(context)
                        showResetDialog = false
                    }) {
                        Text(stringResource(R.string.yes), style = MaterialTheme.typography.titleMedium)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text(stringResource(R.string.no), style = MaterialTheme.typography.titleMedium)
                    }
                }
            )
        }
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text(stringResource(R.string.about), style = MaterialTheme.typography.titleLarge) },
                text = { Text(stringResource(R.string.app_info_full), style = MaterialTheme.typography.bodyLarge) },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text(stringResource(R.string.ok), style = MaterialTheme.typography.titleMedium)
                    }
                },
                dismissButton = {}
            )
        }
        if (showExportSuccess) {
            AlertDialog(
                onDismissRequest = { showExportSuccess = false },
                title = { Text(stringResource(R.string.export_success), style = MaterialTheme.typography.titleLarge) },
                text = { Text(stringResource(R.string.export_success_message), style = MaterialTheme.typography.bodyLarge) },
                confirmButton = {
                    TextButton(onClick = { showExportSuccess = false }) {
                        Text(stringResource(R.string.ok), style = MaterialTheme.typography.titleMedium)
                    }
                },
                dismissButton = {}
            )
        }
        if (showImportSuccess) {
            AlertDialog(
                onDismissRequest = { showImportSuccess = false },
                title = { Text(stringResource(R.string.import_success), style = MaterialTheme.typography.titleLarge) },
                text = { Text(stringResource(R.string.import_success_message), style = MaterialTheme.typography.bodyLarge) },
                confirmButton = {
                    TextButton(onClick = { showImportSuccess = false }) {
                        Text(stringResource(R.string.ok), style = MaterialTheme.typography.titleMedium)
                    }
                },
                dismissButton = {}
            )
        }
        if (showErrorDialog != null) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = null },
                title = { Text(stringResource(R.string.error), style = MaterialTheme.typography.titleLarge) },
                text = { Text(showErrorDialog ?: "", style = MaterialTheme.typography.bodyLarge) },
                confirmButton = {
                    TextButton(onClick = { showErrorDialog = null }) {
                        Text(stringResource(R.string.ok), style = MaterialTheme.typography.titleMedium)
                    }
                },
                dismissButton = {}
            )
        }
        if (showVersionDialog) {
            AlertDialog(
                onDismissRequest = { showVersionDialog = false },
                title = { Text(stringResource(R.string.error), style = MaterialTheme.typography.titleLarge) },
                text = { Text(stringResource(R.string.import_version_mismatch), style = MaterialTheme.typography.bodyLarge) },
                confirmButton = {
                    TextButton(onClick = { showVersionDialog = false }) {
                        Text(stringResource(R.string.ok), style = MaterialTheme.typography.titleMedium)
                    }
                },
                dismissButton = {}
            )
        }
        if (showContactDialog) {
            AlertDialog(
                onDismissRequest = { showContactDialog = false },
                title = { Text(stringResource(R.string.contact_support), style = MaterialTheme.typography.titleLarge) },
                text = {
                    Column {
                        FilledTonalButton(
                            onClick = {
                                showContactDialog = false
                                val number = "+201007394856"
                                val url = "https://wa.me/$number"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                try {
                                    context.startActivity(intent)
                                } catch (e: ActivityNotFoundException) {
                                    Toast.makeText(context, "WhatsApp not found", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = Color.White, modifier = Modifier.padding(end = 8.dp))
                            Text(stringResource(R.string.open_whatsapp), color = Color.White)
                        }
                        FilledTonalButton(
                            onClick = {
                                showContactDialog = false
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:eng.ahmedkhaled.work@gmail.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "Timely App Support")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: ActivityNotFoundException) {
                                    Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF4285F4)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.Email, contentDescription = null, tint = Color.White, modifier = Modifier.padding(end = 8.dp))
                            Text(stringResource(R.string.send_email), color = Color.White)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showContactDialog = false }) {
                        Text(stringResource(R.string.ok), style = MaterialTheme.typography.titleMedium)
                    }
                },
                dismissButton = {}
            )
        }
    }
}

fun resetData(context: Context) {
    val timelyDao = TimelyDatabase.getInstance(context).getTimelyDao()
    (context as? ComponentActivity)?.lifecycleScope?.launch(Dispatchers.IO) {
        timelyDao.deleteAllUsers()
        timelyDao.deleteAllGroups()
        timelyDao.deleteAllSchoolYears()
    }
}

fun contactSupport(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:support@example.com")
        putExtra(Intent.EXTRA_SUBJECT, "Timely App Support")
    }
    startActivity(context, intent, null)
} 