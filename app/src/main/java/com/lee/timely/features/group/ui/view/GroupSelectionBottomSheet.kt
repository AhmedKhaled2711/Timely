package com.lee.timely.features.group.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lee.timely.domain.GroupName
import com.lee.timely.ui.theme.PrimaryBlue
import com.lee.timely.animation.withWinkRoughFont
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import com.lee.timely.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSelectionBottomSheet(
    groups: List<GroupName>,
    currentGroupId: Int?,
    onGroupSelected: (GroupName) -> Unit,
    onDismiss: () -> Unit
) {
    // Search state
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Filter groups based on search query
    val filteredGroups = groups.filter { group ->
        if (searchQuery.isBlank()) {
            true // Show all groups if search is empty
        } else {
            group.groupName.contains(searchQuery, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.select_group),
                style = MaterialTheme.typography.titleLarge
                    .withWinkRoughFont()
                    .copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_groups),
                        style = MaterialTheme.typography.bodyMedium
                            .withWinkRoughFont()
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
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(maxOf(200.dp, (filteredGroups.size * 60).dp))
            ) {
                if (filteredGroups.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = stringResource(R.string.no_groups),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isBlank()) {
                                    stringResource(R.string.no_available_groups_to_transfer_to)
                                } else {
                                    stringResource(R.string.no_groups_found_matching, searchQuery)
                                },
                                style = MaterialTheme.typography.bodyMedium
                                    .withWinkRoughFont()
                                    .copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        textAlign = TextAlign.Center
                                    )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isBlank()) {
                                    stringResource(R.string.create_more_groups_in_current_school_year)
                                } else {
                                    stringResource(R.string.try_different_search_term)
                                },
                                style = MaterialTheme.typography.bodySmall
                                    .withWinkRoughFont()
                                    .copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                        textAlign = TextAlign.Center
                                    )
                            )
                        }
                    }
                } else {
                    items(filteredGroups) { group ->
                    val isCurrentGroup = group.id == currentGroupId
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrentGroup) 
                                PrimaryBlue.copy(alpha = 0.1f) 
                            else 
                                MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (isCurrentGroup) 4.dp else 2.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clickable(enabled = !isCurrentGroup) {
                                    if (!isCurrentGroup) {
                                        onGroupSelected(group)
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = group.groupName,
                                    style = MaterialTheme.typography.titleMedium
                                        .withWinkRoughFont()
                                        .copy(
                                            fontSize = 16.sp,
                                            color = if (isCurrentGroup) 
                                                PrimaryBlue 
                                            else 
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                )
                                if (isCurrentGroup) {
                                    Text(
                                        text = stringResource(R.string.current_group),
                                        style = MaterialTheme.typography.bodySmall
                                            .withWinkRoughFont()
                                            .copy(
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            ),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                            
                            if (!isCurrentGroup) {
                                val layoutDirection = LocalLayoutDirection.current
                                Icon(
                                    imageVector = if (layoutDirection == LayoutDirection.Rtl) {
                                        Icons.Default.ArrowBackIosNew   // Arabic
                                    } else {
                                        Icons.Default.ArrowForwardIos  // English
                                    },
                                    contentDescription = stringResource(R.string.select_group_action),
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
