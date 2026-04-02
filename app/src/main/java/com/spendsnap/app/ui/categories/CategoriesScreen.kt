package com.spendsnap.app.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendsnap.app.ui.components.HeaderSection

@Composable
fun CategoriesScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {

        HeaderSection(modifier = Modifier.padding(horizontal = (8).dp)) // Adjust for parent padding

        Text(
            text = "Categories",
            style = MaterialTheme.typography.displayMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Organize your financial snapshots.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        MostUsedCategoryCard()

        Spacer(modifier = Modifier.height(24.dp))

        // Grid of Categories
        // Since we are inside a verticalScroll, we can't use a full LazyVerticalGrid directly without fixed height or nested scroll issues.
        // We can use a custom FlowRow or just Columns/Rows for a small set of items.
        CategoryGrid()

        Spacer(modifier = Modifier.height(32.dp))

        CategoryLimitsSection()
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun MostUsedCategoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6D04D)) // Yellowish-gold
    ) {
        Row(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MOST USED",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Food & Drink",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "24% of monthly spend",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }
            Icon(
                Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Black.copy(alpha = 0.2f)
            )
        }
    }
}

data class Category(val name: String, val icon: ImageVector, val items: Int, val color: Color)

@Composable
fun CategoryGrid() {
    val categories = listOf(
        Category("Dining", Icons.Default.Home, 12, Color(0xFFD1FF26)),
        Category("Transport", Icons.Default.Home, 8, Color(0xFFFF5C00)),
        Category("Gifts", Icons.Default.Home, 5, Color(0xFFFFEB3B)),
        Category("Groceries", Icons.Default.Home, 22, Color(0xFFD1FF26)),
        Category("Health", Icons.Default.Favorite, 2, Color(0xFFFF5C00)),
        Category("Fun", Icons.Default.Home, 14, Color(0xFFE1F5FE))
    )

    Column {
        for (i in categories.indices step 2) {
            Row(modifier = Modifier.fillMaxWidth()) {
                CategoryItem(categories[i], modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(16.dp))
                if (i + 1 < categories.size) {
                    CategoryItem(categories[i + 1], modifier = Modifier.weight(1f))
                } else {
                    NewCategoryItem(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (categories.size % 2 == 0) {
            Row(modifier = Modifier.fillMaxWidth()) {
                NewCategoryItem(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.weight(1f).width(16.dp))
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
            Text(text = "${category.items} ITEMS", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun NewCategoryItem(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        border = null // Could add dashed border here with custom modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "NEW", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun CategoryLimitsSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Category Limits",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { }) {
                Text(text = "MANAGE ALL", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
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
                    Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFFFF5C00))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Dining", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(text = "$450", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Text(text = "90% of budget reached", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = 0.9f,
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                        color = Color(0xFFFF5C00),
                        trackColor = Color.Gray.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}
