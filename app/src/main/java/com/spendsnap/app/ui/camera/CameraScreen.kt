package com.spendsnap.app.ui.camera

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.spendsnap.app.R
import com.spendsnap.app.data.local.CurrencyManager
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.shared.Constants
import com.spendsnap.app.ui.components.AppStatusDialog
import com.spendsnap.app.ui.components.DialogType
import com.spendsnap.app.ui.components.LoadingDialog
import com.spendsnap.app.view_models.CategoryViewModel
import com.spendsnap.app.view_models.TransactionViewModel
import kotlinx.coroutines.delay
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    viewModel: TransactionViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    var hasCameraPermission by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    val createTransactionState by viewModel.createTransactionState.collectAsState()
    val categoriesState by categoryViewModel.categoriesState.collectAsState()
    val categoryIconsState by categoryViewModel.categoryIconsState.collectAsState()
    val isLoading = createTransactionState is ApiResult.Loading

    var showErrorDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }
    var selectedKind by remember { mutableStateOf(Constants.KIND_EXPENSE) }
    var capturedUri by remember { mutableStateOf<Uri?>(null) }
    var amountText by remember { mutableStateOf("") }
    var isCapturing by remember { mutableStateOf(false) }

    LaunchedEffect(capturedUri) {
        if (capturedUri != null && isCapturing) {
            delay(250)
            isCapturing = false
        }
    }

    LoadingDialog(isLoading = isLoading || isCapturing)

    AppStatusDialog(
        show = showErrorDialog,
        type = DialogType.Error,
        title = stringResource(R.string.dialog_error_title),
        message = errorMessage,
        onDismiss = { showErrorDialog = false }
    )

    AppStatusDialog(
        show = showSuccessDialog,
        type = DialogType.Success,
        title = stringResource(R.string.dialog_success_title),
        message = stringResource(R.string.tx_success_message),
        onDismiss = {
            showSuccessDialog = false
            capturedUri = null
            amountText = ""
        }
    )

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
        categoryViewModel.getCategories()
        categoryViewModel.getCategoryIcons()
    }

    val allCategories = (categoriesState as? ApiResult.Success)?.data.orEmpty()
    val iconMap = (categoryIconsState as? ApiResult.Success)?.data
        ?.associate { it.slug to it.icon }
        .orEmpty()
    val filteredCategories = allCategories.filter { it.kind == selectedKind }

    LaunchedEffect(selectedKind, filteredCategories) {
        val stillValid = filteredCategories.any { it.id == selectedCategoryId }
        if (!stillValid) {
            selectedCategoryId = filteredCategories.firstOrNull()?.id.orEmpty()
        }
    }

    LaunchedEffect(createTransactionState) {
        when (val state = createTransactionState) {
            is ApiResult.Error -> {
                errorMessage = state.exception.message
                showErrorDialog = true
                viewModel.resetCreateState()
            }

            is ApiResult.Success -> {
                showSuccessDialog = true
                viewModel.resetCreateState()
            }

            else -> {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column {
            if (hasCameraPermission) {
                if (capturedUri == null) {
                    CameraView(
                        executor = cameraExecutor,
                        onCaptureStart = { isCapturing = true },
                        onImageCaptured = { capturedUri = it },
                        onError = {
                            isCapturing = false
                            Log.e("CameraScreen", "Error", it)
                        }
                    )
                } else {
                    CapturePreview(
                        uri = capturedUri!!,
                        amount = amountText,
                        onAmountChange = { amountText = it },
                        categories = filteredCategories,
                        iconMap = iconMap,
                        selectedCategoryId = selectedCategoryId,
                        onCategorySelected = { selectedCategoryId = it },
                        selectedKind = selectedKind,
                        onKindSelected = { selectedKind = it },
                        onConfirm = {
                            val amountValue = amountText.toDoubleOrNull()
                            if (amountValue == null || amountValue <= 0) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.validate_amount_invalid),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@CapturePreview
                            }
                            if (selectedCategoryId.isEmpty()) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.validate_select_category),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@CapturePreview
                            }
                            val imageFile =
                                capturedUri?.path?.let { File(it) }?.takeIf { it.exists() }
                            viewModel.createTransaction(
                                amount = amountValue,
                                categoryId = selectedCategoryId,
                                currency = CurrencyManager.getSavedCurrency(context),
                                imageFile = imageFile
                            )
                        },
                        onRetake = {
                            capturedUri = null
                            amountText = ""
                        }
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.camera_permission_required), color = Color.White)
                }
            }
        }
    }
}

@Composable
fun CameraView(
    executor: ExecutorService,
    onCaptureStart: () -> Unit = {},
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    DisposableEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        var boundProvider: ProcessCameraProvider? = null
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            boundProvider = cameraProvider
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e("CameraView", "Binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            boundProvider?.unbindAll()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.85f)
                .clip(RoundedCornerShape(32.dp))
                .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
        ) {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
            CornerMarkers(MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = {
                onCaptureStart()
                takePhoto(context, imageCapture, executor, onImageCaptured, onError)
            },
            modifier = Modifier
                .padding(bottom = 40.dp)
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                painterResource(R.drawable.outline_photo_camera_24),
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = Color.Black
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapturePreview(
    uri: Uri,
    amount: String,
    onAmountChange: (String) -> Unit,
    categories: List<com.spendsnap.app.data.remote.models.CategoryResponse>,
    iconMap: Map<String, String>,
    selectedCategoryId: String,
    onCategorySelected: (String) -> Unit,
    selectedKind: String,
    onKindSelected: (String) -> Unit,
    onConfirm: () -> Unit,
    onRetake: () -> Unit
) {
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = true,
        confirmValueChange = { it == SheetValue.Expanded }
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 96.dp,
        sheetContainerColor = Color(0xFF0E0E10),
        sheetContentColor = Color.White,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                KindTabs(selectedKind = selectedKind, onKindSelected = onKindSelected)

                Spacer(modifier = Modifier.height(10.dp))

                CategoryPicker(
                    categories = categories,
                    iconMap = iconMap,
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelected = onCategorySelected
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayAmount = formatAmountInput(amount)
                    val amountTextStyle = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold, fontSize = 24.sp
                    )
                    Text(
                        text = displayAmount.ifEmpty { stringResource(R.string.placeholder_amount_zero) },
                        style = amountTextStyle,
                        color = if (displayAmount.isEmpty()) Color.White.copy(alpha = 0.2f) else Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = CurrencyManager.getSavedSymbol(LocalContext.current),
                        style = amountTextStyle,
                        color = Color.White
                    )
                }

                NumericKeypad(
                    value = amount,
                    onValueChange = onAmountChange,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextButton(onClick = onRetake, modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.btn_retake_photo),
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(2f)
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            stringResource(R.string.btn_confirm),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.85f)
                    .clip(RoundedCornerShape(32.dp))
                    .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun NumericKeypad(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxLength: Int = Constants.MAX_AMOUNT_LENGTH
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "BACKSPACE")
    )

    fun handleKey(key: String) {
        when {
            key == "BACKSPACE" -> if (value.isNotEmpty()) onValueChange(value.dropLast(1))
            key == "." && value.contains(".") -> Unit
            value.length >= maxLength -> Unit
            else -> onValueChange(value + key)
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (key == "BACKSPACE") Color.Transparent else Color(
                                    0xFF1C1C1E
                                )
                            )
                            .clickable { handleKey(key) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (key == "BACKSPACE") {
                            Icon(
                                Icons.Default.Backspace,
                                contentDescription = null,
                                tint = Color.White
                            )
                        } else {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KindTabs(selectedKind: String, onKindSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1C1C1E))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf(
            Constants.KIND_EXPENSE to R.string.tab_expense,
            Constants.KIND_INCOME to R.string.tab_income
        ).forEach { (kind, labelRes) ->
            val isSelected = kind == selectedKind
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onKindSelected(kind) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(labelRes),
                    color = if (isSelected) Color.Black else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun CategoryPicker(
    categories: List<com.spendsnap.app.data.remote.models.CategoryResponse>,
    iconMap: Map<String, String>,
    selectedCategoryId: String,
    onCategorySelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.label_category),
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
    }
    Spacer(modifier = Modifier.height(8.dp))

    if (categories.isEmpty()) {
        Text(
            text = stringResource(R.string.label_no_category),
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            categories.forEach { category ->
                val isSelected = category.id == selectedCategoryId
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onCategorySelected(category.id) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else Color(
                                    0xFF1C1C1E
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = iconMap[category.icon] ?: Constants.FALLBACK_ICON,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = category.name.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CornerMarkers(color: Color) {
    Canvas(modifier = Modifier
        .fillMaxSize()
        .padding(20.dp)) {
        val strokeWidth = 10f
        val cornerSize = 50f
        // TL
        drawPath(path = Path().apply {
            moveTo(0f, cornerSize); lineTo(0f, 0f); lineTo(
            cornerSize,
            0f
        )
        }, color = color, style = Stroke(strokeWidth))
        // TR
        drawPath(path = Path().apply {
            moveTo(size.width - cornerSize, 0f); lineTo(
            size.width,
            0f
        ); lineTo(size.width, cornerSize)
        }, color = color, style = Stroke(strokeWidth))
        // BL
        drawPath(path = Path().apply {
            moveTo(0f, size.height - cornerSize); lineTo(
            0f,
            size.height
        ); lineTo(cornerSize, size.height)
        }, color = color, style = Stroke(strokeWidth))
        // BR
        drawPath(path = Path().apply {
            moveTo(
                size.width - cornerSize,
                size.height
            ); lineTo(size.width, size.height); lineTo(size.width, size.height - cornerSize)
        }, color = color, style = Stroke(strokeWidth))
    }
}

private val thousandsFormatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))

/**
 * Format raw digits input thành chuỗi có ngăn cách hàng nghìn để hiển thị.
 *  - "1234" → "1,234"
 *  - "1234567.89" → "1,234,567.89"
 *  - "" / "." → giữ nguyên
 *
 * State gốc (`amountText`) vẫn là raw digits để `toDoubleOrNull` parse được.
 */
private fun formatAmountInput(raw: String): String {
    if (raw.isEmpty()) return ""
    val dotIdx = raw.indexOf('.')
    val intPart = if (dotIdx >= 0) raw.substring(0, dotIdx) else raw
    val decPart = if (dotIdx >= 0) raw.substring(dotIdx) else ""
    val formattedInt = intPart.toLongOrNull()?.let { thousandsFormatter.format(it) } ?: intPart
    return formattedInt + decPart
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    executor: ExecutorService,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val photoFile = File(context.cacheDir, "snap_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    imageCapture.takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
            onImageCaptured(Uri.fromFile(photoFile))
        }

        override fun onError(exception: ImageCaptureException) {
            onError(exception)
        }
    })
}
