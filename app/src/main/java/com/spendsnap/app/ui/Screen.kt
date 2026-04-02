package com.spendsnap.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val icon: ImageVector? = null, val label: String? = null) {
    object Home : Screen("home", Icons.Default.Home, "Home")
    object History : Screen("history", Icons.Default.DateRange, "History")
    object Camera : Screen("camera", Icons.Default.Home, "Camera")
    object Categories : Screen("categories", Icons.Default.Home, "Categories")
    object Profile : Screen("profile", Icons.Default.Person, "Profile")
    object TransactionDetail : Screen("transaction_detail")
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.History,
    Screen.Camera,
    Screen.Categories,
    Screen.Profile
)
