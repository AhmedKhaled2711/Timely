package com.lee.timely.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.lee.timely.details.view.GroupsScreen
import com.lee.timely.groupName.view.GroupDetailsScreen
import com.lee.timely.home.view.GradeScreen
import com.lee.timely.home.view.AddUserScreen
import com.lee.timely.home.viewModel.MainViewModel
import com.lee.timely.model.GradeYear

@Composable
fun AppNavGraph(viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "grade") {

        // Grade Screen
        composable("grade") {
            val schoolYears by viewModel.schoolYears.collectAsState()

            GradeScreen(
                navController = navController,
                schoolYears = schoolYears,
                onAddSchoolYear = { year ->
                    viewModel.insertSchoolYear(GradeYear(year = year))
                },
                onDeleteSchoolYear = { schoolYear ->
                    viewModel.deleteSchoolYear(schoolYear)
                }
            )
        }

//        // Details of a school year
//        composable(
//            route = "schoolYearDetails/{id}",
//            arguments = listOf(navArgument("id") { type = NavType.IntType })
//        ) { backStackEntry ->
//            val id = backStackEntry.arguments?.getInt("id") ?: 0  // Default to 0 if the ID is null
//            val groupNames by viewModel.getGroupsForYear(id).collectAsState(emptyList())
//
//            GroupsScreen(
//                navController = navController,
//                id = id,
//                groupNames = groupNames,
//                onAddGroupName = { groupName -> viewModel.addGroupToYear(id, groupName) },
//                onDeleteGroupName = { group -> viewModel.deleteGroup(group) },
//                onNavigateToGroup = { groupId -> navController.navigate("groupDetails/$groupId") }
//            )
//        }

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

            GroupsScreen(
                navController = navController,
                id = id,
                schoolYearName = name, // 💡 Pass name to display in TopAppBar
                groupNames = groupNames,
                onAddGroupName = { groupName -> viewModel.addGroupToYear(id, groupName) },
                onDeleteGroupName = { group -> viewModel.deleteGroup(group) },
                onNavigateToGroup = { groupId, groupName ->
                    navController.navigate("groupDetails/$groupId/$groupName")
                }
            )
        }




        // User screen for adding users to group
        composable(
            route = "user_screen/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0

            AddUserScreen(
                viewModel = viewModel,
                groupId = groupId,
                onUserAdded = {
                    navController.popBackStack()
                },
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }

//        composable(
//            route = "groupDetails/{groupId}",
//            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
//        ) { backStackEntry ->
//            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0
//            val usersFlow = viewModel.getUsersByGroup(groupId)
//            val users by usersFlow.collectAsState(initial = emptyList())
//
//            GroupDetailsScreen(
//                navController = navController,
//                users = users,
//                onAddUserClick = {
//                    navController.navigate("user_screen/$groupId")
//                }
//            )
//        }

        composable(
            route = "groupDetails/{groupId}/{groupName}",
            arguments = listOf(
                navArgument("groupId") { type = NavType.IntType },
                navArgument("groupName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0
            val groupName = backStackEntry.arguments?.getString("groupName") ?: ""
            val users by viewModel.getUsersByGroup(groupId).collectAsState(initial = emptyList())

            GroupDetailsScreen(
                navController = navController,
                users = users,
                groupName = groupName, // 💡 Pass to show in TopAppBar
                onAddUserClick = {
                    navController.navigate("user_screen/$groupId")
                },
                onFlagToggle = { userId, flagNumber, newValue ->
                    viewModel.toggleUserFlag(userId, flagNumber, newValue)
                },
                onDeleteUser = { user ->
                    viewModel.deleteUser(user)
                }
            )
        }




    }
}
