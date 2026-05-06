package com.spendsnap.app.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendsnap.app.R
import com.spendsnap.app.data.remote.models.CategoryResponse
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.ui.components.HeaderSection
import com.spendsnap.app.view_models.CategoryViewModel

private val categoryIcons = listOf(
    Icons.Default.Restaurant,
    Icons.Default.ShoppingBag,
    Icons.Default.DirectionsCar,
    Icons.Default.CardGiftcard,
    Icons.Default.FitnessCenter,
    Icons.Default.Movie,
    Icons.Default.Work,
    Icons.Default.Home,
    Icons.Default.Favorite,
    Icons.Default.Star
)

private val categoryColors = listOf(
    Color(0xFFD1FF26),
    Color(0xFFFF5C00),
    Color(0xFFFFEB3B),
    Color(0xFF80DEEA),
    Color(0xFFCE93D8),
    Color(0xFFA5D6A7),
    Color(0xFFEF9A9A),
    Color(0xFFFFCC80),
    Color(0xFF90CAF9),
    Color(0xFFF48FB1)
)

data class Category(
    val id: Int = 0,
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val type: String = "EXPENSE",
    val limitBudget: Double = 0.0
)

fun CategoryResponse.toCategory(index: Int) = Category(
    id = id,
    name = name,
    icon = categoryIcons[index % categoryIcons.size],
    color = categoryColors[index % categoryColors.size],
    type = type,
    limitBudget = limitBudget
)

@Composable
fun CategoriesScreen(
    modifier: Modifier = Modifier,
    onNavigateToAddNewCategory: () -> Unit = {},
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val categoriesState by viewModel.categoriesState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getCategories()
    }

    val categories = when (val state = categoriesState) {
        is ApiResult.Success -> state.data.mapIndexed { index, response -> response.toCategory(index) }
        else -> emptyList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text(
            text = stringResource(R.string.title_categories),
            style = MaterialTheme.typography.displayMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.subtitle_categories),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        MostUsedCategoryCard(categories.firstOrNull())

        Spacer(modifier = Modifier.height(24.dp))

        when (val state = categoriesState) {
            is ApiResult.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is ApiResult.Error -> {
                Text(
                    text = state.exception.message ?: "Lỗi tải danh mục",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            else -> {
                CategoryGrid(categories = categories, onNavigateToAddNewCategory = onNavigateToAddNewCategory)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        CategoryLimitsSection(categories = categories.filter { it.limitBudget > 0 })

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun MostUsedCategoryCard(mostUsed: Category?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6D04D))
    ) {
        Row(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.label_most_used),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
                Text(
                    text = mostUsed?.name ?: "—",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (mostUsed != null) mostUsed.type else "No categories yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }
            Icon(
                mostUsed?.icon ?: Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Black.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun CategoryGrid(categories: List<Category>, onNavigateToAddNewCategory: () -> Unit = {}) {
    Column {
        for (i in categories.indices step 2) {
            Row(modifier = Modifier.fillMaxWidth()) {
                CategoryItem(categories[i], modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(16.dp))
                if (i + 1 < categories.size) {
                    CategoryItem(categories[i + 1], modifier = Modifier.weight(1f))
                } else {
                    NewCategoryItem(modifier = Modifier.weight(1f), onNavigateToAddNewCategory = onNavigateToAddNewCategory)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            if (categories.isEmpty() || categories.size % 2 == 0) {
                NewCategoryItem(modifier = Modifier.weight(1f), onNavigateToAddNewCategory = onNavigateToAddNewCategory)
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun CategoryItem(category: Category, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(category.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(category.icon, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = category.name, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = category.type,
                color = Color.Gray,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun NewCategoryItem(modifier: Modifier = Modifier, onNavigateToAddNewCategory: () -> Unit = {}) {
    Card(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        onClick = onNavigateToAddNewCategory,
        border = null
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(R.string.label_new_category), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun CategoryLimitsSection(categories: List<Category> = emptyList()) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.title_category_limits),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { }) {
                Text(text = stringResource(R.string.btn_manage_all), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (categories.isEmpty()) {
            Text(
                text = "No budget limits set",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            categories.forEach { category ->
                CategoryLimitItem(category = category)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun CategoryLimitItem(category: Category) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF2C2C2E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(category.icon, contentDescription = null, tint = category.color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = category.name, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(text = "$${category.limitBudget.toInt()}", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Text(text = category.type, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = 0f,
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    color = category.color,
                    trackColor = Color.Gray.copy(alpha = 0.2f)
                )
            }
        }
    }
}
