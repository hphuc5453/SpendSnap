package com.spendsnap.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendsnap.app.R
import com.spendsnap.app.data.local.CurrencyManager
import com.spendsnap.app.data.remote.models.CurrencyResponse
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.ui.shared.HeaderSection
import com.spendsnap.app.view_models.CurrencyViewModel
import com.spendsnap.app.view_models.UserViewModel

@Composable
fun SettingsCurrencyScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onCurrencyChanged: (String) -> Unit = {},
    viewModel: CurrencyViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currenciesState by viewModel.currenciesState.collectAsState()

    var selectedCode by remember { mutableStateOf(CurrencyManager.getSavedCurrency(context)) }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.getCurrencies()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HeaderSection(stringResource(R.string.settings_currency), onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            SearchBar(query = query, onQueryChange = { query = it })

            Spacer(modifier = Modifier.height(20.dp))

            when (val state = currenciesState) {
                is ApiResult.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is ApiResult.Error -> {
                    Text(
                        text = state.exception.message ?: stringResource(R.string.currency_load_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                is ApiResult.Success -> {
                    CurrencyList(
                        all = state.data,
                        query = query,
                        selectedCode = selectedCode,
                        onSelect = { selectedCode = it }
                    )
                }
                else -> {}
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            val all = (currenciesState as? ApiResult.Success)?.data.orEmpty()
            val selectedCurrency = all.firstOrNull { it.code == selectedCode }
            Button(
                onClick = {
                    val symbol = selectedCurrency?.symbol ?: "$"
                    CurrencyManager.saveCurrency(context, selectedCode, symbol)
                    userViewModel.updateCurrency(selectedCode)
                    onCurrencyChanged(selectedCode)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = stringResource(R.string.btn_save_selection),
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        placeholder = { Text(stringResource(R.string.currency_search_hint), color = Color.Gray) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1C1C1E),
            unfocusedContainerColor = Color(0xFF1C1C1E),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
private fun CurrencyList(
    all: List<CurrencyResponse>,
    query: String,
    selectedCode: String,
    onSelect: (String) -> Unit
) {
    val filter = query.trim()
    val matches = if (filter.isEmpty()) all
    else all.filter {
        it.code.contains(filter, ignoreCase = true) ||
            it.name.contains(filter, ignoreCase = true)
    }

    val selected = matches.firstOrNull { it.code == selectedCode }
    val others = matches.filter { it.code != selectedCode }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        if (selected != null) {
            item {
                SectionLabel(stringResource(R.string.currency_current_selection))
                Spacer(modifier = Modifier.height(8.dp))
                CurrencyItem(
                    currency = selected,
                    isSelected = true,
                    onClick = { onSelect(selected.code) }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (others.isNotEmpty()) {
            item {
                SectionLabel(stringResource(R.string.currency_all_currencies))
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(others, key = { it.code }) { currency ->
                CurrencyItem(
                    currency = currency,
                    isSelected = false,
                    onClick = { onSelect(currency.code) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (matches.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.currency_empty_search),
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = Color.Gray,
        letterSpacing = 1.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun CurrencyItem(
    currency: CurrencyResponse,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1A1A1A) else Color(0xFF1A1A1A)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, primary.copy(alpha = 0.4f)) else null,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) primary else Color(0xFF2C2C2E)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currency.symbol,
                    color = if (isSelected) Color.Black else Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currency.code,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currency.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                        .clickable(onClick = onClick)
                )
            }
        }
    }
}
