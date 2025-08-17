package com.lee.timely.features.group.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lee.timely.model.User
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.unit.times
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.times
import androidx.compose.ui.unit.times
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.res.stringResource
import com.lee.timely.R
import com.lee.timely.ui.theme.LighterSecondaryBlue
import com.lee.timely.ui.theme.PrimaryBlue
import com.lee.timely.ui.theme.SecondaryBlue
import java.util.Locale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.material.icons.filled.Whatsapp
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import com.lee.timely.animation.withWinkRoughFont

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StudentProfileScreen(
    user: User,
    onBack: () -> Unit,
    onMonthPaid: (Int) -> Unit,
    onCallNumber: (String) -> Unit = {},
    onEditUser: (User) -> Unit,
    onDeleteUser: (User) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var paidFlags by remember {
        mutableStateOf(
            listOf(
                user.flag1, user.flag2, user.flag3, user.flag4, user.flag5, user.flag6,
                user.flag7, user.flag8, user.flag9, user.flag10, user.flag11, user.flag12
            )
        )
    }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedMonth by remember { mutableStateOf<Int?>(null) }
    var showCallDialog by remember { mutableStateOf(false) }
    var numberToCall by remember { mutableStateOf("") }
    var showCopyToast by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showWhatsappError by remember { mutableStateOf(false) }

    // Helper function to open WhatsApp
    fun openWhatsApp(context: android.content.Context, number: String, onError: () -> Unit) {
        val waNumber = if (number.startsWith("+2")) number else "+2$number"
        val cleanNumber = waNumber.replace("[^\\d+]".toRegex(), "")
        val url = "https://wa.me/$cleanNumber"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        intent.setPackage("com.whatsapp")
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            onError()
        }
    }

    // Check if the current language is Arabic
    val isArabic = Locale.getDefault().language == "ar"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.student_profile_screen_title),
                        style = MaterialTheme.typography.titleLarge.withWinkRoughFont()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = if (isArabic) Icons.Filled.ArrowForward else Icons.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEditUser(user) }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(R.string.student_profile_screen_edit))
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        snackbarHost = {
            if (showCopyToast) {
                Snackbar(
                    modifier = Modifier.padding(8.dp),
                    action = {
                        TextButton(onClick = { showCopyToast = false }) {
                            Text(
                                stringResource(R.string.ok),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium.withWinkRoughFont()
                            )
                        }
                    },
                    containerColor = PrimaryBlue // Example: Green background
                ) { 
                    Text(
                        text = stringResource(R.string.no_phone_number),
                        style = MaterialTheme.typography.bodyMedium
                            .withWinkRoughFont()
                            .copy(color = Color.Gray)
                    ) 
                }
            }
            if (showWhatsappError) {
                Snackbar(
                    modifier = Modifier.padding(8.dp),
                    action = {
                        TextButton(onClick = { showWhatsappError = false }) {
                            Text(
                            stringResource(R.string.ok), 
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.withWinkRoughFont()
                        )
                        }
                    },
                    containerColor = Color(0xFF25D366) // WhatsApp green
                ) { 
                    Text(
                        stringResource(R.string.open_whatsapp) + ": " + stringResource(R.string.error),
                        style = MaterialTheme.typography.bodyMedium.withWinkRoughFont()
                    ) 
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = stringResource(R.string.name_label, user.firstName ?: "", user.lastName ?: ""), 
                style = MaterialTheme.typography.titleMedium
                    .withWinkRoughFont()
                    .copy(fontSize = 20.sp, fontWeight = FontWeight.Bold)
            )

            // UID
            Text(
                text = stringResource(R.string.uid_text, user.uid.toString()),
                style = MaterialTheme.typography.bodySmall
                    .withWinkRoughFont()
                    .copy(fontSize = 15.sp),
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.guardians_number_label_full, user.guardiansNumber ?: ""),
                    style = MaterialTheme.typography.bodyMedium
                        .withWinkRoughFont()
                        .copy(fontSize = 16.sp),
                    color = Color(0xFF1976D2),
                    modifier = Modifier.weight(1f).combinedClickable(
                        enabled = !user.guardiansNumber.isNullOrBlank(),
                        onClick = {},
                        onLongClick = {
                            user.guardiansNumber?.let {
                                clipboardManager.setText(AnnotatedString(it))
                                showCopyToast = true
                            }
                        }
                    )
                )
                if (!user.guardiansNumber.isNullOrBlank()) {
                    IconButton(onClick = {
                        val rawNumber = user.guardiansNumber.trim().replace(" ", "")
                        openWhatsApp(context, rawNumber) {
                            showWhatsappError = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Whatsapp, // Replace with WhatsApp icon
                            contentDescription = "WhatsApp",
                            tint = Color(0xFF25D366)
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.student_number_label_full, user.studentNumber ?: ""),
                    style = MaterialTheme.typography.bodyMedium
                        .withWinkRoughFont()
                        .copy(fontSize = 16.sp),
                    color = Color(0xFF1976D2),
                    modifier = Modifier.weight(1f).combinedClickable(
                        enabled = !user.studentNumber.isNullOrBlank(),
                        onClick = {},
                        onLongClick = {
                            user.studentNumber?.let {
                                clipboardManager.setText(AnnotatedString(it))
                                showCopyToast = true
                            }
                        }
                    )
                )
                if (!user.studentNumber.isNullOrBlank()) {
                    IconButton(onClick = {
                        val rawNumber = user.studentNumber.trim().replace(" ", "")
                        openWhatsApp(context, rawNumber) {
                            showWhatsappError = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Whatsapp, // Replace with WhatsApp icon
                            contentDescription = "WhatsApp",
                            tint = Color(0xFF25D366)
                        )
                    }
                }
            }
            // Start Date: revert to default (label and value together)
            Text(
                text = stringResource(R.string.start_date_label_full, user.startDate ?: ""),
                style = MaterialTheme.typography.bodyMedium
                    .withWinkRoughFont()
                    .copy(fontSize = 16.sp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.monthly_payment_status),
                style = MaterialTheme.typography.titleMedium
                    .withWinkRoughFont()
                    .copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val monthNames = listOf(
                stringResource(R.string.month_jan),
                stringResource(R.string.month_feb),
                stringResource(R.string.month_mar),
                stringResource(R.string.month_apr),
                stringResource(R.string.month_may),
                stringResource(R.string.month_jun),
                stringResource(R.string.month_jul),
                stringResource(R.string.month_aug),
                stringResource(R.string.month_sep),
                stringResource(R.string.month_oct),
                stringResource(R.string.month_nov),
                stringResource(R.string.month_dec)
            )
            val months = (1..12).toList()
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(6 * 60.dp),
                userScrollEnabled = false
            ) {
                items(months) { month ->
                    val paid = paidFlags[month - 1]
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (paid) Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier
                            .padding(4.dp)
                            .size(80.dp, 48.dp)
                            .clickable {
                                if (!paid) {
                                    selectedMonth = month
                                    showConfirmDialog = true
                                }
                            },
                        shadowElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$month ${monthNames[month-1]}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall
                                        .withWinkRoughFont()
                                        .copy(fontSize = 14.sp)
                                )
                                Text(
                                    text = if (paid) stringResource(R.string.paid) else stringResource(R.string.not_paid),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall
                                        .withWinkRoughFont()
                                        .copy(fontSize = 10.sp)
                                )
                            }
                        }
                    }
                }
            }
            if (showConfirmDialog && selectedMonth != null) {
                AlertDialog(
                    onDismissRequest = { showConfirmDialog = false },
                    title = { 
                        Text(
                            stringResource(R.string.confirm_payment_title),
                            style = MaterialTheme.typography.titleLarge.withWinkRoughFont()
                        )
                    },
                    text = { 
                        Text(
                            stringResource(R.string.confirm_payment_message, monthNames[selectedMonth!! - 1]),
                            style = MaterialTheme.typography.bodyLarge.withWinkRoughFont()
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            paidFlags = paidFlags.toMutableList().also { it[selectedMonth!! - 1] = true }
                            onMonthPaid(selectedMonth!!)
                            showConfirmDialog = false
                        }) {
                            Text(
                                stringResource(R.string.yes),
                                style = MaterialTheme.typography.titleMedium.withWinkRoughFont()
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmDialog = false }) {
                            Text(
                                stringResource(R.string.no),
                                style = MaterialTheme.typography.titleMedium.withWinkRoughFont()
                            )
                        }
                    }
                )
            }
            if (showCallDialog && numberToCall.isNotBlank()) {
                AlertDialog(
                    onDismissRequest = { showCallDialog = false },
                    title = { 
                        Text(
                            stringResource(R.string.call_number_title),
                            style = MaterialTheme.typography.titleLarge.withWinkRoughFont()
                        )
                    },
                    text = { 
                        Text(
                            stringResource(R.string.call_number_message, numberToCall),
                            style = MaterialTheme.typography.bodyLarge.withWinkRoughFont()
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            onCallNumber(numberToCall)
                            showCallDialog = false
                        }) {
                            Text(
                                stringResource(R.string.yes),
                                style = MaterialTheme.typography.titleMedium.withWinkRoughFont()
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCallDialog = false }) {
                            Text(
                                stringResource(R.string.no),
                                style = MaterialTheme.typography.titleMedium.withWinkRoughFont()
                            )
                        }
                    }
                )
            }
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { 
                        Text(
                            stringResource(R.string.delete),
                            style = MaterialTheme.typography.titleLarge.withWinkRoughFont()
                        )
                    },
                    text = { 
                        Text(
                            stringResource(R.string.delete_confirmation, user.firstName ?: ""),
                            style = MaterialTheme.typography.bodyLarge.withWinkRoughFont()
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            onDeleteUser(user)
                            // The navigation will handle the refresh through the navigation graph
                            showDeleteDialog = false
                        }) {
                            Text(
                                stringResource(R.string.yes),
                                style = MaterialTheme.typography.titleMedium.withWinkRoughFont()
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text(
                                stringResource(R.string.no),
                                style = MaterialTheme.typography.titleMedium.withWinkRoughFont()
                            )
                        }
                    }
                )
            }
        }
    }
} 