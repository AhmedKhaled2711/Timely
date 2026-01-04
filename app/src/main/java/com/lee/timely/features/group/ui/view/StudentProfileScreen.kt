package com.lee.timely.features.group.ui.view

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.navigation.NavController
import com.lee.timely.R
import com.lee.timely.animation.withWinkRoughFont
import com.lee.timely.domain.AcademicYearPayment
import com.lee.timely.domain.GroupName
import com.lee.timely.domain.User
import com.lee.timely.features.home.ui.state.TransferUserUiEvent
import com.lee.timely.features.home.ui.state.TransferUserUiState
import com.lee.timely.features.home.viewmodel.viewModel.MainViewModel
import com.lee.timely.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StudentProfileScreen(
    user: User,
    onBack: () -> Unit,
    onMonthPaid: (Int, Boolean) -> Unit,
    onCallNumber: (String) -> Unit = {},
    onEditUser: (User) -> Unit,
    onDeleteUser: (User) -> Unit,
    navController: NavController,
    userPayments: List<AcademicYearPayment> = emptyList(),
    viewModel: MainViewModel
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    
    // Get payment status from academic year payments
    val currentAcademicYear = com.lee.timely.util.AcademicYearUtils.getCurrentAcademicYear()
    Log.d("StudentProfile", "Current academic year: $currentAcademicYear")
    Log.d("StudentProfile", "User payments count: ${userPayments.size}")
    val paidFlags = remember(userPayments, currentAcademicYear) {
        (1..12).map { month ->
            val isPaid = userPayments.find { 
                it.academicYear == currentAcademicYear && it.month == month 
            }?.isPaid ?: false
            Log.d("StudentProfile", "Month $month paid: $isPaid")
            isPaid
        }
    }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var selectedMonth by remember { mutableStateOf<Int?>(null) }
    var showCallDialog by remember { mutableStateOf(false) }
    val numberToCall by remember { mutableStateOf("") }
    var showCopyToast by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showWhatsappError by remember { mutableStateOf(false) }
    var showTransferBottomSheet by remember { mutableStateOf(false) }
    var availableGroups by remember { mutableStateOf<List<GroupName>>(emptyList()) }

    // Transfer state
    val transferUserUiState by viewModel.transferUserUiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

    // Handle transfer events
    LaunchedEffect(Unit) {
        viewModel.transferUserEvent.collect { event ->
            when (event) {
                is TransferUserUiEvent.ShowSnackbar -> {
                    // Handle snackbar (you can add snackbar host state if needed)
                    Log.d("Transfer", "ShowSnackbar: ${event.message}")
                }
                is TransferUserUiEvent.NavigateBack -> {
                    // Set refresh flag before navigating back after successful transfer
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(100) // Small delay to ensure database operation completes
                        // Set refresh flag for parent screen
                        navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                        // Navigate back
                        onBack()
                    }
                }
                is TransferUserUiEvent.None -> { /* No action needed */ }
            }
        }
    }

    // Load available groups when transfer bottom sheet is shown
    LaunchedEffect(showTransferBottomSheet) {
        if (showTransferBottomSheet) {
            Log.d("Transfer", "Loading groups for transfer. Current user groupId: ${user.groupId}")
            
            // Get the school year ID from the user's current group
            viewModel.getSchoolYearIdForGroup(user.groupId).collect { schoolYearId ->
                if (schoolYearId != null) {
                    Log.d("Transfer", "Found school year ID: $schoolYearId for user's group")
                    
                    // Load all groups in the same school year
                    viewModel.getGroupsForYear(schoolYearId).collect { groups ->
                        val filteredGroups = groups.filter { it.id != user.groupId }
                        Log.d("Transfer", "Loaded ${groups.size} groups in school year $schoolYearId, filtered to ${filteredGroups.size} available groups")
                        availableGroups = filteredGroups
                    }
                } else {
                    Log.e("Transfer", "Could not find school year ID for group ${user.groupId}")
                    availableGroups = emptyList()
                }
            }
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
                            imageVector = if (isArabic) Icons.AutoMirrored.Filled.ArrowForward else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEditUser(user) }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(R.string.student_profile_screen_edit) , tint = PrimaryBlue)
                    }

                    IconButton(
                    onClick = { 
                        showTransferBottomSheet = true
                    },
                    enabled = transferUserUiState !is TransferUserUiState.Loading
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = stringResource(R.string.transfer_user),
                        tint = if (transferUserUiState is TransferUserUiState.Loading) 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
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
                text = stringResource(R.string.name_label, user.allName),
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
                            contentDescription = stringResource(R.string.whatsapp),
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
                            contentDescription = stringResource(R.string.whatsapp),
                            tint = Color(0xFF25D366)
                        )
                    }
                }
            }
            // Start Date: revert to default (label and value together)
            Text(
                text = stringResource(R.string.start_date_label_full, user.startDate),
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
            
            // Get academic year months with proper year context
            val academicYearMonths = com.lee.timely.util.AcademicYearUtils.getAcademicYearMonths(currentAcademicYear)
            // Order months according to academic year (August to July)
            val months = listOf(8, 9, 10, 11, 12, 1, 2, 3, 4, 5, 6, 7)
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(6 * 60.dp),
                userScrollEnabled = false
            ) {
                items(months) { month ->
                    val paid = paidFlags[month - 1]
                    val year = academicYearMonths.find { it.first == month }?.second ?: 2024
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (paid) Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier
                            .padding(4.dp)
                            .size(80.dp, 48.dp)
                            .clickable {
                                Log.d("StudentProfile", "Month clicked: $month, paid: $paid")
                                selectedMonth = month
                                if (paid) {
                                    // If already paid, show cancel confirmation dialog
                                    Log.d("StudentProfile", "Showing cancel dialog for month: $month")
                                    showCancelDialog = true
                                } else {
                                    // If unpaid, show payment confirmation dialog
                                    Log.d("StudentProfile", "Showing payment dialog for month: $month")
                                    showConfirmDialog = true
                                }
                            },
                        shadowElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${monthNames[month-1]} $year",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall
                                        .withWinkRoughFont()
                                        .copy(fontSize = 12.sp)
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
                            onMonthPaid(selectedMonth!!, true) // Mark as paid
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
            
            // Cancel payment confirmation dialog
            if (showCancelDialog && selectedMonth != null) {
                AlertDialog(
                    onDismissRequest = { showCancelDialog = false },
                    title = { 
                        Text(
                            stringResource(R.string.confirm_cancel_payment_title),
                            style = MaterialTheme.typography.titleLarge.withWinkRoughFont()
                        )
                    },
                    text = { 
                        Text(
                            stringResource(R.string.confirm_cancel_payment_message, monthNames[selectedMonth!! - 1]),
                            style = MaterialTheme.typography.bodyLarge.withWinkRoughFont()
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            onMonthPaid(selectedMonth!!, false) // Mark as unpaid
                            showCancelDialog = false
                        }) {
                            Text(
                                stringResource(R.string.yes),
                                style = MaterialTheme.typography.titleMedium.withWinkRoughFont()
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCancelDialog = false }) {
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
                            stringResource(R.string.delete_confirmation, user.allName),
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
            
            // Transfer Bottom Sheet
            if (showTransferBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { 
                        showTransferBottomSheet = false
                        viewModel.resetTransferUserUiState()
                    },
                    sheetState = sheetState
                ) {
                    GroupSelectionBottomSheet(
                        groups = availableGroups,
                        currentGroupId = user.groupId,
                        onGroupSelected = { selectedGroup ->
                            viewModel.transferUser(user, selectedGroup.id, context)
                            showTransferBottomSheet = false
                        },
                        onDismiss = { 
                            showTransferBottomSheet = false
                            viewModel.resetTransferUserUiState()
                        }
                    )
                }
            }
            
            // Transfer Loading Overlay
            if (transferUserUiState is TransferUserUiState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.transferring_user),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
} 