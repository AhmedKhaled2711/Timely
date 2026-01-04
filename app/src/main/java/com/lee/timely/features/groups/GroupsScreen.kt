package com.lee.timely.features.groups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.lee.timely.R
import com.lee.timely.animation.NoGroupsAnimation
import com.lee.timely.domain.GroupName
import com.lee.timely.ui.theme.BackgroundCream
import com.lee.timely.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    navController: NavController,
    id: Int,
    schoolYearName: String, // 👈 Add this
    groupNames: List<GroupName>,
    isLoading: Boolean,
    onAddGroupName: (String) -> Unit,
    onDeleteGroupName: (GroupName) -> Unit,
    onUpdateGroupName: (GroupName) -> Unit,
    onNavigateToGroup: (Int, String) -> Unit // 👈 Update to send name
) {
    var showDialog by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedGroupToDelete by remember { mutableStateOf<GroupName?>(null) }
    var selectedGroupToEdit by remember { mutableStateOf<GroupName?>(null) }
    var inputTextError by remember { mutableStateOf(false) }
    var hasLoadedOnce by remember { mutableStateOf(false) }
    
    // Search state
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Filter groups based on search query
    val filteredGroups = groupNames.filter { group ->
        if (searchQuery.isBlank()) {
            true // Show all groups if search is empty
        } else {
            group.groupName.contains(searchQuery, ignoreCase = true)
        }
    }
    
    LaunchedEffect(groupNames, isLoading) {
        if (!hasLoadedOnce && (groupNames.isNotEmpty() || !isLoading)) {
            hasLoadedOnce = true
        }
    }


    val columnCount = 2
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.details_for, schoolYearName),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = PrimaryBlue
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_group))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            Spacer(modifier = Modifier.height(5.dp))
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_groups),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedBorderColor = PrimaryBlue
                )
            )
            
            Spacer(modifier = Modifier.height(5.dp))
            
            SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                // You may want to trigger a reload from ViewModel here
            }
        ) {
            when {
                !hasLoadedOnce -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                filteredGroups.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val winkRoughMediumItalic = FontFamily(
                            Font(R.font.winkyrough_mediumitalic)
                        )
                        NoGroupsAnimation()
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isBlank()) {
                                    stringResource(R.string.no_groups_yet)
                                } else {
                                    stringResource(R.string.no_groups_found_matching, searchQuery)
                                },
                                style = TextStyle(
                                    fontFamily = winkRoughMediumItalic,
                                    fontSize = 20.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                        if (searchQuery.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.try_different_search_term),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columnCount),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredGroups) { group ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clickable {
                                        onNavigateToGroup(group.id, group.groupName)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE3F2FD)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Group name - centered in the card
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .padding(2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = group.groupName,
                                            fontSize = 20.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    // Action buttons at the bottom
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(horizontal = 0.dp, vertical = 1.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        // Edit button
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = BackgroundCream,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    selectedGroupToEdit = group
                                                    inputText = group.groupName
                                                    showEditDialog = true
                                                },
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = stringResource(R.string.edit),
                                                    tint = PrimaryBlue,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        // Delete button
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.errorContainer,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    selectedGroupToDelete = group
                                                    showDeleteDialog = true
                                                },
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = stringResource(R.string.delete),
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                            }
                        }
                    }
                    }
                }
            }
            
            // Dialogs
            val groupAdded = stringResource(R.string.group_added)
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            if (inputText.isNotBlank() && inputText.length >= 3) {
                                onAddGroupName(inputText)
                                inputText = ""
                                showDialog = false
                                inputTextError = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(groupAdded)
                                }
                            } else {
                                inputTextError = true
                            }
                        },
                        enabled = inputText.isNotBlank() && inputText.length >= 3
                    ) {
                        Text(stringResource(R.string.save))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDialog = false
                            inputText = ""
                        }) {
                            Text(stringResource(R.string.cancel), style = MaterialTheme.typography.titleMedium)
                        }
                    },
                    title = { Text(stringResource(R.string.add_group_name), style = MaterialTheme.typography.titleLarge) },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = {
                                    val filtered = it.filter { c -> c.isLetterOrDigit() || c.isWhitespace() }
                                        .replace(Regex("\\s+"), " ")
                                        .trimStart()
                                        .take(30)
                                    inputText = filtered
                                    inputTextError = filtered.length < 2
                                },
                                label = { Text(stringResource(R.string.group_name), style = MaterialTheme.typography.bodyLarge) },
                                isError = inputTextError,
                                singleLine = true
                            )
                            if (inputTextError) {
                                Text(
                                    text = stringResource(R.string.error_enter_group_name),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 1.dp),
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                )
            }
            val groupDeleted =  stringResource(R.string.group_deleted)
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            selectedGroupToDelete?.let {
                                onDeleteGroupName(it)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(groupDeleted)
                                }
                            }
                            showDeleteDialog = false
                        }) {
                            Text(stringResource(R.string.delete), style = MaterialTheme.typography.titleMedium)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text(stringResource(R.string.cancel), style = MaterialTheme.typography.titleMedium)
                        }
                    },
                    title = { Text(stringResource(R.string.delete_group_GroupsScreen), style = MaterialTheme.typography.titleLarge) },
                    text = {
                        Text(stringResource(R.string.delete_confirmation_GroupsScreen, selectedGroupToDelete?.groupName ?: ""), style = MaterialTheme.typography.bodyLarge)
                    }
                )
            }

            if (showEditDialog) {
                AlertDialog(
                    onDismissRequest = { showEditDialog = false },
                    title = { Text(stringResource(R.string.edit_group)) },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { 
                                    inputText = it
                                    inputTextError = it.length < 3
                                },
                                label = { Text(stringResource(R.string.group_name)) },
                                isError = inputTextError,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (inputTextError) {
                                Text(
                                    text = stringResource(R.string.group_name_too_short),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 1.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (inputText.isNotBlank() && inputText.length >= 3) {
                                    selectedGroupToEdit?.let { group ->
                                        onUpdateGroupName(group.copy(groupName = inputText))
                                    }
                                    inputText = ""
                                    showEditDialog = false
                                } else {
                                    inputTextError = true
                                }
                            },
                            enabled = inputText.isNotBlank() && inputText.length >= 3
                        ) {
                            Text(stringResource(R.string.save))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
        } // Close Column
    } // Close Scaffold

    }
}
