package com.spendsnap.app.ui.categories

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendsnap.app.R
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.shared.Constants
import com.spendsnap.app.ui.components.AppStatusDialog
import com.spendsnap.app.ui.components.DialogType
import com.spendsnap.app.ui.shared.HeaderSection
import com.spendsnap.app.ui.shared.TextFieldCommon
import com.spendsnap.app.view_models.CategoryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddNewCategoryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val nameFieldRequester = remember { BringIntoViewRequester() }
    val budgetFieldRequester = remember { BringIntoViewRequester() }
    var categoryName by remember { mutableStateOf("") }
    var categoryNameError by remember { mutableStateOf("") }
    var selectedIconSlug by remember { mutableStateOf("") }
    var budgetAmount by remember { mutableStateOf("") }
    var budgetError by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(Constants.KIND_EXPENSE) }

    val createCategoryState by viewModel.createCategoryState.collectAsState()
    val categoryIconsState by viewModel.categoryIconsState.collectAsState()
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var apiErrorMessage by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.getCategoryIcons()
    }

    val icons = (categoryIconsState as? ApiResult.Success)?.data.orEmpty()

    LaunchedEffect(icons) {
        if (selectedIconSlug.isEmpty() && icons.isNotEmpty()) {
            selectedIconSlug = icons.first().slug
        }
    }

    LaunchedEffect(createCategoryState) {
        when (val state = createCategoryState) {
            is ApiResult.Success -> {
                showSuccessDialog = true
                viewModel.resetCreateState()
            }
            is ApiResult.Error -> {
                apiErrorMessage = state.exception.message ?: context.getString(R.string.cat_create_failed)
                showErrorDialog = true
                viewModel.resetCreateState()
            }
            else -> {}
        }
    }

    AppStatusDialog(
        show = showSuccessDialog,
        type = DialogType.Success,
        title = stringResource(R.string.dialog_success_title),
        message = stringResource(R.string.cat_create_success_message),
        onDismiss = { showSuccessDialog = false; onBack() }
    )

    AppStatusDialog(
        show = showErrorDialog,
        type = DialogType.Error,
        title = stringResource(R.string.dialog_error_title),
        message = apiErrorMessage,
        onDismiss = { showErrorDialog = false }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection(onBack = onBack, title = stringResource(R.string.btn_create_category))

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = modifier.fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            TextFieldCommon(
                label = stringResource(R.string.label_category_name),
                value = categoryName,
                onValueChange = { categoryName = it; if (categoryNameError.isNotEmpty()) categoryNameError = "" },
                placeholder = stringResource(R.string.placeholder_category_name),
                error = categoryNameError,
                modifier = Modifier.bringIntoViewRequester(nameFieldRequester)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.label_category_type),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    Constants.KIND_EXPENSE to R.string.tab_expense,
                    Constants.KIND_INCOME to R.string.tab_income
                ).forEach { (kind, labelRes) ->
                    val isSelected = selectedType == kind
                    Button(
                        onClick = { selectedType = kind },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1A1A1A),
                            contentColor = if (isSelected) Color.Black else Color.Gray
                        ),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(text = stringResource(labelRes), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.label_select_icon),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = categoryIconsState) {
                is ApiResult.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is ApiResult.Error -> {
                    Text(
                        text = state.exception.message ?: stringResource(R.string.cat_icons_load_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        icons.chunked(4).forEach { rowIcons ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                rowIcons.forEach { iconItem ->
                                    val isSelected = selectedIconSlug == iconItem.slug
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color(0xFFD1FF26) else Color(0xFF1A1A1A))
                                            .clickable { selectedIconSlug = iconItem.slug },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = iconItem.icon,
                                            fontSize = 28.sp
                                        )
                                    }
                                }
                                repeat(4 - rowIcons.size) {
                                    Spacer(modifier = Modifier.size(64.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.label_monthly_budget_limit),
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = budgetAmount,
                onValueChange = {
                    budgetAmount = it
                    if (budgetError.isNotEmpty()) budgetError = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(budgetFieldRequester),
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = {
                    Text(
                        text = stringResource(R.string.currency_symbol),
                        color = Color(0xFFD1FF26),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                },
                placeholder = {
                    Text(stringResource(R.string.placeholder_amount_zero_decimal), color = Color.Gray)
                },
                isError = budgetError.isNotEmpty(),
                supportingText = if (budgetError.isNotEmpty()) {
                    { Text(budgetError, color = MaterialTheme.colorScheme.error) }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFF2C2C2E),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = Color(0xFF1A1A1A),
                    unfocusedContainerColor = Color(0xFF1A1A1A)
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.hint_monthly_budget),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OptionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.FlashOn,
                    iconColor = Color(0xFFFF5C00),
                    label = stringResource(R.string.label_smart_alerts),
                    value = stringResource(R.string.value_smart_alerts)
                )
                OptionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Visibility,
                    iconColor = Color(0xFFFFEB3B),
                    label = stringResource(R.string.label_visibility),
                    value = stringResource(R.string.value_visibility)
                )
            }

            if (validationError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = validationError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(40.dp))

            val isLoading = createCategoryState is ApiResult.Loading
            Button(
                onClick = {
                    var valid = true
                    var firstErrorRequester: BringIntoViewRequester? = null
                    if (categoryName.isBlank()) {
                        categoryNameError = context.getString(R.string.error_category_name_required)
                        if (firstErrorRequester == null) firstErrorRequester = nameFieldRequester
                        valid = false
                    }
                    val budget = budgetAmount.toDoubleOrNull()
                    if (budgetAmount.isNotEmpty() && budget == null) {
                        budgetError = context.getString(R.string.error_invalid_amount)
                        if (firstErrorRequester == null) firstErrorRequester = budgetFieldRequester
                        valid = false
                    }
                    if (selectedIconSlug.isEmpty()) {
                        validationError = context.getString(R.string.error_select_icon)
                        valid = false
                    }
                    if (valid) {
                        validationError = ""
                        viewModel.createCategory(
                            name = categoryName.trim(),
                            kind = selectedType,
                            icon = selectedIconSlug
                        )
                    } else {
                        firstErrorRequester?.let { requester ->
                            scope.launch { requester.bringIntoView() }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD1FF26)),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.btn_create_category),
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun OptionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
