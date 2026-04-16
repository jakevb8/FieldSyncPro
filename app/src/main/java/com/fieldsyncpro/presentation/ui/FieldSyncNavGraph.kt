package com.fieldsyncpro.presentation.ui

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fieldsyncpro.presentation.ui.screen.LoginScreen
import com.fieldsyncpro.presentation.ui.screen.TaskDetailScreen
import com.fieldsyncpro.presentation.ui.screen.TaskListScreen
import com.fieldsyncpro.presentation.viewmodel.AuthViewModel

private const val ROUTE_LOGIN       = "login"
private const val ROUTE_TASK_LIST   = "task_list"
private const val ROUTE_TASK_DETAIL = "task_detail/{taskId}"
private const val ROUTE_TASK_CREATE = "task_create"
private const val ARG_TASK_ID       = "taskId"

@Composable
fun FieldSyncNavGraph(
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val currentUser by authViewModel.currentUser.collectAsState()

    // Derive whether the user is authenticated; null means "not yet known" (splash).
    // We use a key on currentUser so the LaunchedEffect re-runs on auth state changes.
    LaunchedEffect(currentUser) {
        val destination = navController.currentDestination?.route
        if (currentUser == null) {
            // Not signed in — go to login (clear back stack so back doesn't return to tasks)
            if (destination != ROUTE_LOGIN) {
                navController.navigate(ROUTE_LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        } else {
            // Signed in — leave login screen
            if (destination == ROUTE_LOGIN) {
                navController.navigate(ROUTE_TASK_LIST) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = ROUTE_LOGIN) {
        composable(ROUTE_LOGIN) {
            LoginScreen(
                onNavigateToTaskList = {
                    navController.navigate(ROUTE_TASK_LIST) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(ROUTE_TASK_LIST) {
            TaskListScreen(
                onNavigateToDetail = { id ->
                    navController.navigate("task_detail/$id")
                },
                onNavigateToCreate = {
                    navController.navigate(ROUTE_TASK_CREATE)
                }
            )
        }
        composable(
            route     = ROUTE_TASK_DETAIL,
            arguments = listOf(navArgument(ARG_TASK_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString(ARG_TASK_ID)
            TaskDetailScreen(
                taskId         = taskId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(ROUTE_TASK_CREATE) {
            TaskDetailScreen(
                taskId         = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
