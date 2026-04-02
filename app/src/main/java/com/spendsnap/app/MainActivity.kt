package com.spendsnap.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.spendsnap.app.ui.ExpenseViewModel
import com.spendsnap.app.ui.ExpenseViewModelFactory
import com.spendsnap.app.ui.Screen
import com.spendsnap.app.ui.bottomNavItems
import com.spendsnap.app.ui.camera.CameraScreen
import com.spendsnap.app.ui.categories.CategoriesScreen
import com.spendsnap.app.ui.history.HistoryScreen
import com.spendsnap.app.ui.history.TransactionDetailScreen
import com.spendsnap.app.ui.home.HomeScreen
import com.spendsnap.app.ui.theme.SpendSnapTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory((application as App).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpendSnapTheme {
                MainScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: ExpenseViewModel) {
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val expenses by viewModel.allExpenses.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Only show bottom bar for main screens, hide for detail screen
            if (selectedScreen != Screen.TransactionDetail) {
                SpendSnapBottomNav(
                    currentScreen = selectedScreen,
                    onScreenSelected = { selectedScreen = it }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedScreen) {
                Screen.Home -> HomeScreen()
                Screen.History -> HistoryScreen(onTransactionClick = { selectedScreen = Screen.TransactionDetail })
                Screen.Categories -> CategoriesScreen()
                Screen.Profile -> com.spendsnap.app.ui.profile.ProfileScreen()
                Screen.TransactionDetail -> TransactionDetailScreen(onBack = { selectedScreen = Screen.History })
                Screen.Camera -> CameraScreen(
                    onPhotoCaptured = { uri, amount ->
                        viewModel.addExpense(amount, uri)
                        selectedScreen = Screen.Home
                    }
                )
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
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.height(80.dp)
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
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = screen.icon!!,
                                contentDescription = screen.label,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = screen.icon!!,
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
