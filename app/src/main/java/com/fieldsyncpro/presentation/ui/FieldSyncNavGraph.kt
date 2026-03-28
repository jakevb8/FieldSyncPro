package com.fieldsyncpro.presentation.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fieldsyncpro.presentation.ui.screen.TaskDetailScreen
import com.fieldsyncpro.presentation.ui.screen.TaskListScreen

private const val ROUTE_TASK_LIST   = "task_list"
private const val ROUTE_TASK_DETAIL = "task_detail/{taskId}"
private const val ROUTE_TASK_CREATE = "task_create"
private const val ARG_TASK_ID       = "taskId"

@Composable
fun FieldSyncNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_TASK_LIST) {
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
                taskId          = taskId,
                onNavigateBack  = { navController.popBackStack() }
            )
        }
        composable(ROUTE_TASK_CREATE) {
            TaskDetailScreen(
                taskId          = null,
                onNavigateBack  = { navController.popBackStack() }
            )
        }
    }
}
