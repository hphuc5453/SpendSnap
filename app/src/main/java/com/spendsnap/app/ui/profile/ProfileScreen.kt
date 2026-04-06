package com.spendsnap.app.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendsnap.app.ui.auth.AuthViewModel
import com.spendsnap.app.ui.components.HeaderSection

@Composable
fun ProfileScreen(modifier: Modifier = Modifier, viewModel: AuthViewModel = hiltViewModel(), onLogoutSuccess: () -> Unit) {

    val logoutState by viewModel.logoutState.collectAsState()

    val handleLogout = {
        viewModel.logout()
    }

    LaunchedEffect(logoutState) {
        when (logoutState) {
            true -> {
                onLogoutSuccess()
                viewModel.resetLoginState()
            }
            else -> {}
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection(modifier = Modifier.padding(horizontal = 8.dp))

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Info Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2C2C2E))
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF5C00)), // Secondary/Orange
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Alex Mercer",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "alex.mercer@viewfinder.io",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Money Left Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(40.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE6D04D)) // Yellow
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "MONEY LEFT TO SPEND",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$2,840.50",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "ACCOUNT SETTINGS",
            style = MaterialTheme.typography.labelLarge,
            color = Color.Gray,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Settings Items
        SettingsItem(
            icon = Icons.Default.Home,
            title = "Monthly Budget",
            subtitle = "Set your monthly spending limits"
        )
        SettingsItem(
            icon = Icons.Default.Home,
            title = "Linked Accounts",
            subtitle = "Manage banks and crypto wallets"
        )
        SettingsItem(
            icon = Icons.Default.Notifications,
            title = "Notification Settings",
            subtitle = "Alerts, updates, and reminders"
        )
        SettingsItem(
            icon = Icons.Default.Home,
            title = "Privacy & Security",
            subtitle = "2FA, password, and data privacy"
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Sign Out Button
        OutlinedButton(
            onClick = handleLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, Color(0xFFFF5C00).copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5C00))
        ) {
            Text(text = "SIGN OUT", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2C2C2E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Icon(
                Icons.Default.Home,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}
