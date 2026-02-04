package com.pecule.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pecule.app.ui.screens.budget.BudgetScreen
import com.pecule.app.ui.screens.dashboard.DashboardScreen
import com.pecule.app.ui.screens.profile.CategoriesScreen
import com.pecule.app.ui.screens.profile.ProfileScreen
import com.pecule.app.ui.screens.statistics.StatisticsScreen

@Composable
fun PeculeNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Dashboard.route,
        modifier = modifier
    ) {
        composable(Routes.Dashboard.route) {
            DashboardScreen(
                onNavigateToProfile = {
                    navController.navigate(Routes.Profile.route)
                }
            )
        }
        composable(Routes.Budget.route) {
            BudgetScreen()
        }
        composable(Routes.Statistics.route) {
            StatisticsScreen()
        }
        composable(Routes.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCategories = {
                    navController.navigate(Routes.Categories.route)
                }
            )
        }
        composable(Routes.Categories.route) {
            CategoriesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
