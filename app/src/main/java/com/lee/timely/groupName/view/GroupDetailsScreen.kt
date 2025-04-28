package com.lee.timely.groupName.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lee.timely.R
import com.lee.timely.animation.NoGroupsAnimation
import com.lee.timely.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    navController: NavController,
    users: List<User>,
    groupName: String, // 👈 Add this
    onAddUserClick: () -> Unit,
    onFlagToggle: (Int, Int, Boolean) -> Unit, // userId, flagNumber, newValue
    onDeleteUser: (User) -> Unit // Add this parameter


) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details for $groupName") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddUserClick) {
                Icon(Icons.Default.Add, contentDescription = "Add User")
            }
        }
    ) { padding ->  // ⬅️ this 'padding' is automatically passed
        if (users.isEmpty()) {
            // Show animation if no users are available
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
                        "No users in this group",
                        style = TextStyle(
                            fontFamily = winkRoughMediumItalic,
                            fontSize = 20.sp
                        )
                    )
                }
            }
        }
        else {
            LazyColumn(
                contentPadding = padding ,
                modifier = Modifier.fillMaxSize()
            ) {
                items(users) { user ->
                    UserListItem(
                        user = user,
                        onFlagToggle = { flagNumber, newValue ->
                            onFlagToggle(user.uid, flagNumber, newValue)
                        },
                        onDeleteUser = { onDeleteUser(user) } // Pass the delete action

                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun UserListItem(
    user: User,
    onFlagToggle: (Int, Boolean) -> Unit ,
    onDeleteUser: () -> Unit // Add this parameter

) {
    var showDeleteDialog by remember { mutableStateOf(false) } // State for dialog

    // Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete User") },
            text = { Text("Are you sure you want to delete ${user.firstName} ${user.lastName}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteUser()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // User info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${user.firstName} ${user.lastName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
//                Text(
//                    text = "ID: ${user.uid}",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
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

            Spacer(modifier = Modifier.height(8.dp))

            // Group info
            Text(
                text = "Group: ${user.groupId}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Flags row
            Text(
                text = "Months",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FlagButton(
                    number = 1,
                    isActive = user.flag1,
                    onToggle = { newValue -> onFlagToggle(1, newValue) }
                )
                FlagButton(
                    number = 2,
                    isActive = user.flag2,
                    onToggle = { newValue -> onFlagToggle(2, newValue) }
                )
                FlagButton(
                    number = 3,
                    isActive = user.flag3,
                    onToggle = { newValue -> onFlagToggle(3, newValue) }
                )
                FlagButton(
                    number = 4,
                    isActive = user.flag4,
                    onToggle = { newValue -> onFlagToggle(4, newValue) }
                )
                FlagButton(
                    number = 5,
                    isActive = user.flag5,
                    onToggle = { newValue -> onFlagToggle(5, newValue) }
                )
                FlagButton(
                    number = 6,
                    isActive = user.flag6,
                    onToggle = { newValue -> onFlagToggle(6, newValue) }
                )
            }
        }
    }
}

@Composable
fun FlagButton(
    number: Int,
    isActive: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val greenColor = colorResource(id = R.color.green)

    OutlinedButton(
        onClick = { onToggle(!isActive) },
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isActive) greenColor else MaterialTheme.colorScheme.surface,
            contentColor = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isActive) greenColor else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.size(40.dp)
    ) {
        Text(text = number.toString())
    }
}
