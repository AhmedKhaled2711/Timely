package com.lee.timely.features.settings

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.google.gson.Gson
import com.lee.timely.R
import com.lee.timely.db.TimelyDatabase
import com.lee.timely.domain.AcademicYearPayment
import com.lee.timely.domain.GradeYear
import com.lee.timely.domain.GroupName
import com.lee.timely.domain.User
import com.lee.timely.ui.theme.PrimaryBlue
import com.lee.timely.util.AcademicYearUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    var showContactDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf<String?>(null) }
    var showVersionDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val activity = getActivity()
    val gson = Gson()
    val timelyDao = TimelyDatabase.getInstance(context).getTimelyDao()

    // Data class for old export structure (for backward compatibility)
    data class OldUser(
        val uid: Int = 0,
        val firstName: String,
        val lastName: String,
        val groupId: Int,
        val flag1: Boolean = false,
        val flag2: Boolean = false,
        val flag3: Boolean = false,
        val flag4: Boolean = false,
        val flag5: Boolean = false,
        val flag6: Boolean = false,
        val flag7: Boolean = false,
        val flag8: Boolean = false,
        val flag9: Boolean = false,
        val flag10: Boolean = false,
        val flag11: Boolean = false,
        val flag12: Boolean = false,
        val guardiansNumber: String? = null,
        val startDate: String,
        val studentNumber: String? = null
    )

    // Data class for old export structure (for backward compatibility)
    data class OldExportData(
        val version: Int = 1,
        val exportedAt: String,
        val schoolYears: List<GradeYear>,
        val groups: List<GroupName>,
        val users: List<OldUser>
    )

    // Data class for refined export structure
    data class ExportData(
        val version: Int = 2,
        val exportedAt: String = getNowString(),
        val schoolYears: List<GradeYear>,
        val groups: List<GroupName>,
        val users: List<User>
    )

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            (context as? ComponentActivity)?.lifecycleScope?.launch(Dispatchers.IO) {
                try {
                    val users = timelyDao.getAllUsers().firstOrNull() ?: emptyList()
                    val groups = timelyDao.getAllGroups().firstOrNull() ?: emptyList()
                    val years = timelyDao.getAllSchoolYears().firstOrNull() ?: emptyList()
                    val exportData = ExportData(
                        version = 2,
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
                        val json = reader.readText()

                        // Try to parse as new format first
                        try {
                            val importData = gson.fromJson(json, ExportData::class.java)
                            when (importData.version) {
                                2 -> {
                                    // New format with allName
                                    timelyDao.deleteAllUsers()
                                    timelyDao.deleteAllGroups()
                                    timelyDao.deleteAllSchoolYears()
                                    importData.schoolYears.forEach { timelyDao.insertSchoolYear(it) }
                                    importData.groups.forEach { timelyDao.insertGroup(it) }
                                    importData.users.forEach { timelyDao.insertUser(it) }
                                }
                                1 -> {
                                    // Old format - convert from firstName/lastName to allName
                                    val oldData = gson.fromJson(json, OldExportData::class.java)
                                    timelyDao.deleteAllUsers()
                                    timelyDao.deleteAllGroups()
                                    timelyDao.deleteAllSchoolYears()
                                    oldData.schoolYears.forEach { timelyDao.insertSchoolYear(it) }
                                    oldData.groups.forEach { timelyDao.insertGroup(it) }
                                    oldData.users.forEach { oldUser ->
                                        val newUser = User(
                                            uid = oldUser.uid,
                                            allName = "${oldUser.firstName} ${oldUser.lastName}".trim(),
                                            groupId = oldUser.groupId,
                                            guardiansNumber = oldUser.guardiansNumber,
                                            startDate = oldUser.startDate,
                                            studentNumber = oldUser.studentNumber
                                        )
                                        timelyDao.insertUser(newUser)
                                        
                                        // Migrate payment data to academic year payments
                                        val currentAcademicYear = AcademicYearUtils.getCurrentAcademicYear()
                                        val academicYearMonths = AcademicYearUtils.getAcademicYearMonths(currentAcademicYear)
                                        
                                        academicYearMonths.forEach { (month, year) ->
                                            val isPaid = when (month) {
                                                1 -> oldUser.flag1
                                                2 -> oldUser.flag2
                                                3 -> oldUser.flag3
                                                4 -> oldUser.flag4
                                                5 -> oldUser.flag5
                                                6 -> oldUser.flag6
                                                7 -> oldUser.flag7
                                                8 -> oldUser.flag8
                                                9 -> oldUser.flag9
                                                10 -> oldUser.flag10
                                                11 -> oldUser.flag11
                                                12 -> oldUser.flag12
                                                else -> false
                                            }
                                            
                                            if (isPaid) {
                                                val payment = AcademicYearPayment(
                                                    userId = newUser.uid,
                                                    academicYear = currentAcademicYear,
                                                    month = month,
                                                    year = year,
                                                    isPaid = true
                                                )
                                                timelyDao.insertPayment(payment)
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    showVersionDialog = true
                                    return@launch
                                }
                            }
                        } catch (e: Exception) {
                            // If new format fails, try old format
                            try {
                                val oldData = gson.fromJson(json, OldExportData::class.java)
                                timelyDao.deleteAllUsers()
                                timelyDao.deleteAllGroups()
                                timelyDao.deleteAllSchoolYears()
                                oldData.schoolYears.forEach { timelyDao.insertSchoolYear(it) }
                                oldData.groups.forEach { timelyDao.insertGroup(it) }
                                oldData.users.forEach { oldUser ->
                                    val newUser = User(
                                        uid = oldUser.uid,
                                        allName = "${oldUser.firstName} ${oldUser.lastName}".trim(),
                                        groupId = oldUser.groupId,
                                        guardiansNumber = oldUser.guardiansNumber,
                                        startDate = oldUser.startDate,
                                        studentNumber = oldUser.studentNumber
                                    )
                                    timelyDao.insertUser(newUser)
                                    
                                    // Migrate payment data to academic year payments
                                    val currentAcademicYear = AcademicYearUtils.getCurrentAcademicYear()
                                    val academicYearMonths = AcademicYearUtils.getAcademicYearMonths(currentAcademicYear)
                                    
                                    academicYearMonths.forEach { (month, year) ->
                                        val isPaid = when (month) {
                                            1 -> oldUser.flag1
                                            2 -> oldUser.flag2
                                            3 -> oldUser.flag3
                                            4 -> oldUser.flag4
                                            5 -> oldUser.flag5
                                            6 -> oldUser.flag6
                                            7 -> oldUser.flag7
                                            8 -> oldUser.flag8
                                            9 -> oldUser.flag9
                                            10 -> oldUser.flag10
                                            11 -> oldUser.flag11
                                            12 -> oldUser.flag12
                                            else -> false
                                        }
                                        
                                        if (isPaid) {
                                            val payment = AcademicYearPayment(
                                                userId = newUser.uid,
                                                academicYear = currentAcademicYear,
                                                month = month,
                                                year = year,
                                                isPaid = true
                                            )
                                            timelyDao.insertPayment(payment)
                                        }
                                    }
                                }
                            } catch (e2: Exception) {
                                showErrorDialog = "Invalid file format: ${e.localizedMessage ?: e2.localizedMessage}"
                                return@launch
                            }
                        }
                    }
                } catch (e: Exception) {
                    showErrorDialog = e.localizedMessage ?: "Unknown error"
                }
                // Show success message after import is completed
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.import_success))
                }
            }
        }
    }

    Scaffold(
        topBar = {
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            item {
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
            }
            item {
                Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray)
                Text(stringResource(R.string.data_management), style = MaterialTheme.typography.titleMedium)
            }
            item {
                FilledTonalButton(
                    onClick = {
                        exportLauncher.launch("timely_export.json")
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.export_success))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Filled.FileUpload,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.export_data),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
            item {
                FilledTonalButton(
                    onClick = {
                        importLauncher.launch(arrayOf("application/json"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Filled.FileDownload,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.import_data),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
            item {
                Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray)
                Text(stringResource(R.string.reset_data), style = MaterialTheme.typography.titleMedium)
            }
            item {
                FilledTonalButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.padding(end = 8.dp), tint = MaterialTheme.colorScheme.error)
                    Text(stringResource(R.string.reset_data), style = MaterialTheme.typography.titleMedium)
                }
            }
            item {
                Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray)
                Text(stringResource(R.string.contact_support), style = MaterialTheme.typography.titleMedium)
            }
            item {
                FilledTonalButton(
                    onClick = { showContactDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Filled.SupportAgent,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.contact_support),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
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
