package com.spendsnap.app.shared

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object Utils {
    /**
     * Format số: 1,234,567.89
     */
    fun formatNumber(number: Double): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val formatter = DecimalFormat("#,###.##", symbols)
        return formatter.format(number)
    }

    /**
     * Wrapper để thêm hiệu ứng chuyển trang mượt mà
     */
    @Composable
    fun <T> AnimatedScreenTransition(
        targetState: T,
        modifier: Modifier = Modifier,
        content: @Composable (T) -> Unit
    ) {
        AnimatedContent(
            targetState = targetState,
            transitionSpec = {
                // Hiệu ứng Fade + Slide nhẹ từ dưới lên
                (fadeIn(animationSpec = tween(300)) + 
                 slideInVertically(animationSpec = tween(300), initialOffsetY = { 40 }))
                    .togetherWith(fadeOut(animationSpec = tween(200)))
            },
            label = "ScreenTransition",
            modifier = modifier
        ) { state ->
            content(state)
        }
    }
}
