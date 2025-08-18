package com.lee.timely.features.group.ui.view

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.lee.timely.R
import com.lee.timely.animation.NoGroupsAnimation
import com.lee.timely.animation.withWinkRoughFont
import com.lee.timely.features.group.ui.viewmodel.GroupDetailsViewModel
import com.lee.timely.features.group.ui.viewmodel.GroupDetailsViewModelFactory
import com.lee.timely.model.Repository
import com.lee.timely.model.User
import com.lee.timely.ui.theme.PaleSecondaryBlue
import com.lee.timely.ui.theme.PrimaryBlue
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    navController: NavController,
    groupName: String,
    groupId: Int,
    onAddUserClick: () -> Unit,
    onDeleteUser: (User) -> Unit,
    repository: Repository,
    onUserListUpdated: (() -> Unit)? = null,
    onUserAddedOrDeleted: (() -> Unit)? = null
) {
    val viewModel : GroupDetailsViewModel = viewModel(
        factory = GroupDetailsViewModelFactory(repository)
    )
    // Handle refresh when returning from AddUserScreen
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("refresh")
            ?.observeForever { shouldRefresh ->
                if (shouldRefresh) {
                    viewModel.refreshUserList()
                    // Reset the flag
                    navController.currentBackStackEntry?.savedStateHandle?.set("refresh", false)
                }
            }
    }

    // Initialize ViewModel with groupId
    LaunchedEffect(groupId) {
        viewModel.setGroupId(groupId)
    }

    // Collect UI state
    val uiState by viewModel.uiState.collectAsState()

    // Collect paged users with retry support
    val users = viewModel.users.collectAsLazyPagingItems()
    val unpaidUsers = viewModel.unpaidUsers.collectAsLazyPagingItems()
    
    // Track the current list of users for immediate UI updates
    val currentUsers = remember { mutableStateListOf<User>() }
    
    // Update current users when paging data changes
    LaunchedEffect(users.itemSnapshotList.items) {
        currentUsers.clear()
        currentUsers.addAll(users.itemSnapshotList.items)
    }
    
    // Observe refresh trigger
    LaunchedEffect(uiState.refreshTrigger) {
        // Force refresh both lists when refresh is triggered
        users.refresh()
        unpaidUsers.refresh()
    }

    // Handle user list updates
    LaunchedEffect(Unit) {
        onUserListUpdated?.invoke()
    }

    // Handle user added/deleted events
    LaunchedEffect(Unit) {
        // This will be called when returning from a screen where a user might have been deleted
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("refresh")?.observeForever { shouldRefresh ->
            if (shouldRefresh) {
                // Refresh the user lists
                users.refresh()
                if (uiState.selectedMonth != null) {
                    unpaidUsers.refresh()
                }
                // Notify parent component
                onUserAddedOrDeleted?.invoke()
                // Reset the flag
                navController.currentBackStackEntry?.savedStateHandle?.set("refresh", false)
            }
        }
    }

    // Retry loading if there's an error
    LaunchedEffect(users.loadState.refresh) {
        if (users.loadState.refresh is androidx.paging.LoadState.Error) {
            users.retry()
        }
    }

    LaunchedEffect(unpaidUsers.loadState.refresh) {
        if (unpaidUsers.loadState.refresh is androidx.paging.LoadState.Error) {
            unpaidUsers.retry()
        }
    }

    // Pull to refresh state
    val isRefreshing by remember { derivedStateOf { uiState.isRefreshing } }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    // Search state
    var searchText by remember { mutableStateOf("") }
    var debounceJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()

    // Handle search text changes
    fun onSearchTextChanged(text: String) {
        searchText = text
        debounceJob?.cancel()

        if (text.isNotEmpty()) {
            debounceJob = scope.launch {
                delay(500) // Wait for typing to stop
                viewModel.updateSearchQuery(text)
            }
        } else {
            viewModel.clearSearch()
        }
    }

    // Clear search
    val onClearSearch = {
        searchText = ""
        debounceJob?.cancel()
        viewModel.clearSearch()
    }

    // Snackbar host for error messages
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error message if any
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            val result = snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "Dismiss",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.Dismissed) {
                viewModel.clearError()
            }
        }
    }
    val student_deleted_successfully = stringResource(R.string.student_deleted_successfully)
    val error_deleting_student = stringResource(R.string.error_deleting_student )
    val error_unknown = stringResource(R.string.error_unknown)
    // Handle user deletion with proper refresh
    val onDeleteUserWithRefresh: (User) -> Unit = { user ->
        scope.launch {
            try {
                // 1. Call the delete operation
                onDeleteUser(user)
                
                // 2. Show success message
                snackbarHostState.showSnackbar(
                    message = student_deleted_successfully,
                    duration = SnackbarDuration.Short
                )
                
                // 3. Force refresh both user lists
                users.refresh()
                if (uiState.selectedMonth != null) {
                    unpaidUsers.refresh()
                }
                
                // 4. Notify parent component
                onUserAddedOrDeleted?.invoke()
                
            } catch (e: Exception) {
                Log.e("GroupDetailsScreen", "Error deleting user", e)
                snackbarHostState.showSnackbar(
                    message = error_deleting_student, e.message ?: error_unknown,
                    duration = SnackbarDuration.Long
                )
            }
        }
    }
    
    // Handle flag toggle using ViewModel
    val onFlagToggleWithRefresh: (Int, Int, Boolean) -> Unit = { userId, month, isPaid ->
        viewModel.toggleUserFlag(userId, month, isPaid)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = groupName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.semantics { contentDescription = "Back" }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Column {
                val addStudentDescription = stringResource(R.string.add_student)
                Spacer(modifier = Modifier.height(8.dp))
                FloatingActionButton(
                    onClick = onAddUserClick,
                    containerColor = PrimaryBlue,
                    modifier = Modifier.semantics {
                        contentDescription = addStudentDescription
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_student)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            SearchBar(
                query = searchText,
                onQueryChange = { onSearchTextChanged(it) },
                onSearch = { /* Handled by onQueryChange */ },
                onClear = onClearSearch,
                isLoading = false, // No loading indicator during typing
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
            val months = listOf(
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
            // Month filter chips (moved outside SwipeRefresh)
            MonthFilterChips(
                selectedMonth = uiState.selectedMonth,
                monthNames = remember {
                    months
                },
                onMonthSelected = { month ->
                    // Update the selected month in ViewModel
                    viewModel.updateSelectedMonth(month)
                    
                    // Immediately refresh the data
                    scope.launch {
                        try {
                            // Force refresh the appropriate list based on month selection
                            if (month != null) {
                                // When a month is selected, refresh the unpaid users list
                                unpaidUsers.refresh()
                                // Also refresh the main users list in case we switch back to all months
                                users.refresh()
                            } else {
                                // When no month is selected, refresh the main users list
                                users.refresh()
                            }
                            
                            // Force a UI update
                            viewModel.refreshWithCurrentState()
                        } catch (e: Exception) {
                            Log.e("GroupDetailsScreen", "Error refreshing data after month selection", e)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp, horizontal = 4.dp)
            )

            // Get the current UI state
            val uiState by viewModel.uiState.collectAsState()
            
            // Swipe Refresh with User List (only contains the list now)
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = {
                    if (uiState.isUpdatingPayment) return@SwipeRefresh
                    scope.launch {
                        try {
                            users.refresh()
                            if (uiState.selectedMonth != null) {
                                unpaidUsers.refresh()
                            }
                            // Small delay to ensure the refresh is registered
                            delay(100)
                        } catch (e: Exception) {
                            // Ignore cancellation
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .pointerInput(uiState.isUpdatingPayment) {
                        if (uiState.isUpdatingPayment) {
                            // Disable all pointer input while updating
                            awaitPointerEventScope { awaitPointerEvent() }
                        }
                    }
            ) {
                // Get loading and error states
                val isLoading = users.loadState.refresh is androidx.paging.LoadState.Loading
                val isError = users.loadState.refresh is androidx.paging.LoadState.Error
                val selectedMonth = uiState.selectedMonth

                // Show loading state
                if (isLoading && users.itemCount == 0) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                // Show error state
                else if (isError) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.error_loading_students))
                    }
                }
                // Show student list
                else {
                    // Determine if we should show empty state
                    val showEmptyState = when {
                        isLoading -> false
                        selectedMonth != null -> users.itemCount == 0 && unpaidUsers.itemCount == 0
                        else -> users.itemCount == 0
                    }

                    // Track which sections to show
                    val showEmptyStateSection = remember(showEmptyState) { showEmptyState }

                    // Use the UserList composable with proper refresh handling
                    val usersToShow = if (selectedMonth != null) unpaidUsers else users
                    val uiState by viewModel.uiState.collectAsState()
                    
                    // Add a key to force recomposition when needed
                    val refreshKey by remember(users.loadState.refresh, users.itemCount) { 
                        mutableStateOf(UUID.randomUUID())
                    }
                    
                    // Use a derived state to track if we need to force a refresh
                    val forceRefresh by remember {
                        derivedStateOf { 
                            users.loadState.refresh is androidx.paging.LoadState.NotLoading 
                        } 
                    }
                    
                    // Force recomposition when needed
                    LaunchedEffect(forceRefresh) {
                        if (forceRefresh) {
                            // Small delay to ensure UI updates
                            delay(50)
                        }
                    }
                    
                    UserList(
                        users = usersToShow,
                        onFlagToggle = onFlagToggleWithRefresh,
                        onDeleteUser = onDeleteUserWithRefresh,
                        selectedMonth = selectedMonth,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { 
                                // Add a test tag for testing
                                testTag = "user-list-$refreshKey"
                            },
                        onUserClick = { user ->
                            navController.navigate("studentProfile/${user.uid}") {
                                // This ensures we can handle the back navigation result
                                launchSingleTop = true
                            }
                        },
                        onUserDeleted = {
                            // Refresh both lists
                            users.refresh()
                            if (selectedMonth != null) {
                                unpaidUsers.refresh()
                            }
                            // Notify parent component
                            onUserAddedOrDeleted?.invoke()
                        },
                        isUpdatingPayment = uiState.isUpdatingPayment,
                        lastUpdatedUser = uiState.lastUpdatedUser
                    )

                }
            }
        }
    }
}

@Composable
fun MonthFlagChip(
    month: Int,
    isActive: Boolean,
    onClick: (() -> Unit)? = null,
    monthName: String? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val paidColor = Color(0xFF4CAF50) // Green
    val unpaidColor = Color(0xFFF44336) // Red
    val disabledColor = Color(0xFF9E9E9E) // Grey for disabled state

    val containerColor = when {
        !enabled -> disabledColor
        isActive -> paidColor
        else -> unpaidColor
    }

    val contentAlpha = when {
        !enabled -> 0.5f
        isLoading -> 0.7f
        else -> 1f
    }

    val isClickable = onClick != null && enabled && !isLoading
    val elevation = if (isActive) 4.dp else 2.dp
    
    // Disable click ripple when loading
    val clickModifier = if (isClickable) {
        Modifier.clickable(enabled = !isLoading) { onClick?.invoke() }
    } else {
        Modifier
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = containerColor.copy(alpha = if (isLoading) 0.7f else 1f),
        modifier = Modifier
            .size(48.dp)
            .padding(1.dp)
            .then(clickModifier),
        shadowElevation = if (enabled && !isLoading) elevation else 0.dp,
        contentColor = Color.White.copy(alpha = contentAlpha)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (enabled) 1f else 0.6f)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(2.dp)
                ) {
                    Text(
                        text = month.toString(),
                        color = Color.White.copy(alpha = contentAlpha),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (monthName != null) {
                        Text(
                            text = monthName,
                            color = Color.White.copy(alpha = contentAlpha),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserListItem12Months(
    user: User,
    onFlagToggleMonth: (Int, Int, Boolean) -> Unit,
    onDeleteUser: () -> Unit,
    onProfileClick: (Int) -> Unit,
    isProcessing: Boolean,
    modifier: Modifier = Modifier,
    updatingMonth: Int? = null
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedMonth by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val payment_success = stringResource(R.string.payment_success)
    val payment_cancelled = stringResource(R.string.payment_cancelled)
    val error_occurred = stringResource(R.string.error_occurred)
    val please_try_again = stringResource(R.string.please_try_again)

    // Local function to handle flag toggle
    fun handleFlagToggle(month: Int, isPaid: Boolean) {
        // Only proceed if not already processing
        if (isProcessing) return
        
        try {
            // Call the toggle function - the ViewModel will handle the loading state
            onFlagToggleMonth(user.uid, month, isPaid)
            
            // Show success message after a short delay
            scope.launch {
                delay(300) // Short delay to ensure the loading state is visible
                val message = if (isPaid) payment_success else payment_cancelled
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
        } catch (e: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error_occurred, e.message ?: please_try_again,
                    duration = SnackbarDuration.Long
                )
            }
        }
    }
    
    // Function to handle month click - shows confirmation dialog for unpaid months
    fun onMonthClick(month: Int, isPaid: Boolean) {
        if (isProcessing) return
        
        if (isPaid) {
            // If already paid, toggle off immediately without confirmation
            handleFlagToggle(month, false)
        } else {
            // If unpaid, show confirmation dialog
            selectedMonth = month
            showConfirmDialog = true
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        val contactName = user.firstName + " " + user.lastName
        var isDeleting by remember { mutableStateOf(false) }
        val student_deleted_failed = stringResource(R.string.student_deleted_failed)
        val unknown_error = stringResource(R.string.unknown_error)
        val student_deleted_successfully = stringResource(R.string.student_deleted_successfully)
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
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
                TextButton(
                    onClick = {
                        isDeleting = true
                        scope.launch {
                            try {
                                onDeleteUser()
                                snackbarHostState.showSnackbar(
                                    message = student_deleted_successfully,
                                    duration = SnackbarDuration.Short
                                )
                                showDeleteDialog = false
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    message = student_deleted_failed, e.message ?: unknown_error,
                                    duration = SnackbarDuration.Long
                                )
                            } finally {
                                isDeleting = false
                            }
                        }
                    },
                    enabled = !isDeleting && !isProcessing
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            stringResource(R.string.confirm),
                            style = MaterialTheme.typography.titleMedium.withWinkRoughFont()
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!isDeleting) showDeleteDialog = false },
                    enabled = !isDeleting && !isProcessing
                ) {
                    Text(
                        stringResource(R.string.cancel),
                        style = MaterialTheme.typography.titleMedium.withWinkRoughFont()
                    )
                }
            }
        )
    }

    // Payment confirmation dialog
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
            onDismissRequest = { if (!isProcessing) showConfirmDialog = false },
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
                TextButton(
                    onClick = {
                        if (!isProcessing) {
                            selectedMonth?.let { month ->
                                handleFlagToggle(month, true)
                                showConfirmDialog = false
                            }
                        }
                    },
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            stringResource(R.string.yes),
                            style = MaterialTheme.typography.titleMedium.withWinkRoughFont()
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!isProcessing) showConfirmDialog = false },
                    enabled = !isProcessing
                ) {
                    Text(
                        stringResource(R.string.no),
                        style = MaterialTheme.typography.titleMedium.withWinkRoughFont()
                    )
                }
            }
        )
    }

    // Main card content
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(enabled = !isProcessing) { onProfileClick(user.uid) }
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
        // Show loading indicator at the top of the card when processing
        if (isProcessing) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp , top = 8.dp , end = 8.dp)
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
                        text = "${user.firstName} ${user.lastName}".trim(),
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
                    onClick = { 
                        if (!isProcessing) {
                            showDeleteDialog = true 
                        }
                    },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = if (isProcessing) 
                            MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.5f) 
                        else 
                            MaterialTheme.colorScheme.onErrorContainer
                    ),
                    enabled = !isProcessing
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
                    text = stringResource(R.string.start_date_label) + ": " + user.startDate,
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
                                .padding(horizontal = 4.dp , vertical = 4.dp)
                        ) {
                            val isThisMonthUpdating = isProcessing && updatingMonth == month
                            val isAnyMonthUpdating = isProcessing && updatingMonth != null
                            
                            MonthFlagChip(
                                month = month,
                                isActive = paid,
                                onClick = { 
                                    if (paid) {
                                        // If already paid, toggle off immediately without confirmation
                                        handleFlagToggle(month, false)
                                    } else {
                                        // If unpaid, show confirmation dialog
                                        selectedMonth = month
                                        showConfirmDialog = true
                                    }
                                },
                                monthName = monthNames[month - 1],
                                enabled = !isAnyMonthUpdating || isThisMonthUpdating,
                                isLoading = isThisMonthUpdating
                            )
                        }
                    }
                }
            }
        }
    }

    // Payment confirmation dialog
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
            onDismissRequest = { if (!isProcessing) showConfirmDialog = false },
            title = { Text(
                stringResource(R.string.confirm_payment_title),
                style = MaterialTheme.typography.titleLarge.withWinkRoughFont()
            ) },
            text = {
                Text(
                    stringResource(R.string.confirm_payment_message, monthNames[selectedMonth!! - 1]),
                    style = MaterialTheme.typography.bodyLarge.withWinkRoughFont()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (!isProcessing) {
                            selectedMonth?.let { month ->
                                handleFlagToggle(month, true)
                                showConfirmDialog = false
                            }
                        }
                    },
                    enabled = !isProcessing
                ) {
                    Text(
                        text = stringResource(R.string.yes),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium.withWinkRoughFont()

                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!isProcessing) showConfirmDialog = false },
                    enabled = !isProcessing
                ) {
                    Text(
                        text = stringResource(R.string.no),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium.withWinkRoughFont()

                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    // Snackbar host for showing messages
    SnackbarHost(hostState = snackbarHostState)
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = { Text(stringResource(R.string.search_users)) },
        leadingIcon = {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingIcon = {
            if (isLoading) {
                // Show nothing while loading to avoid layout shifts
                null
            } else if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = stringResource(R.string.clear_search),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                null
            }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(16.dp),
        enabled = !isLoading
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthFilterChips(
    selectedMonth: Int?,
    monthNames: List<String>,
    onMonthSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAllMonthsSheet by remember { mutableStateOf(false) }
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1 // 1-12
    
    // Show current, previous, and next month
    val visibleMonths = remember(currentMonth) {
        listOf(
            if (currentMonth == 1) 12 else currentMonth - 1, // Previous month
            currentMonth,                                    // Current month
            if (currentMonth == 12) 1 else currentMonth + 1  // Next month
        )
    }

    // Bottom sheet for all months
    if (showAllMonthsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAllMonthsSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                // 4 columns grid for months to save space
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(monthNames.size) { index ->
                        val monthNumber = index + 1
                        val isSelected = selectedMonth == monthNumber
                        
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                   else PaleSecondaryBlue,
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clickable {
                                    onMonthSelected(monthNumber)
                                    showAllMonthsSheet = false
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = monthNames[index],
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                           else MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily(Font(R.font.winkyrough_mediumitalic)),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // All months button
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (selectedMonth == null) MaterialTheme.colorScheme.primary
                           else PaleSecondaryBlue,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (selectedMonth == null) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clickable {
                            onMonthSelected(null)
                            showAllMonthsSheet = false
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.show_all_months),
                            color = if (selectedMonth == null) MaterialTheme.colorScheme.onPrimary
                                   else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily(Font(R.font.winkyrough_mediumitalic)),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .height(40.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // All months button
        Surface(
            shape = MaterialTheme.shapes.small,
            color = if (selectedMonth == null) MaterialTheme.colorScheme.primary
                   else PaleSecondaryBlue,
            border = BorderStroke(
                width = 1.dp,
                color = if (selectedMonth == null) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable { onMonthSelected(null) }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.all),
                    color = if (selectedMonth == null) MaterialTheme.colorScheme.onPrimary
                           else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily(Font(R.font.winkyrough_mediumitalic)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // Current, previous, and next month
        visibleMonths.forEach { month ->
            val isSelected = selectedMonth == month
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                       else PaleSecondaryBlue,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onMonthSelected(month) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = monthNames[month - 1],
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily(Font(R.font.winkyrough_mediumitalic)),
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        // Filter/All months button
        Surface(
            shape = MaterialTheme.shapes.small,
            color = PaleSecondaryBlue,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable { showAllMonthsSheet = true }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = stringResource(R.string.view_all_months),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
    private fun UserList(
    users: LazyPagingItems<User>,
    onFlagToggle: (Int, Int, Boolean) -> Unit,
    onDeleteUser: (User) -> Unit,
    selectedMonth: Int? = null,
    modifier: Modifier = Modifier,
    onUserDeleted: () -> Unit = {},
    onUserClick: (User) -> Unit = {},
    isUpdatingPayment: Boolean = false,
    lastUpdatedUser: Pair<Int, Int>? = null
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Track which user is being updated using a local variable
    var localUpdatingUser by remember { mutableStateOf<Pair<Int, Int>?>(lastUpdatedUser) }
    
    // Update local state when props change, but only if we don't have a local update in progress
    LaunchedEffect(lastUpdatedUser) {
        if (lastUpdatedUser == null) {
            localUpdatingUser = null
        } else if (localUpdatingUser == null || lastUpdatedUser != localUpdatingUser) {
            localUpdatingUser = lastUpdatedUser
        }
    }
    
    // Clear local updating state when update completes
    LaunchedEffect(isUpdatingPayment) {
        if (!isUpdatingPayment) {
            // Small delay to allow the UI to update before clearing the state
            delay(100)
            localUpdatingUser = null
        }
    }
    
    // Handle pull-to-refresh
    val isRefreshing = users.loadState.refresh is androidx.paging.LoadState.Loading || isUpdatingPayment
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)
    
    // Disable swipe refresh while updating payment
    val isSwipeRefreshEnabled = !isUpdatingPayment
    
    // Handle refresh and scroll to top when list is updated
    LaunchedEffect(users.loadState.refresh) {
        when (val refreshState = users.loadState.refresh) {
            is androidx.paging.LoadState.Loading -> {
                if (!swipeRefreshState.isRefreshing) {
                    swipeRefreshState.isRefreshing = true
                }
            }
            is androidx.paging.LoadState.NotLoading -> {
                if (swipeRefreshState.isRefreshing) {
                    swipeRefreshState.isRefreshing = false
                    // Small delay to ensure UI updates before scrolling
                    delay(50)
                    listState.animateScrollToItem(0)
                }
            }
            is androidx.paging.LoadState.Error -> {
                swipeRefreshState.isRefreshing = false
                // Handle error if needed
            }
        }
    }
    
    // Separate paid and unpaid users when a month is selected
    val (paidUsers, unpaidUsers) = remember(users.itemSnapshotList.items, selectedMonth, localUpdatingUser) {
        if (selectedMonth != null) {
            val (paid, unpaid) = users.itemSnapshotList.items.partition { user ->
                // If this is the user being updated, use the opposite of the current state
                // to avoid UI flicker while the update is in progress
if (isUpdatingPayment && user.uid == localUpdatingUser?.first) {
                    !when (selectedMonth) {
                        1 -> user.flag1
                        2 -> user.flag2
                        3 -> user.flag3
                        4 -> user.flag4
                        5 -> user.flag5
                        6 -> user.flag6
                        7 -> user.flag7
                        8 -> user.flag8
                        9 -> user.flag9
                        10 -> user.flag10
                        11 -> user.flag11
                        12 -> user.flag12
                        else -> false
                    }
                } else {
                    when (selectedMonth) {
                        1 -> user.flag1
                        2 -> user.flag2
                        3 -> user.flag3
                        4 -> user.flag4
                        5 -> user.flag5
                        6 -> user.flag6
                        7 -> user.flag7
                        8 -> user.flag8
                        9 -> user.flag9
                        10 -> user.flag10
                        11 -> user.flag11
                        12 -> user.flag12
                        else -> false
                    }
                }
            }
            Pair(paid, unpaid)
        } else {
            Pair(emptyList(), emptyList())
        }
    }

    
    // Handle user deletion with error handling and refresh
    val onDeleteUserWithRefresh: (User) -> Unit = { user ->
        coroutineScope.launch {
            try {
                // Call the delete operation
                onDeleteUser(user)
                
                // Show a small delay to ensure the deletion is processed
                delay(100)
                
                // Force refresh the paging data
                users.refresh()
                
                // Notify parent component about the deletion
                onUserDeleted()
                
                // Scroll to top to show the updated list
                listState.animateScrollToItem(0)
                
            } catch (e: Exception) {
                // Show error message
                Log.e("UserList", "Error deleting user", e)
                // Still refresh the list in case of error to ensure consistency
                users.refresh()
            }
        }
    }

    // Colors
    val paidColor = Color(0xFF4CAF50)
    val unpaidColor = Color(0xFFF44336)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    // Animated content for better transitions
    AnimatedVisibility(
        visible = users.loadState.refresh !is androidx.paging.LoadState.Loading,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Show loading state for first load
            if (users.loadState.refresh is androidx.paging.LoadState.Loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 3.dp
                        )
                    }
                }
            }

            // Show error state if any
            if (users.loadState.refresh is androidx.paging.LoadState.Error) {
                item {
                    val error = (users.loadState.refresh as? androidx.paging.LoadState.Error)?.error
                    ErrorState(
                        message = error?.message ?: "An error occurred",
                        onRetry = { users.retry() }
                    )
                }
            }

            // Show content if available
            if (users.itemCount > 0) {
                if (selectedMonth != null) {
                    // Paid users section
                    if (paidUsers.isNotEmpty()) {
                        stickyHeader(key = "paid_header") {
                            Surface(
                                color = surfaceColor,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(paidColor.copy(alpha = 0.1f))
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = paidColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.paid),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = paidColor,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = "${paidUsers.size} ${if (paidUsers.size == 1) "student" else "students"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = paidColor.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        items(paidUsers, key = { it.uid }) { user ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                val isUserUpdating = isUpdatingPayment && user.uid == localUpdatingUser?.first
                                UserListItem12Months(
                                    user = user,
                                    onFlagToggleMonth = { userId, month, isPaid ->
                                        onFlagToggle(userId, month, isPaid)
                                    },
                                    onDeleteUser = { onDeleteUserWithRefresh(user) },
                                    onProfileClick = { onUserClick(user) },
                                    isProcessing = isUserUpdating,
                                    updatingMonth = if (isUpdatingPayment) localUpdatingUser?.second else null,
                                    modifier = Modifier
                                        .animateItemPlacement()
                                        .clickable { onUserClick(user) }
                                )
                            }
                        }

                        // Divider between sections
                        item(key = "divider") {
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }
                    }

                    // Unpaid users section
                    stickyHeader(key = "unpaid_header") {
                        val showAllStudents = paidUsers.isEmpty()
                        val title = if (showAllStudents) stringResource(R.string.all_students)
                                  else stringResource(R.string.not_paid)
                        val count = if (showAllStudents) users.itemCount else unpaidUsers.size
                        val color = if (showAllStudents) onSurfaceColor.copy(alpha = 0.7f) else unpaidColor

                        Surface(
                            color = surfaceColor,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color.copy(alpha = if (showAllStudents) 0.05f else 0.1f))
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = color,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "$count ${if (count == 1) "student" else "students"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = color.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    items(if (paidUsers.isEmpty()) users.itemSnapshotList.items else unpaidUsers,
                          key = { it.uid }) { user ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            // Check if this is the specific user and month being updated
                            val isUserBeingUpdated = isUpdatingPayment &&
                                user.uid == localUpdatingUser?.first
                            val updatingMonth = if (isUserBeingUpdated) localUpdatingUser?.second else null

                            UserListItem12Months(
                                user = user,
                                onFlagToggleMonth = { userId, month, isPaid ->
                                    onFlagToggle(userId, month, isPaid)
                                },
                                onDeleteUser = { onDeleteUserWithRefresh(user) },
                                onProfileClick = { onUserClick(user) },
                                isProcessing = isUserBeingUpdated,
                                updatingMonth = updatingMonth,
                                modifier = Modifier
                                    .animateItemPlacement()
                                    .clickable { onUserClick(user) }
                            )
                        }
                    }
                } else {
                    // All users view (no month selected)
                    items(users.itemSnapshotList.items, key = { it.uid }) { user ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            // Check if this is the specific user and month being updated
                            val isUserBeingUpdated = isUpdatingPayment &&
                                user.uid == localUpdatingUser?.first
                            val updatingMonth = if (isUserBeingUpdated) localUpdatingUser?.second else null

                            UserListItem12Months(
                                user = user,
                                onFlagToggleMonth = { userId, month, isPaid ->
                                    onFlagToggle(userId, month, isPaid)
                                },
                                onDeleteUser = { onDeleteUserWithRefresh(user) },
                                onProfileClick = { onUserClick(user) },
                                isProcessing = isUserBeingUpdated,
                                updatingMonth = updatingMonth,
                                modifier = Modifier
                                    .animateItemPlacement()
                                    .clickable { onUserClick(user) }
                            )
                        }
                    }
                }

                // Loading more indicator
                if (users.loadState.append is androidx.paging.LoadState.Loading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            } else if (users.loadState.refresh is androidx.paging.LoadState.NotLoading) {
                // Empty state
                item {
                    EmptyState(
                        message = stringResource(R.string.no_students_in_group),
                        onRefresh = { users.retry() }
                    )
                }
            }
        }

    }
}

@Composable
private fun EmptyState(
    message: String,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        NoGroupsAnimation(
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRefresh,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try Again")
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }

}