package com.lee.timely.navigation

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
import com.lee.timely.model.GradeYear
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.lee.timely.features.settings.SettingScreen
import com.lee.timely.features.group.ui.view.StudentProfileScreen
import com.lee.timely.features.home.ui.view.AddUserScreen
import com.lee.timely.features.home.ui.view.GradeScreen
import com.lee.timely.features.settings.ActivationScreen
import com.lee.timely.model.User
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
    } else {
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
                        navController.popBackStack()
                        // Additional delay to ensure navigation completes before refresh
                        kotlinx.coroutines.delay(50)
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

            GroupDetailsScreen(
                navController = navController,
                groupName = groupName,
                groupId = groupId,
                onAddUserClick = {
                    coroutineScope.launch {
                        navController.navigate("user_screen/$groupId")
                    }
                },
                onFlagToggle = { userId, flagNumber, newValue ->
                    coroutineScope.launch(Dispatchers.IO) {
                        viewModel.toggleUserFlag(userId, flagNumber, newValue)
                    }
                },
                onDeleteUser = { user ->
                    coroutineScope.launch(Dispatchers.IO) {
                        viewModel.deleteUser(user)
                    }
                },
                repository = viewModel.repositoryInstance
            )
        }

        composable(
            route = "studentProfile/{userId}",
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
                StudentProfileScreen(
                    user = user!!,
                    onBack = { navController.popBackStack() },
                    onMonthPaid = { month ->
                        coroutineScope.launch(Dispatchers.IO) {
                            viewModel.toggleUserFlag(userId, month, true)
                        }
                    },
                    onEditUser = { editUser ->
                        navController.navigate("edit_user/${editUser.uid}")
                    },
                    onDeleteUser = { deleteUser ->
                        coroutineScope.launch(Dispatchers.IO) {
                            viewModel.deleteUser(deleteUser)
                        }
                        navController.popBackStack()
                    }
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
        
        // composable(
        //     route = "performance_test/{groupId}",
        //     arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        // ) { backStackEntry ->
        //     val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0
        //     PerformanceTestScreen(
        //         navController = navController,
        //         groupId = groupId
        //     )
        // }
    }
}
