package com.pecule.app.ui.navigation

sealed class Routes(val route: String) {
    data object Dashboard : Routes("dashboard")
    data object Budget : Routes("budget")
    data object Statistics : Routes("statistics")
    data object Profile : Routes("profile")
    data object Categories : Routes("categories")
}
