package com.lee.timely.features.home.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.lee.timely.R
import com.lee.timely.animation.NoGroupsAnimation
import com.lee.timely.model.GradeYear
import com.lee.timely.ui.theme.BackgroundCream
import com.lee.timely.ui.theme.PrimaryBlue
import com.lee.timely.ui.theme.SecondaryBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GradeScreen(
    navController: NavController,
    schoolYears: List<GradeYear>,
    isLoading: Boolean,
    onAddSchoolYear: (String) -> Unit,
    onDeleteSchoolYear: (GradeYear) -> Unit,
    onUpdateSchoolYear: (GradeYear) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedYearToDelete by remember { mutableStateOf<GradeYear?>(null) }
    var selectedYearToEdit by remember { mutableStateOf<GradeYear?>(null) }
    var inputTextError by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.all_grades),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    val settingDescription = stringResource(R.string.settings)
                    IconButton(
                        onClick = { navController.navigate("settings") },
                        modifier = Modifier.semantics { 
                            contentDescription = settingDescription
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = PrimaryBlue
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            val addSchoolYearDescription = stringResource(R.string.add_school_year)
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = PrimaryBlue,
                modifier = Modifier.semantics { 
                    contentDescription = addSchoolYearDescription
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_school_year))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                // You may want to trigger a reload from ViewModel here
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                if (schoolYears.isEmpty()) {
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
                                    text = stringResource(R.string.no_grades_message),
                                    style = TextStyle(
                                        fontFamily = winkRoughMediumItalic,
                                        fontSize = 20.sp
                                    )
                                )
                            }
                        }
                    }
                }
                else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(schoolYears) { year ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clickable {
                                        navController.navigate("schoolYearDetails/${year.id}/${year.year}")
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE3F2FD)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Year text - centered in the card
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = year.year,
                                            fontSize = 20.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                    
                                    // Action buttons at the bottom
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(horizontal = 0.dp , vertical = 5.dp),
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
                                                    selectedYearToEdit = year
                                                    inputText = year.year
                                                    showEditDialog = true
                                                },
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = stringResource(R.string.edit),
                                                    tint = PrimaryBlue ,
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
                                                    selectedYearToDelete = year
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

                // Add School Year Dialog
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text(stringResource(R.string.add_school_year), style = MaterialTheme.typography.titleLarge) },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = inputText,
                                    onValueChange = { 
                                        inputText = it
                                        inputTextError = it.isNotBlank() && it.length < 3
                                    },
                                    label = { Text(stringResource(R.string.school_year)) },
                                    isError = inputTextError,
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (inputTextError) {
                                    Text(
                                        text = stringResource(R.string.min_3_characters),
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    if (inputText.isNotBlank() && inputText.length >= 3) {
                                        onAddSchoolYear(inputText)
                                        inputText = ""
                                        showDialog = false
                                    } else {
                                        inputTextError = true
                                    }
                                },
                                enabled = inputText.isNotBlank() && inputText.length >= 3
                            ) {
                                Text(
                                    stringResource(R.string.add),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showDialog = false
                                    inputText = ""
                                    inputTextError = false
                                }
                            ) {
                                Text(
                                    stringResource(R.string.cancel),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    )
                }
                val school_year_updated = stringResource(R.string.school_year_updated)
                val school_year_deleted = stringResource(R.string.school_year_deleted)


                // Edit School Year Dialog
                if (showEditDialog) {
                    AlertDialog(
                        onDismissRequest = { 
                            showEditDialog = false
                            inputText = ""
                            inputTextError = false
                        },
                        title = { Text(stringResource(R.string.edit_school_year), style = MaterialTheme.typography.titleLarge) },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = inputText,
                                    onValueChange = { 
                                        inputText = it
                                        inputTextError = it.length < 3
                                    },
                                    label = { Text(stringResource(R.string.school_year)) },
                                    isError = inputTextError,
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (inputTextError) {
                                    Text(
                                        text = stringResource(R.string.min_3_characters),
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    if (inputText.isNotBlank() && inputText.length >= 3) {
                                        selectedYearToEdit?.let { year ->
                                            onUpdateSchoolYear(year.copy(year = inputText))
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(school_year_updated)
                                            }
                                        }
                                        inputText = ""
                                        showEditDialog = false
                                    } else {
                                        inputTextError = true
                                    }
                                },
                                enabled = inputText.isNotBlank() && inputText.length >= 3
                            ) {
                                Text(
                                    stringResource(R.string.save),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { 
                                    showEditDialog = false
                                    inputText = ""
                                    inputTextError = false
                                }
                            ) {
                                Text(
                                    stringResource(R.string.cancel),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    )
                }
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        confirmButton = {
                            TextButton(onClick = {
                                selectedYearToDelete?.let {
                                    onDeleteSchoolYear(it)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(school_year_deleted)
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
                        title = { Text(stringResource(R.string.delete_school_year), style = MaterialTheme.typography.titleLarge) },
                        text = {
                            Text(stringResource(R.string.delete_confirmation, selectedYearToDelete?.year ?: ""), style = MaterialTheme.typography.bodyLarge)
                        }
                    )
                }
            }
        }
    }
}