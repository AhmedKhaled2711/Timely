package com.lee.timely.features.group.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.lee.timely.features.group.ui.viewmodel.GroupDetailsViewModelFactory
import androidx.navigation.NavController
import com.lee.timely.R
import com.lee.timely.animation.NoGroupsAnimation
import com.lee.timely.features.group.ui.viewmodel.GroupDetailsViewModel
import com.lee.timely.model.Repository
import com.lee.timely.model.User
import com.lee.timely.ui.theme.ExtraLightSecondaryBlue
import com.lee.timely.ui.theme.PrimaryBlue
import java.text.Normalizer
import java.util.Calendar
import kotlinx.coroutines.delay

// Suppress warning for experimental API usage
@Suppress("OPT_IN_IS_NOT_ENABLED")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    navController: NavController,
    groupName: String,
    groupId: Int,
    onAddUserClick: () -> Unit,
    onFlagToggle: (Int, Int, Boolean) -> Unit, // (userId, flagNumber, newValue)
    onDeleteUser: (User) -> Unit,
    repository: Repository
) {
    val viewModel: GroupDetailsViewModel = viewModel(
        factory = GroupDetailsViewModelFactory(repository)
    )
    val lazyListState = rememberLazyListState()
    var selectedMonth by remember { mutableStateOf<Int?>(null) }
    val isArabic = java.util.Locale.getDefault().language == "ar"

    // Refresh trigger for actions
    var refreshTrigger by remember { mutableStateOf(0) }

    // Initialize ViewModel with groupId
    LaunchedEffect(groupId) {
        viewModel.setGroupId(groupId)
    }

    // Refresh users when screen becomes active (e.g., when returning from add user screen)
    LaunchedEffect(Unit) {
        // Initial refresh
        viewModel.forceRefreshUsers()
    }

    // Refresh when screen is focused (e.g., when returning from add user screen)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.forceRefreshUsers()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Refresh after actions
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            delay(100) // Small delay to ensure database operation completes
            viewModel.forceRefreshUsers()
        }
    }

    // Collect UI state and users
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val directUsers by viewModel.directUsers.collectAsState()

    // Helper to normalize text for robust search (works for Arabic and English)
    fun normalizeText(text: String): String {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .replace(Regex("[\\u064B-\\u0652]"), "") // Remove Arabic harakat
            .replace("أ", "ا")
            .replace("إ", "ا")
            .replace("آ", "ا")
            .replace("ى", "ي")
            .replace("ة", "ه")
            .replace("ؤ", "و")
            .replace("ئ", "ي")
            .lowercase()
            .trim()
    }

    // Filtering logic: robust for Arabic and English, supports multi-word search and UID search
    val queryWords = normalizeText(searchQuery).split(" ").filter { it.isNotBlank() }
    val filteredUsers = directUsers.filter { user ->
        val first = normalizeText(user.firstName)
        val last = normalizeText(user.lastName)
        val uid = user.uid.toString()
        queryWords.all { word ->
            first.contains(word, ignoreCase = true) || 
            last.contains(word, ignoreCase = true) ||
            uid.contains(word, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    stringResource(R.string.details_for, groupName),
                    style = MaterialTheme.typography.titleLarge
                ) },
                navigationIcon = {
                    val backDescription = stringResource(R.string.back)
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.semantics {
                            contentDescription = backDescription
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        floatingActionButton = {
            Column {
                // Performance test button (small FAB)
                // FloatingActionButton(
                //     onClick = { navController.navigate("performance_test/$groupId") },
                //     containerColor = MaterialTheme.colorScheme.secondary,
                //     modifier = Modifier.size(48.dp)
                // ) {
                //     Icon(Icons.Default.Settings, contentDescription = "Performance Test")
                // }
                Spacer(modifier = Modifier.height(8.dp))
                // Main add button
                //
                val addStudentDescription = stringResource(R.string.add_student)
                FloatingActionButton(
                    onClick = onAddUserClick,
                    containerColor = PrimaryBlue,
                    modifier = Modifier.semantics {
                        contentDescription = addStudentDescription
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_student))
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Search bar
                if (isArabic) {
                    val searchDescription = stringResource(R.string.search)
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search)) },
                            label = { Text(stringResource(R.string.search)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .semantics {
                                    contentDescription = searchDescription
                                },
                            textStyle = TextStyle(
                                textDirection = TextDirection.Content,
                                fontSize = 16.sp
                            )
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        label = { Text(stringResource(R.string.search)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        textStyle = TextStyle(
                            textDirection = TextDirection.Content,
                            fontSize = 16.sp
                        )
                    )
                }
                // Month filter
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
                // Two rows of 6 months each, with 'All' at the start
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // All months circle
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = if (selectedMonth == null) PrimaryBlue else Color.LightGray,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clickable { selectedMonth = null },
                            shadowElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = stringResource(R.string.all), color = Color.White, fontSize = 12.sp)
                            }
                        }
                        // First 6 months (Jan-Jun)
                        for (i in 1..6) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = if (selectedMonth == i) PrimaryBlue else Color.LightGray,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clickable { selectedMonth = i },
                                shadowElevation = 2.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = monthNames[i-1], color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Next 6 months (Jul-Dec)
                        for (i in 7..12) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = if (selectedMonth == i) PrimaryBlue else Color.LightGray,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clickable { selectedMonth = i },
                                shadowElevation = 2.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = monthNames[i-1], color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                        // Add an empty box to make 7 items in the second row for symmetry
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                if (directUsers.isEmpty()) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val winkRoughMediumItalic = FontFamily(
                                Font(R.font.winkyrough_mediumitalic)
                            )
                            NoGroupsAnimation()
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.no_students_in_group),
                                    style = TextStyle(
                                        fontFamily = winkRoughMediumItalic,
                                        fontSize = 20.sp
                                    )
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = lazyListState,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val month = selectedMonth
                        if (month != null) {
                            // Filter users by payment status for the selected month
                            val paidUsers = filteredUsers.filter { user -> user.isMonthPaid(month) }
                            val notPaidUsers = filteredUsers.filter { user -> !user.isMonthPaid(month) }

                            if (paidUsers.isNotEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = stringResource(R.string.paid),
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4CAF50),
                                            modifier = Modifier.padding(vertical = 12.dp)
                                        )
                                    }
                                }
                            }
                            items(paidUsers) { user ->
                                UserListItem12Months(
                                    user = user,
                                    onFlagToggleMonth = { flagNumber, newValue ->
                                        onFlagToggle(user.uid, flagNumber, newValue)
                                        refreshTrigger++
                                    },
                                    onDeleteUser = {
                                        onDeleteUser(user)
                                        refreshTrigger++
                                    },
                                    onProfileClick = { userId ->
                                        navController.navigate("studentProfile/$userId")
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (notPaidUsers.isNotEmpty()) {
                                item {
                                    if (paidUsers.isNotEmpty()) {
                                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                                    }
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = stringResource(R.string.not_paid),
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF44336),
                                            modifier = Modifier.padding(vertical = 12.dp)
                                        )
                                    }
                                }
                            }
                            items(notPaidUsers) { user ->
                                UserListItem12Months(
                                    user = user,
                                    onFlagToggleMonth = { flagNumber, newValue ->
                                        onFlagToggle(user.uid, flagNumber, newValue)
                                        refreshTrigger++
                                    },
                                    onDeleteUser = {
                                        onDeleteUser(user)
                                        refreshTrigger++
                                    },
                                    onProfileClick = { userId ->
                                        navController.navigate("studentProfile/$userId")
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        } else {
                            // Display all filtered users when no month is selected
                            items(filteredUsers) { user ->
                                UserListItem12Months(
                                    user = user,
                                    onFlagToggleMonth = { flagNumber, newValue ->
                                        onFlagToggle(user.uid, flagNumber, newValue)
                                        refreshTrigger++
                                    },
                                    onDeleteUser = {
                                        onDeleteUser(user)
                                        refreshTrigger++
                                    },
                                    onProfileClick = { userId ->
                                        navController.navigate("studentProfile/$userId")
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Updated parameter name for clarity
@Composable
fun UserListItem12Months(
    user: User,
    onFlagToggleMonth: (Int, Boolean) -> Unit, // (flagNumber, newValue)
    onDeleteUser: () -> Unit,
    onProfileClick: (Int) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedMonth by remember { mutableStateOf<Int?>(null) }
    if (showDeleteDialog) {
        val contactName = (user.firstName ?: "") + " " + (user.lastName ?: "")
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    stringResource(R.string.delete_student_title),
                    style = MaterialTheme.typography.titleLarge.withWinkRoughFont()
                )
            },
            text = {
                Text(
                    stringResource(R.string.delete_student_message, contactName),
                    style = MaterialTheme.typography.bodyLarge.withWinkRoughFont()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteUser()
                    showDeleteDialog = false
                })
                {
                    Text(
                        stringResource(R.string.confirm),
                        style = MaterialTheme.typography.titleMedium.withWinkRoughFont()
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        stringResource(R.string.cancel),
                        style = MaterialTheme.typography.titleMedium.withWinkRoughFont()
                    )
                }
            }
        )
    }
    if (showConfirmDialog && selectedMonth != null) {
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
                    onFlagToggleMonth(selectedMonth!!, true)
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onProfileClick(user.uid) }
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp,
            hoveredElevation = 6.dp,
            focusedElevation = 6.dp
        ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Info Column
                Column(modifier = Modifier.weight(1f)) {
                    // Student Name
                    Text(
                        text = "${user.firstName ?: ""} ${user.lastName ?: ""}".trim(),
                        style = MaterialTheme.typography.titleLarge
                            .withWinkRoughFont()
                            .copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp
                            ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.primary
                    )
                    // UID
                    Text(
                        text = stringResource(R.string.uid_text, user.uid.toString()),
                        style = MaterialTheme.typography.bodySmall
                            .withWinkRoughFont()
                            .copy(fontSize = 15.sp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Delete Button
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.delete),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Divider
            Spacer(modifier = Modifier.height(12.dp))
            Divider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Start Date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.start_date_label) + ": " + (user.startDate ?: ""),
                    style = MaterialTheme.typography.bodyMedium
                        .withWinkRoughFont()
                        .copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Month Chips Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {

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

                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH) + 1 // 1-based
                val monthsOrder = listOf(
                    if (currentMonth == 1) 12 else currentMonth - 1, // previous
                    currentMonth, // current
                    if (currentMonth == 12) 1 else currentMonth + 1 // next
                )

                // Month Chips Row with fixed width and centered
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    //horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    monthsOrder.forEach { month ->
                        val paid = when (month) {
                            8 -> user.flag8
                            9 -> user.flag9
                            10 -> user.flag10
                            11 -> user.flag11
                            12 -> user.flag12
                            1 -> user.flag1
                            2 -> user.flag2
                            3 -> user.flag3
                            4 -> user.flag4
                            5 -> user.flag5
                            6 -> user.flag6
                            7 -> user.flag7
                            else -> false
                        }

                        // Month chip with proper spacing
                        Box(
                            modifier = Modifier
                                .height(64.dp)
                        ) {
                            MonthFlagChip(
                                month = month,
                                isActive = paid,
                                onClick = { if (!paid) { selectedMonth = month; showConfirmDialog = true } },
                                monthName = monthNames[month - 1]
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthFlagChip(
    month: Int,
    isActive: Boolean,
    onClick: () -> Unit,
    monthName: String
) {
    val containerColor = if (isActive) {
        Color(0xFF4CAF50) // Green for paid
    } else {
        Color(0xFFF44336) // Red for unpaid
    }

    val contentColor = Color.White // White text for better contrast on both colors

    // Fixed size for the chip
    val chipSize = 60.dp

    Surface(
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        onClick = onClick,
        modifier = Modifier
            .size(chipSize)
            .padding(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Month number
            Text(
                text = month.toString(),
                style = MaterialTheme.typography.bodyMedium
                    .withWinkRoughFont()
                    .copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                color = contentColor,
                maxLines = 1,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            Text(
                text = monthName,
                style = MaterialTheme.typography.labelMedium
                    .withWinkRoughFont()
                    .copy(
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    ),
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Visible
            )
        }
    }
}

// Helper function to check if a user has paid for a given month
fun User.isMonthPaid(month: Int): Boolean = when (month) {
    1 -> flag1
    2 -> flag2
    3 -> flag3
    4 -> flag4
    5 -> flag5
    6 -> flag6
    7 -> flag7
    8 -> flag8
    9 -> flag9
    10 -> flag10
    11 -> flag11
    12 -> flag12
    else -> false
}

// Font family for the app
private val winkRoughMediumItalic = FontFamily(
    Font(R.font.winkyrough_mediumitalic)
)

// Extension function to apply the custom font to any TextStyle
fun TextStyle.withWinkRoughFont(): TextStyle {
    return this.copy(fontFamily = winkRoughMediumItalic)
}