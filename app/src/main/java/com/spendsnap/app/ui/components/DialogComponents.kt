package com.spendsnap.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

enum class DialogType { Success, Error }

/**
 * Dialog hiển thị vòng quay loading không cho phép tắt bằng cách nhấn bên ngoài
 */
@Composable
fun LoadingDialog(isLoading: Boolean) {
    if (isLoading) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.DarkGray.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp))
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

/**
 * Dialog dùng chung cho cả thông báo Thành công và Lỗi.
 * Có icon tròn primary/đỏ, title, message, button primary, optional secondary action / footer.
 */
@Composable
fun AppStatusDialog(
    show: Boolean,
    type: DialogType,
    title: String,
    message: String,
    onDismiss: () -> Unit,
    confirmText: String = "ĐÓNG",
    secondaryText: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    footer: @Composable (() -> Unit)? = null
) {
    if (!show) return

    val primary = MaterialTheme.colorScheme.primary
    val errorRed = Color(0xFFFF4D4F)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111111))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon halo + icon
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            (if (type == DialogType.Success) primary else errorRed)
                                .copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(if (type == DialogType.Success) primary else errorRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (type == DialogType.Success) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (type == DialogType.Success) Color.Black else Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    Text(
                        text = confirmText,
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (secondaryText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { onSecondaryClick?.invoke() }) {
                        Text(
                            text = secondaryText,
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                if (footer != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    footer()
                }
            }
        }
    }
}
