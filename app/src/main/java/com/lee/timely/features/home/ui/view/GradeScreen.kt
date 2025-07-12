package com.lee.timely.features.home.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.lee.timely.R
import com.lee.timely.animation.NoGroupsAnimation
import com.lee.timely.model.GradeYear
import com.lee.timely.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GradeScreen(
    navController: NavController,
    schoolYears: List<GradeYear>,
    isLoading: Boolean,
    onAddSchoolYear: (String) -> Unit,
    onDeleteSchoolYear: (GradeYear) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedYearToDelete by remember { mutableStateOf<GradeYear?>(null) }
    var inputTextError by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)

    //val columnCount = max(1, sqrt(schoolYears.size.toDouble()).roundToInt())

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
                    IconButton(onClick = { navController.navigate("settings") }) {
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
            FloatingActionButton(
                onClick = { showDialog = true } ,
                containerColor = PrimaryBlue
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
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(schoolYears) { year ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .combinedClickable(
                                            onClick = {
                                                navController.navigate("schoolYearDetails/${year.id}/${year.year}")
                                            },
                                            onLongClick = {
                                                selectedYearToDelete = year
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
                                        Text(text = year.year,
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
                                    if (inputText.isNotBlank() && inputText.length >= 3) {
                                        onAddSchoolYear(inputText)
                                        inputText = ""
                                        showDialog = false
                                        inputTextError = false
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("School year added")
                                        }
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
                            title = { Text(stringResource(R.string.add_school_year), style = MaterialTheme.typography.titleLarge) },
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
                                        label = { Text(stringResource(R.string.school_year_hint), style = MaterialTheme.typography.bodyLarge) },
                                        isError = inputTextError,
                                        singleLine = true
                                    )
                                    if (inputTextError) {
                                        Text(
                                            text = stringResource(R.string.error_enter_school_year),
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, bottom = 4.dp),
                                            textAlign = TextAlign.Start
                                        )
                                    }
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
                                        snackbarHostState.showSnackbar("School year deleted")
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