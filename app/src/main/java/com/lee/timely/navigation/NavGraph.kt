package com.lee.timely.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lee.timely.features.groups.GroupsScreen
import com.lee.timely.features.group.ui.view.GroupDetailsScreen
import com.lee.timely.features.home.ui.view.AddUserScreen
import com.lee.timely.features.home.ui.view.GradeScreen
import com.lee.timely.features.home.viewmodel.viewModel.MainViewModel
import com.lee.timely.model.GradeYear
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.lee.timely.features.settings.SettingScreen
import com.lee.timely.features.group.ui.view.StudentProfileScreen
// import com.lee.timely.features.testing.PerformanceTestScreen // commented for production

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

            val users by viewModel.users.collectAsState()
            val isLoading by viewModel.isLoading.collectAsState()
            val isLastPage by viewModel.isLastPage.collectAsState()

            LaunchedEffect(groupId) {
                coroutineScope.launch(Dispatchers.IO) {
                    viewModel.loadInitialUsers(groupId)
                }
            }

            GroupDetailsScreen(
                navController = navController,
                users = users,
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
                loadMoreUsers = {
                    coroutineScope.launch(Dispatchers.IO) {
                        viewModel.loadMoreUsers(groupId)
                    }
                },
                isLoading = isLoading,
                isLastPage = isLastPage
            )
        }

        composable(
            route = "studentProfile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val user = viewModel.users.collectAsState().value.find { it.uid == userId }
            if (user != null) {
                StudentProfileScreen(
                    user = user,
                    onBack = { navController.popBackStack() },
                    onMonthPaid = { month ->
                        coroutineScope.launch(Dispatchers.IO) {
                            viewModel.toggleUserFlag(user.uid, month, true)
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
            val user = viewModel.users.collectAsState().value.find { it.uid == userId }
            if (user != null) {
                AddUserScreen(
                    viewModel = viewModel,
                    groupId = user.groupId,
                    onUserAdded = {
                        coroutineScope.launch {
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
