package com.spendsnap.app.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendsnap.app.R
import com.spendsnap.app.data.local.LanguageManager
import com.spendsnap.app.ui.shared.HeaderSection
import com.spendsnap.app.view_models.UserViewModel

private data class Language(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val flag: String
)

private val supportedLanguages = listOf(
    Language("en", "English", "English", "🇺🇸"),
    Language("vi", "Tiếng Việt", "Vietnamese", "🇻🇳")
//    Language("fr", "Français", "French", "🇫🇷"),
//    Language("es", "Español", "Spanish", "🇪🇸"),
//    Language("ja", "日本語", "Japanese", "🇯🇵"),
//    Language("ko", "한국어", "Korean", "🇰🇷"),
//    Language("zh", "中文", "Chinese", "🇨🇳"),
//    Language("de", "Deutsch", "German", "🇩🇪"),
//    Language("pt", "Português", "Portuguese", "🇧🇷"),
)

@Composable
fun SettingsLanguageScreen(
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onLanguageChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val currentLanguage = remember { LanguageManager.getSavedLanguage(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HeaderSection(stringResource(R.string.settings_language), onBack)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 0.dp)
        ) {
            items(supportedLanguages) { language ->
                LanguageItem(
                    language = language,
                    isSelected = currentLanguage == language.code,
                    onClick = {
                        LanguageManager.saveLanguage(context, language.code)
                        userViewModel.updateLanguage(language.code)
                        onLanguageChanged(language.code)
                    }
                )
            }
        }
    }
}

@Composable
private fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF2C2C2E) else Color(0xFF1A1A1A)
        ),
        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3A3A3C)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = language.flag,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = language.nativeName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = language.englishName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
