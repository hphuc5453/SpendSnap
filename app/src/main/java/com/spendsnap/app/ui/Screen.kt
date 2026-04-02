package com.spendsnap.app.ui

import androidx.annotation.DrawableRes
import com.spendsnap.app.R

sealed class Screen(@DrawableRes val icon: Int? = null, val label: String? = null) {
    object Home : Screen(R.drawable.outline_home_24, "Home")
    object History : Screen(R.drawable.outline_history_24, "History")
    object Camera : Screen(R.drawable.outline_photo_camera_24, "Camera")
    object Categories : Screen(R.drawable.outline_category_24, "Categories")
    object Profile : Screen(R.drawable.outline_person_apron_24, "Profile")
    object TransactionDetail : Screen()
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.History,
    Screen.Camera,
    Screen.Categories,
    Screen.Profile
)
