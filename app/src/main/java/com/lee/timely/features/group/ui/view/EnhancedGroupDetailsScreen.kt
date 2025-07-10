package com.lee.timely.features.group.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.lee.timely.R
import com.lee.timely.animation.NoGroupsAnimation
import com.lee.timely.model.User
import com.lee.timely.ui.theme.PrimaryBlue
import kotlinx.coroutines.flow.Flow
import java.text.Normalizer
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedGroupDetailsScreen(
    navController: NavController,
    usersPagingData: Flow<PagingData<User>>,
    groupName: String,
    onAddUserClick: () -> Unit,
    onFlagToggle: (Int, Int, Boolean) -> Unit,
    onDeleteUser: (User) -> Unit,
    isLoading: Boolean
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

    val pagingItems = usersPagingData.collectAsLazyPagingItems()

    // Filtering logic: robust for Arabic and English, supports multi-word search
    val queryWords = normalizeText(searchQuery).split(" ").filter { it.isNotBlank() }
    val filteredUsers = pagingItems.itemSnapshotList.items.filter { user ->
        val first = normalizeText(user.firstName)
        val last = normalizeText(user.lastName)
        queryWords.all { word ->
            first.contains(word) || last.contains(word)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(groupName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAddUserClick) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_student)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddUserClick,
                containerColor = PrimaryBlue
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_student),
                    tint = Color.White
                )
            }
        }
    ) { padding ->
        if (isLoading && pagingItems.loadState.refresh is LoadState.Loading) {
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
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

                    (currentMonth..minOf(currentMonth + 3, 12)).forEach { month ->
                        val monthIndex = month - 1
                        val isSelected = selectedMonth == month
                        Button(
                            onClick = { selectedMonth = if (isSelected) null else month },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) PrimaryBlue else Color.Gray
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = monthNames[monthIndex],
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // Content
                if (filteredUsers.isEmpty() && searchQuery.isEmpty() && pagingItems.loadState.refresh !is LoadState.Loading) {
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
                        // Show loading state at the end
                        if (pagingItems.loadState.append is LoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        // Show error state
                        if (pagingItems.loadState.append is LoadState.Error) {
                            item {
                                val error = (pagingItems.loadState.append as LoadState.Error).error
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Error: ${error.localizedMessage}",
                                        color = Color.Red
                                    )
                                }
                            }
                        }

                        // Filter and display users
                        val month = selectedMonth
                        val usersToShow = if (month != null) {
                            filteredUsers.filter { it.isMonthPaid(month) } +
                                    filteredUsers.filter { !it.isMonthPaid(month) }
                        } else {
                            filteredUsers
                        }

                        items(
                            items = usersToShow,
                            key = { it.uid }
                        ) { user ->
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