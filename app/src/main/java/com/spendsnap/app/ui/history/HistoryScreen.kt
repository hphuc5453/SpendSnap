package com.spendsnap.app.ui.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendsnap.app.ui.components.HeaderSection

@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection(modifier = Modifier.padding(horizontal = 8.dp))

        MoneyLeftCard()

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Recent Moments",
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Your visual spending timeline",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Grid of Moments
        MomentsGrid()
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun MoneyLeftCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6D04D)) // Yellow
    ) {
        Box(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
            Column {
                Text(
                    text = "MONEY LEFT TO SPEND",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$1,240.50",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Daily Budget: $15.00",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.6f)
                )
            }
            
            // Trending Button
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
fun MomentsGrid() {
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            MomentItem(
                price = "$6.50",
                info = "COFFEE SHOP • 2H AGO",
                modifier = Modifier.weight(1f).height(180.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            MomentItem(
                price = "$899.00",
                info = "APPLE STORE • 5H AGO",
                modifier = Modifier.weight(1f).height(180.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        MomentItem(
            price = "$120.00",
            info = "NIKE FLAGSHIP • YESTERDAY",
            isLarge = true,
            modifier = Modifier.fillMaxWidth().height(300.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            MomentItem(
                price = "$45.00",
                info = "TOP GRILL • 2D AGO",
                modifier = Modifier.weight(1f).height(180.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            MomentItem(
                price = "$22.00",
                info = "PET SHOP • 3D AGO",
                modifier = Modifier.weight(1f).height(180.dp)
            )
        }
    }
}

@Composable
fun MomentItem(
    price: String,
    info: String,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Placeholder for Image (Black background with gradient)
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
                if (isLarge) {
                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "NEW ORDER",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 8.sp
                        )
                    }
                }
                
                Text(
                    text = price,
                    style = if (isLarge) MaterialTheme.typography.displaySmall else MaterialTheme.typography.headlineSmall,
                    color = Color(0xFFD1FF26),
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = info,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
