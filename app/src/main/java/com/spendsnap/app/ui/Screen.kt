package com.spendsnap.app.ui

import androidx.annotation.DrawableRes
import com.spendsnap.app.R

sealed class Screen(@DrawableRes val icon: Int? = null, val label: String? = null) {
    object Login : Screen()
    object Signup : Screen()
    object Home : Screen(R.drawable.outline_category_24, "Home")
    object History : Screen(R.drawable.outline_category_24, "History")
    object Camera : Screen(R.drawable.outline_category_24, "Camera")
    object Categories : Screen(R.drawable.outline_category_24, "Categories")
    object Profile : Screen(R.drawable.outline_category_24, "Profile")
    object TransactionDetail : Screen()
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.History,
    Screen.Camera,
    Screen.Categories,
    Screen.Profile
)
