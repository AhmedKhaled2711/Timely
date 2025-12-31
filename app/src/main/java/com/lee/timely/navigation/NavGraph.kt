package com.lee.timely.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lee.timely.features.groups.GroupsScreen
import com.lee.timely.features.group.ui.view.GroupDetailsScreen
import com.lee.timely.features.home.viewmodel.viewModel.MainViewModel
import com.lee.timely.domain.GradeYear
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.lee.timely.features.settings.SettingScreen
import com.lee.timely.features.group.ui.view.StudentProfileScreen
import com.lee.timely.features.home.ui.view.AddUserScreen
import com.lee.timely.features.home.ui.view.GradeScreen
import com.lee.timely.features.settings.ActivationScreen
import com.lee.timely.domain.AcademicYearPayment
import com.lee.timely.domain.User
import com.lee.timely.util.AcademicYearUtils
import kotlinx.coroutines.withContext

@Composable
fun NavGraph(
    isActivated: Boolean,
    showActivationScreen: Boolean = false,
    onActivationComplete: () -> Unit,
    viewModel: MainViewModel
) {
    val navController = rememberNavController()

    if (showActivationScreen || !isActivated) {
        // Show activation screen
        ActivationScreen(
            onNavigateToHome = {
                // This will be called when activation is complete
                onActivationComplete()
            }
        )
    }
    else {
        // Show main app with navigation
        AppNavGraph(
            viewModel = viewModel,
            coroutineScope = CoroutineScope(Dispatchers.Main)
        )
    }
}

@Composable
fun AppNavGraph(
    viewModel: MainViewModel,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "grade") {
        composable("grade") {
            val schoolYears by viewModel.schoolYears.collectAsState()
            val isLoading by viewModel.isSchoolYearsLoading.collectAsState()

            GradeScreen(
                navController = navController,
                schoolYears = schoolYears,
                isLoading = isLoading,
                onAddSchoolYear = { year ->
                    coroutineScope.launch(Dispatchers.IO) {
                        viewModel.insertSchoolYear(GradeYear(year = year))
                    }
                },
                onDeleteSchoolYear = { schoolYear ->
                    coroutineScope.launch(Dispatchers.IO) {
                        viewModel.deleteSchoolYear(schoolYear)
                    }
                },
                onUpdateSchoolYear = { updatedYear ->
                    coroutineScope.launch(Dispatchers.IO) {
                        viewModel.updateSchoolYear(updatedYear)
                    }
                }
            )
        }

        composable(
            route = "schoolYearDetails/{id}/{name}",
            arguments = listOf(
                navArgument("id") { type = NavType.IntType },
                navArgument("name") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            val name = backStackEntry.arguments?.getString("name") ?: ""

            val groupNames by viewModel.getGroupsForYear(id).collectAsState(emptyList())
            val isLoading by viewModel.isGroupsLoading.collectAsState()

            // Handle refresh when returning from child screens
            LaunchedEffect(Unit) {
                // This will be called when returning from a screen where a user might have been transferred
                navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("refresh")?.observeForever { shouldRefresh ->
                    if (shouldRefresh) {
                        // Reset the flag
                        navController.currentBackStackEntry?.savedStateHandle?.set("refresh", false)
                        // Refresh the groups data by triggering recomposition
                        // The collectAsState will automatically refresh when the underlying data changes
                    }
                }
            }

            GroupsScreen(
                navController = navController,
                id = id,
                schoolYearName = name,
                groupNames = groupNames,
                isLoading = isLoading,
                onAddGroupName = { groupName ->
                    coroutineScope.launch(Dispatchers.IO) {
                        viewModel.addGroupToYear(id, groupName)
                    }
                },
                onDeleteGroupName = { group ->
                    coroutineScope.launch(Dispatchers.IO) {
                        viewModel.deleteGroup(group)
                    }
                },
                onUpdateGroupName = { updatedGroup ->
                    coroutineScope.launch(Dispatchers.IO) {
                        viewModel.updateGroup(updatedGroup)
                    }
                },
                onNavigateToGroup = { groupId, groupName ->
                    navController.navigate("groupDetails/$groupId/$groupName")
                }
            )
        }

        composable(
            route = "user_screen/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0

            AddUserScreen(
                viewModel = viewModel,
                groupId = groupId,
                onUserAdded = {
                    coroutineScope.launch {
                        // Small delay to ensure database operation completes
                        kotlinx.coroutines.delay(100)
                        // Pass back result to trigger refresh
                        navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                        navController.popBackStack()
                    }
                },
                onBackPressed = {
                    coroutineScope.launch {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(
            route = "groupDetails/{groupId}/{groupName}",
            arguments = listOf(
                navArgument("groupId") { type = NavType.IntType },
                navArgument("groupName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0
            val groupName = backStackEntry.arguments?.getString("groupName") ?: ""

            // Handle refresh when returning from child screens
            LaunchedEffect(Unit) {
                // This will be called when returning from a screen where a user might have been added/deleted
                navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("refresh")?.observeForever { shouldRefresh ->
                    if (shouldRefresh) {
                        // Reset the flag
                        navController.currentBackStackEntry?.savedStateHandle?.set("refresh", false)
                        // Refresh the data
                        viewModel.refreshUsers(groupId)
                    }
                }
            }
            
            GroupDetailsScreen(
                navController = navController,
                groupName = groupName,
                groupId = groupId,
                onAddUserClick = {
                    coroutineScope.launch {
                        navController.navigate("user_screen/$groupId") {
                            // This ensures we refresh when coming back from AddUserScreen
                            launchSingleTop = true
                        }
                    }
                },
//                onFlagToggle = { userId, flagNumber, newValue ->
//                    coroutineScope.launch(Dispatchers.IO) {
//                        viewModel.toggleUserFlag(userId, flagNumber, newValue)
//                    }
//                },
                onDeleteUser = { user ->
                    coroutineScope.launch(Dispatchers.IO) {
                        viewModel.deleteUser(user)
                        // Set refresh flag
                        withContext(Dispatchers.Main) {
                            navController.currentBackStackEntry?.savedStateHandle?.set("refresh", true)
                        }
                    }
                },
                repository = viewModel.repositoryInstance,
                onUserAddedOrDeleted = {
                    // Refresh the users list when a user is added or deleted
                    viewModel.refreshUsers(groupId)
                }
            )
        }

        composable(
            route = "studentProfile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            
            // Get user data using the new method
            var user by remember { mutableStateOf<User?>(null) }
            var userPayments by remember { mutableStateOf<List<AcademicYearPayment>>(emptyList()) }
            var isLoading by remember { mutableStateOf(true) }
            
            LaunchedEffect(userId) {
                user = viewModel.getUserById(userId)
                // Fetch payment data for the user
                val currentAcademicYear = AcademicYearUtils.getCurrentAcademicYear()
                userPayments = viewModel.getUserPayments(userId, currentAcademicYear)
                isLoading = false
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (user != null) {
                // Observe for refresh trigger when returning from this screen
                LaunchedEffect(Unit) {
                    navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("refresh")?.observeForever { shouldRefresh ->
                        if (shouldRefresh) {
                            // Clear the flag
                            navController.currentBackStackEntry?.savedStateHandle?.set("refresh", false)
                            // Notify the parent screen to refresh
                            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                        }
                    }
                }
                
                StudentProfileScreen(
                    user = user!!,
                    onBack = { navController.popBackStack() },
                    onMonthPaid = { month, isPaid ->
                        Log.d("NavGraph", "Payment action: month=$month, isPaid=$isPaid, userId=$userId")
                        Log.d("NavGraph", "Current userPayments size: ${userPayments.size}")
                        
                        // Simple optimistic update - create new list immediately
                        val currentAcademicYear = AcademicYearUtils.getCurrentAcademicYear()
                        val existingPayment = userPayments.find { it.month == month }
                        
                        Log.d("NavGraph", "Existing payment found: ${existingPayment != null}")
                        
                        val newPaymentsList = if (existingPayment != null) {
                            // Update existing payment
                            userPayments.map { payment ->
                                if (payment.month == month) {
                                    Log.d("NavGraph", "Updating existing payment for month $month")
                                    payment.copy(isPaid = isPaid, paymentDate = if (isPaid) java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) else null)
                                } else {
                                    payment
                                }
                            }
                        } else {
                            // Create new payment entry
                            Log.d("NavGraph", "Creating new payment for month $month")
                            try {
                                val academicYearMonths = com.lee.timely.util.AcademicYearUtils.getAcademicYearMonths(currentAcademicYear)
                                val monthYearPair = academicYearMonths.find { it.first == month }
                                if (monthYearPair == null) {
                                    Log.d("NavGraph", "Invalid month: $month")
                                    userPayments
                                } else {
                                    val year = monthYearPair.second
                                    val newPayment = AcademicYearPayment(
                                        userId = userId,
                                        academicYear = currentAcademicYear,
                                        month = month,
                                        year = year,
                                        isPaid = isPaid,
                                        paymentDate = if (isPaid) java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) else null
                                    )
                                    userPayments + newPayment
                                }
                            } catch (e: Exception) {
                                Log.d("NavGraph", "Error: ${e.message}")
                                userPayments
                            }
                        }
                        
                        Log.d("NavGraph", "New payments list size: ${newPaymentsList.size}")
                        Log.d("NavGraph", "About to update userPayments state...")
                        
                        // Update UI immediately
                        userPayments = newPaymentsList
                        
                        Log.d("NavGraph", "userPayments state updated! New size: ${userPayments.size}")
                        Log.d("NavGraph", "Optimistic UI update completed")
                        
                        // Then update database in background
                        coroutineScope.launch(Dispatchers.IO) {
                            Log.d("NavGraph", "Calling toggleUserFlag")
                            viewModel.toggleUserFlag(userId, month, isPaid)
                            Log.d("NavGraph", "toggleUserFlag completed")
                            // No need to refresh - optimistic update already shows correct state
                            Log.d("NavGraph", "Database update completed, optimistic UI state maintained")
                        }
                    },
                    onEditUser = { editUser ->
                        navController.navigate("edit_user/${editUser.uid}")
                    },
                    onDeleteUser = { deleteUser ->
                        coroutineScope.launch(Dispatchers.IO) {
                            viewModel.deleteUser(deleteUser)
                            // Set refresh flag before navigating back
                            withContext(Dispatchers.Main) {
                                navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                                navController.popBackStack()
                            }
                        }
                    },
                    navController = navController,
                    userPayments = userPayments,
                    viewModel = viewModel
                )
            }
        }

        composable(
            route = "edit_user/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            
            // Get user data using the new method
            var user by remember { mutableStateOf<User?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            
            LaunchedEffect(userId) {
                user = viewModel.getUserById(userId)
                isLoading = false
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (user != null) {
                AddUserScreen(
                    viewModel = viewModel,
                    groupId = user!!.groupId ?: 0,
                    onUserAdded = {
                        coroutineScope.launch {
                            // Small delay to ensure database operation completes
                            delay(100)
                            navController.popBackStack()
                        }
                    },
                    onBackPressed = {
                        coroutineScope.launch {
                            navController.popBackStack()
                        }
                    },
                    editUser = user
                )
            }
        }

        composable("settings") {
            SettingScreen(navController)
        }

    }
}
