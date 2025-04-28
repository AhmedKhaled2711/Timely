package com.lee.timely.details.view

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

    val columnCount = max(1, sqrt(groupNames.size.toDouble()).roundToInt())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details for $schoolYearName") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Group")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (groupNames.isEmpty()) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
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
                        Text(
                            "No groups yet. Add one to get started!",
                            style = TextStyle(
                                fontFamily = winkRoughMediumItalic,
                                fontSize = 20.sp
                            )
                        )
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
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = group.groupName)
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
                            if (inputText.isNotBlank()) {
                                onAddGroupName(inputText)
                                inputText = ""
                                showDialog = false
                            }
                        }) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDialog = false
                            inputText = ""
                        }) {
                            Text("Cancel")
                        }
                    },
                    title = { Text("Add Group Name") },
                    text = {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            label = { Text("Group Name") },
                            singleLine = true
                        )
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
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDeleteDialog = false
                            selectedGroupToDelete = null
                        }) {
                            Text("Cancel")
                        }
                    },
                    title = { Text("Delete Group") },
                    text = {
                        Text("Are you sure you want to delete the group \"${selectedGroupToDelete?.groupName}\"?")
                    }
                )
            }
        }
    }
}
