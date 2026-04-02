package com.spendsnap.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val icon: ImageVector? = null, val label: String? = null) {
    object Home : Screen(Icons.Default.Home, "Home")
    object History : Screen(Icons.Default.DateRange, "History")
    object Camera : Screen(Icons.Default.Home, "Camera")
    object Categories : Screen(Icons.Default.Home, "Categories")
    object Profile : Screen(Icons.Default.Person, "Profile")
    object TransactionDetail : Screen()
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.History,
    Screen.Camera,
    Screen.Categories,
    Screen.Profile
)
