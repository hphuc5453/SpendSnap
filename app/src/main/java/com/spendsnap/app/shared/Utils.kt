package com.spendsnap.app.shared

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.spendsnap.app.R
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object Utils {
    private val isoParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
    private val displayDateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    /**
     * Format số: 1,234,567.89
     */
    fun formatNumber(number: Double): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val formatter = DecimalFormat("#,###.##", symbols)
        return formatter.format(number)
    }

    /**
     * Format theo currency được user chọn (symbol từ CurrencyManager). Mặc định prefix.
     * vd USD: "$1,234.56"; VND: "₫1,234"
     */
    fun formatCurrency(context: android.content.Context, value: Double): String {
        val symbol = com.spendsnap.app.data.local.CurrencyManager.getSavedSymbol(context)
        return "$symbol${formatNumber(value)}"
    }

    /**
     * Format thời gian tương đối:
     *  - < 1 giờ  → "Xm ago"
     *  - < 1 ngày → "Xh ago"
     *  - < 1 tuần → "X days ago"
     *  - else     → "dd/MM/yyyy"
     *
     *  Input: ISO-8601 string (vd "2026-05-12T10:00:00.000Z"). Nếu parse fail trả lại nguyên gốc.
     */
    fun formatRelativeDate(context: Context, iso: String): String {
        val date: Date = runCatching { isoParser.parse(iso) }.getOrNull() ?: return iso
        val diffMs = (System.currentTimeMillis() - date.time).coerceAtLeast(0L)
        val minutes = (diffMs / 60_000L).toInt()
        val hours = (diffMs / 3_600_000L).toInt()
        val days = (diffMs / 86_400_000L).toInt()
        return when {
            hours < 1 -> context.getString(R.string.date_minutes_ago, minutes)
            days < 1 -> context.getString(R.string.date_hours_ago, hours)
            days < 7 -> context.getString(R.string.date_days_ago, days)
            else -> displayDateFormatter.format(date)
        }
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
