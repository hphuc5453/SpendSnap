package com.spendsnap.app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendsnap.app.data.remote.models.BreakdownItemResponse
import com.spendsnap.app.data.remote.models.StatisticsOverviewResponse
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.view_models.CategoryViewModel
import com.spendsnap.app.view_models.StatisticsViewModel
import java.util.Locale

private val fallbackColors = listOf(
    Color(0xFFD1FF26),
    Color(0xFFFF5C00),
    Color(0xFFFFEB3B),
    Color(0xFF80DEEA),
    Color(0xFFCE93D8),
    Color(0xFFA5D6A7),
    Color(0xFFEF9A9A),
    Color(0xFFFFCC80)
)

private const val FALLBACK_ICON = "📦"

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    statisticsViewModel: StatisticsViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val overviewState by statisticsViewModel.overviewState.collectAsState()
    val categoryIconsState by categoryViewModel.categoryIconsState.collectAsState()

    LaunchedEffect(Unit) {
        statisticsViewModel.getOverview()
        categoryViewModel.getCategoryIcons()
    }

    val iconMap = (categoryIconsState as? ApiResult.Success)?.data
        ?.associate { it.slug to it.icon }
        .orEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = "Stats",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            when (val state = overviewState) {
                is ApiResult.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is ApiResult.Error -> {
                    Text(
                        text = state.exception.message ?: "Lỗi tải dữ liệu",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                is ApiResult.Success -> {
                    OverviewContent(overview = state.data, iconMap = iconMap)
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun OverviewContent(
    overview: StatisticsOverviewResponse,
    iconMap: Map<String, String>
) {
    TotalSpentCard(totalSpent = overview.totalSpent, changePercent = overview.spentChangePercent)

    Spacer(modifier = Modifier.height(32.dp))

    DonutChartSection(breakdown = overview.breakdown, monthLabel = monthLabel(overview.yearMonth))

    Spacer(modifier = Modifier.height(32.dp))

    RemainingBudgetCard(
        remainingBudget = overview.remainingBudget,
        safeToSpendPerDay = overview.safeToSpendPerDay
    )

    Spacer(modifier = Modifier.height(32.dp))

    overview.insights.topImprovement?.let { improvement ->
        Text(
            text = "You're spending less on ${improvement.name}. That's ${formatCurrency(improvement.savedAmount)} saved this month.",
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }

    Text(
        text = "Breakdown",
        style = MaterialTheme.typography.titleLarge,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    if (overview.breakdown.isEmpty()) {
        Text(
            text = "No spending yet this month",
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    } else {
        overview.breakdown.forEachIndexed { index, item ->
            BreakdownItemView(
                item = item,
                iconBgColor = parseHexColor(item.color) ?: fallbackColors[index % fallbackColors.size],
                emoji = iconMap[item.icon] ?: FALLBACK_ICON
            )
        }
    }
}

@Composable
fun TotalSpentCard(totalSpent: Double, changePercent: Double?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TOTAL MONTHLY SPENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Text(
                    text = formatCurrency(totalSpent),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            if (changePercent != null) {
                val isUp = changePercent >= 0
                Surface(
                    color = if (isUp) Color(0xFFFFCDD2) else MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isUp) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f%%", kotlin.math.abs(changePercent)),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DonutChartSection(breakdown: List<BreakdownItemResponse>, monthLabel: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(240.dp)) {
            val strokeWidth = 40.dp.toPx()

            if (breakdown.isEmpty()) {
                drawArc(
                    color = Color(0xFF2C2C2E),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            } else {
                val totalPercentage = breakdown.sumOf { it.percentage }.coerceAtLeast(0.0)
                val gapDeg = if (breakdown.size > 1) 4f else 0f
                val usableArc = 360f - gapDeg * breakdown.size
                var startAngle = -90f
                breakdown.forEachIndexed { index, item ->
                    val fraction = if (totalPercentage > 0) (item.percentage / totalPercentage).toFloat() else 0f
                    val sweep = (usableArc * fraction).coerceAtLeast(0f)
                    val color = parseHexColor(item.color) ?: fallbackColors[index % fallbackColors.size]
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += sweep + gapDeg
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "SPENT", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Text(
                text = monthLabel,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun RemainingBudgetCard(remainingBudget: Double, safeToSpendPerDay: Double?) {
    val isOverBudget = remainingBudget < 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverBudget) Color(0xFFFF8A80) else Color(0xFFFFD54F)
        )
    ) {
        Box(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
            Column {
                Text(
                    text = if (isOverBudget) "OVER BUDGET" else "REMAINING BUDGET",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
                Text(
                    text = formatCurrency(kotlin.math.abs(remainingBudget)),
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                if (safeToSpendPerDay != null && !isOverBudget) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = Color.Black.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            text = "Safe to spend today: ${formatCurrency(safeToSpendPerDay)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Black.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BreakdownItemView(
    item: BreakdownItemResponse,
    iconBgColor: Color,
    emoji: String
) {
    Card(
        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    text = String.format(Locale.US, "%.0f%% of spending", item.percentage),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = formatCurrency(item.amount), style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                val change = item.changePercent
                if (change != null) {
                    val trendColor = if (change >= 0) Color(0xFFEF5350) else MaterialTheme.colorScheme.primary
                    val sign = if (change >= 0) "+" else ""
                    Text(
                        text = String.format(Locale.US, "$sign%.1f%%", change),
                        style = MaterialTheme.typography.labelSmall,
                        color = trendColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatCurrency(value: Double): String =
    String.format(Locale.US, "$%,.2f", value)

private fun monthLabel(yearMonth: String): String {
    val months = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
    val m = yearMonth.split("-").getOrNull(1)?.toIntOrNull() ?: return ""
    return months.getOrElse(m - 1) { "" }
}

private fun parseHexColor(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    return runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrNull()
}
