package com.spendsnap.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.spendsnap.app.R
import com.spendsnap.app.data.remote.models.TransactionResponse
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.shared.Constants
import com.spendsnap.app.shared.Utils
import com.spendsnap.app.view_models.CategoryViewModel
import com.spendsnap.app.view_models.TransactionViewModel
import com.spendsnap.app.ui.components.HeaderSection
import com.spendsnap.app.ui.components.LoadingDialog

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    onTransactionClick: (TransactionResponse) -> Unit = {},
    viewModel: TransactionViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val transactionsState by viewModel.transactionsState.collectAsState()
    val categoryIconsState by categoryViewModel.categoryIconsState.collectAsState()

    // Gọi API khi màn hình được tạo
    LaunchedEffect(Unit) {
        viewModel.getTransactions()
        categoryViewModel.getCategoryIcons()
    }

    val iconMap = (categoryIconsState as? ApiResult.Success)?.data
        ?.associate { it.slug to it.icon }
        .orEmpty()

    LoadingDialog(isLoading = transactionsState is ApiResult.Loading)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {

        val successData = (transactionsState as? ApiResult.Success)?.data
        MoneyLeftCard(totalSpent = successData?.totalSpent ?: 0.0)

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.history_recent_moments),
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.history_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        when (val state = transactionsState) {
            is ApiResult.Success -> {
                val transactions = state.data.transactions
                if (transactions.isEmpty()) {
                    EmptyState()
                } else {
                    MomentsGrid(transactions, iconMap, onTransactionClick)
                }
            }
            is ApiResult.Error -> {
                Text(
                    text = stringResource(R.string.history_load_error, state.exception.message),
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            else -> {}
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun MoneyLeftCard(totalSpent: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6D04D)) // Yellow
    ) {
        Box(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
            Column {
                Text(
                    text = stringResource(R.string.history_total_spent_month),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
                Text(
                    text = Utils.formatCurrency(LocalContext.current, totalSpent),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.history_doing_great),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.6f)
                )
            }
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2C2C2E))
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Home,
                    contentDescription = null,
                    tint = Color(0xFFD1FF26)
                )
            }
        }
    }
}

@Composable
fun MomentsGrid(
    transactions: List<TransactionResponse>,
    iconMap: Map<String, String>,
    onTransactionClick: (TransactionResponse) -> Unit
) {
    Column {
        // Chúng ta sẽ lặp qua danh sách và hiển thị theo pattern: 2 nhỏ - 1 lớn - 2 nhỏ
        transactions.chunked(5).forEach { group ->
            // Row 1: 2 items nhỏ
            if (group.size >= 1) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    MomentItem(
                        transaction = group[0],
                        iconMap = iconMap,
                        modifier = Modifier.weight(1f).height(180.dp).clickable { onTransactionClick(group[0]) }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    if (group.size >= 2) {
                        MomentItem(
                            transaction = group[1],
                            iconMap = iconMap,
                            modifier = Modifier.weight(1f).height(180.dp).clickable { onTransactionClick(group[1]) }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // Row 2: 1 item lớn
            if (group.size >= 3) {
                Spacer(modifier = Modifier.height(16.dp))
                MomentItem(
                    transaction = group[2],
                    iconMap = iconMap,
                    isLarge = true,
                    modifier = Modifier.fillMaxWidth().height(300.dp).clickable { onTransactionClick(group[2]) }
                )
            }

            // Row 3: 2 items nhỏ tiếp theo
            if (group.size >= 4) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    MomentItem(
                        transaction = group[3],
                        iconMap = iconMap,
                        modifier = Modifier.weight(1f).height(180.dp).clickable { onTransactionClick(group[3]) }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    if (group.size >= 5) {
                        MomentItem(
                            transaction = group[4],
                            iconMap = iconMap,
                            modifier = Modifier.weight(1f).height(180.dp).clickable { onTransactionClick(group[4]) }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MomentItem(
    transaction: TransactionResponse,
    iconMap: Map<String, String>,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Hiển thị ảnh thật từ Backend
            if (!transaction.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = transaction.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = Utils.formatCurrency(LocalContext.current, transaction.amount),
                    style = if (isLarge) MaterialTheme.typography.displaySmall else MaterialTheme.typography.headlineSmall,
                    color = Color(0xFFD1FF26),
                    fontWeight = FontWeight.ExtraBold
                )

                val category = transaction.categoryId
                val emoji = category?.icon?.let { iconMap[it] } ?: Constants.FALLBACK_ICON
                val categoryName = category?.name ?: stringResource(R.string.history_unknown_category)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = emoji, fontSize = if (isLarge) 16.sp else 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isLarge) 14.sp else 11.sp
                    )
                }

                val context = LocalContext.current
                Text(
                    text = Utils.formatRelativeDate(context, transaction.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(stringResource(R.string.history_empty), color = Color.Gray)
    }
}
