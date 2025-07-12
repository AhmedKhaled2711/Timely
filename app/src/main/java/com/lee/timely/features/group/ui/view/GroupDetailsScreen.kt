package com.lee.timely.features.group.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.lee.timely.R
import com.lee.timely.animation.NoGroupsAnimation
import com.lee.timely.db.TimelyLocalDataSourceImpl
import com.lee.timely.features.home.viewmodel.viewModel.EnhancedMainViewModel
import com.lee.timely.features.home.viewmodel.viewModel.EnhancedMainViewModelFactory
import com.lee.timely.features.home.viewmodel.viewModel.MainViewModel
import com.lee.timely.features.home.viewmodel.viewModel.MainViewModelFactory
import com.lee.timely.model.RepositoryImpl
import com.lee.timely.model.User
import com.lee.timely.ui.theme.ExtraLightSecondaryBlue
import com.lee.timely.ui.theme.PrimaryBlue
import java.text.Normalizer
import java.util.Calendar

// Suppress warning for experimental API usage
@Suppress("OPT_IN_IS_NOT_ENABLED")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    navController: NavController,
    users: List<User>,
    groupName: String,
    groupId: Int,
    onAddUserClick: () -> Unit,
    onFlagToggle: (Int, Int, Boolean) -> Unit, // (userId, flagNumber, newValue)
    onDeleteUser: (User) -> Unit,
    loadMoreUsers: () -> Unit,
    isLoading: Boolean,
    isLastPage: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMonth by remember { mutableStateOf<Int?>(null) }
    val isArabic = java.util.Locale.getDefault().language == "ar"

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

    // Filtering logic: robust for Arabic and English, supports multi-word search
    val queryWords = normalizeText(searchQuery).split(" ").filter { it.isNotBlank() }
    val filteredUsers = users.filter { user ->
        val first = normalizeText(user.firstName)
        val last = normalizeText(user.lastName)
        queryWords.all { word ->
            first.contains(word) || last.contains(word)
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
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        floatingActionButton = {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                FloatingActionButton(
                    onClick = onAddUserClick,
                    containerColor = PrimaryBlue
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_student))
                }
            }
        }
    ) { padding ->
        if (isLoading) {
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
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
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
                } else {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
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
                if (filteredUsers.isEmpty()) {
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
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val month = selectedMonth
                        if (month != null) {
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
                                    },
                                    onDeleteUser = { onDeleteUser(user) },
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
                                    },
                                    onDeleteUser = { onDeleteUser(user) },
                                    onProfileClick = { userId ->
                                        navController.navigate("studentProfile/$userId")
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        } else {
                            items(filteredUsers) { user ->
                                UserListItem12Months(
                                    user = user,
                                    onFlagToggleMonth = { flagNumber, newValue ->
                                        onFlagToggle(user.uid, flagNumber, newValue)
                                    },
                                    onDeleteUser = { onDeleteUser(user) },
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
            title = { Text(stringResource(R.string.delete_student_title), style = MaterialTheme.typography.titleLarge) },
            text = { Text(stringResource(R.string.delete_student_message, contactName), style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteUser()
                    showDeleteDialog = false
                }) {
                    Text(stringResource(R.string.confirm), style = MaterialTheme.typography.titleMedium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel), style = MaterialTheme.typography.titleMedium)
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
            title = { Text(stringResource(R.string.confirm_payment_title), style = MaterialTheme.typography.titleLarge) },
            text = { Text(stringResource(R.string.confirm_payment_message, monthNames[selectedMonth!! - 1]), style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                TextButton(onClick = {
                    onFlagToggleMonth(selectedMonth!!, true)
                    showConfirmDialog = false
                }) {
                    Text(stringResource(R.string.yes), style = MaterialTheme.typography.titleMedium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(R.string.no), style = MaterialTheme.typography.titleMedium)
                }
            }
        )
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onProfileClick(user.uid) },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = ExtraLightSecondaryBlue
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Name and delete icon in top row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${user.firstName ?: ""} ${user.lastName ?: ""}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete User",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Start date only (no student number)
            Text(
                text = stringResource(R.string.start_date_label) + ": " + (user.startDate ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            // Months row (current + next 3 months)
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
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                for (month in monthsOrder) {
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

@Composable
fun MonthFlagChip(month: Int, isActive: Boolean, onClick: (() -> Unit)? = null, monthName: String? = null) {
    val paidColor = Color(0xFF4CAF50) // Green
    val unpaidColor = Color(0xFFF44336) // Red
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (isActive) paidColor else unpaidColor,
        modifier = Modifier
            .size(48.dp)
            .padding(1.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = month.toString(),
                    color = Color.White,
                    fontSize = 12.sp
                )
                if (monthName != null) {
                    Text(
                        text = monthName,
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
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