package com.lee.timely.features.groups

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lee.timely.R
import com.lee.timely.animation.NoGroupsAnimation
import com.lee.timely.model.GroupName
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.lee.timely.ui.theme.PrimaryBlue


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    navController: NavController,
    id: Int,
    schoolYearName: String, // 👈 Add this
    groupNames: List<GroupName>,
    onAddGroupName: (String) -> Unit,
    onDeleteGroupName: (GroupName) -> Unit,
    onNavigateToGroup: (Int, String) -> Unit // 👈 Update to send name
) {
    var showDialog by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedGroupToDelete by remember { mutableStateOf<GroupName?>(null) }
    var inputTextError by remember { mutableStateOf(false) }

    val columnCount = 2

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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(10.dp)
        ) {
            if (groupNames.isEmpty()) {
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
                                text = stringResource(R.string.no_groups_yet),
                                style = TextStyle(
                                    fontFamily = winkRoughMediumItalic,
                                    fontSize = 20.sp
                                )
                            )
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnCount),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(groupNames) { group ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .combinedClickable(
                                    onClick = { onNavigateToGroup(group.id , group.groupName) },
                                    onLongClick = {
                                        selectedGroupToDelete = group
                                        showDeleteDialog = true
                                    }
                                ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE3F2FD) // Example light blue background
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = group.groupName,
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(5.dp)
                                )
                            }
                        }
                    }
                }
            }


            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            if (inputText.isNotBlank() && inputText.length >= 2) {
                                onAddGroupName(inputText)
                                inputText = ""
                                showDialog = false
                                inputTextError = false
                            } else {
                                inputTextError = true
                            }
                        }) {
                            Text(stringResource(R.string.add), style = MaterialTheme.typography.titleMedium)
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
                                    text = stringResource(id = R.string.error_enter_group_name),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, top = 2.dp),
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                )
            }

            if (showDeleteDialog && selectedGroupToDelete != null) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteDialog = false
                        selectedGroupToDelete = null
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            selectedGroupToDelete?.let {
                                onDeleteGroupName(it)
                            }
                            showDeleteDialog = false
                            selectedGroupToDelete = null
                        }) {
                            Text(stringResource(R.string.delete), style = MaterialTheme.typography.titleMedium)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDeleteDialog = false
                            selectedGroupToDelete = null
                        }) {
                            Text(stringResource(R.string.cancel), style = MaterialTheme.typography.titleMedium)
                        }
                    },
                    title = { Text(stringResource(R.string.delete_group_GroupsScreen), style = MaterialTheme.typography.titleLarge) },
                    text = {
                        Text(stringResource(R.string.delete_confirmation, selectedGroupToDelete?.groupName ?: ""), style = MaterialTheme.typography.bodyLarge)
                    })
                }
        }
    }
}

