package com.spendsnap.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.spendsnap.app.data.local.AuthManager
import com.spendsnap.app.shared.Utils
import com.spendsnap.app.ui.Screen
import com.spendsnap.app.ui.auth.LoginScreen
import com.spendsnap.app.ui.auth.SignupScreen
import com.spendsnap.app.ui.bottomNavItems
import com.spendsnap.app.ui.camera.CameraScreen
import com.spendsnap.app.ui.categories.CategoriesScreen
import com.spendsnap.app.ui.history.HistoryScreen
import com.spendsnap.app.ui.history.TransactionDetailScreen
import com.spendsnap.app.ui.home.HomeScreen
import com.spendsnap.app.ui.theme.SpendSnapTheme
import com.spendsnap.app.ui.theme.bottomBackground
import com.spendsnap.app.ui.theme.topBackground
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpendSnapTheme {
                MainScreen(authManager)
            }
        }
    }
}

@Composable
fun MainScreen(authManager: AuthManager) {
    var selectedScreen by remember { mutableStateOf<Screen?>(null) }

    LaunchedEffect(Unit) {
        val token = authManager.accessToken.firstOrNull()
        selectedScreen = if (!token.isNullOrEmpty()) {
            Screen.Camera
        } else {
            Screen.Login
        }
    }

    if (selectedScreen == null) {
        return
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(topBackground, bottomBackground)
                )
            ),
        containerColor = Color.Transparent,
        bottomBar = {
            if (selectedScreen != Screen.Login && selectedScreen != Screen.Signup && selectedScreen != Screen.TransactionDetail) {
                SpendSnapBottomNav(
                    currentScreen = selectedScreen!!,
                    onScreenSelected = { selectedScreen = it }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Utils.AnimatedScreenTransition(targetState = selectedScreen) { screen ->
                when (screen) {
                    Screen.Login -> LoginScreen(
                        onLoginSuccess = { selectedScreen = Screen.Camera },
                        onSignupClick = { selectedScreen = Screen.Signup }
                    )

                    Screen.Signup -> SignupScreen(
                        onSignupSuccess = { selectedScreen = Screen.Login },
                        onBackToLogin = { selectedScreen = Screen.Login }
                    )

                    Screen.Home -> HomeScreen()
                    Screen.History -> HistoryScreen(onTransactionClick = {
                        selectedScreen = Screen.TransactionDetail
                    })

                    Screen.Categories -> CategoriesScreen()
                    Screen.Profile -> com.spendsnap.app.ui.profile.ProfileScreen(
                        onLogoutSuccess = { selectedScreen = Screen.Login }
                    )

                    Screen.TransactionDetail -> TransactionDetailScreen(onBack = {
                        selectedScreen = Screen.History
                    })

                    Screen.Camera -> CameraScreen()

                    else -> {}
                }
            }
        }
    }
}

@Composable
fun SpendSnapBottomNav(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .height(86.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
    ) {
        bottomNavItems.forEach { screen ->
            val isSelected = currentScreen == screen
            val isCamera = screen is Screen.Camera

            NavigationBarItem(
                selected = isSelected,
                onClick = { onScreenSelected(screen) },
                icon = {
                    if (isCamera) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .shadow(
                                    8.dp,
                                    CircleShape,
                                    ambientColor = MaterialTheme.colorScheme.primary,
                                    spotColor = MaterialTheme.colorScheme.primary
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = screen.icon!!),
                                contentDescription = screen.label,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Icon(
                            painter = painterResource(id = screen.icon!!),
                            contentDescription = screen.label,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = null,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = if (isCamera) Color.Unspecified else MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.outline,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
