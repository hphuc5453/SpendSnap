package com.spendsnap.app.ui.camera

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.spendsnap.app.R
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.ui.components.LoadingDialog
import com.spendsnap.app.ui.components.MessageDialog
import com.spendsnap.app.ui.shared.HeaderSection
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    var hasCameraPermission by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    val createTransactionState by viewModel.createTransactionState.collectAsState()
    val isLoading = createTransactionState is ApiResult.Loading

    // State cho Error Dialog
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Dialog Components
    LoadingDialog(isLoading = isLoading)
    MessageDialog(
        show = showErrorDialog,
        message = errorMessage,
        onDismiss = { showErrorDialog = false }
    )

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)

    }

    // Xử lý các side-effects từ trạng thái API
    LaunchedEffect(createTransactionState) {
        when (createTransactionState) {
            is ApiResult.Error -> {
                val error = (createTransactionState as ApiResult.Error).exception
                errorMessage = error.message
                showErrorDialog = true
            }
            else -> {}
        }
    }

    var capturedUri by remember { mutableStateOf<Uri?>(null) }
    var amountText by remember { mutableStateOf("") }

    Box(modifier = modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        Column {
            HeaderSection()
            if (hasCameraPermission) {
                if (capturedUri == null) {
                    CameraView(
                        executor = cameraExecutor,
                        onImageCaptured = { capturedUri = it },
                        onError = { Log.e("CameraScreen", "Error", it) }
                    )
                } else {
                    CapturePreview(
                        uri = capturedUri!!,
                        amount = amountText,
                        onAmountChange = { amountText = it },
                        onConfirm = {
                            // 1. Validation số tiền
                            val amountValue = amountText.toDoubleOrNull()
                            if (amountValue == null || amountValue <= 0) {
                                Toast.makeText(context, "Vui lòng nhập số tiền hợp lệ (> 0)", Toast.LENGTH_SHORT).show()
                                return@CapturePreview
                            }

                            // 2. Chuyển đổi URI sang File để truyền vào FormData
                            val imageFile = capturedUri?.path?.let { File(it) }
                            if (imageFile == null || !imageFile.exists()) {
                                Toast.makeText(context, "Không tìm thấy ảnh vừa chụp", Toast.LENGTH_SHORT).show()
                                return@CapturePreview
                            }

                            // 3. Chuẩn bị dữ liệu gửi đi
                            Log.d("CameraScreen", "Ready to upload: Amount=$amountValue, File=${imageFile.absolutePath}")
                            Toast.makeText(context, "Đang xử lý hóa đơn...", Toast.LENGTH_SHORT).show()
                            
                            // TODO: Gọi API service tại đây (Sử dụng MultipartBody cho imageFile)
                            viewModel.createTransaction(imageFile, amountValue)
                        },
                        onRetake = { capturedUri = null }
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
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
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
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.8f)
                .clip(RoundedCornerShape(32.dp))
                .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
        ) {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
            CornerMarkers(MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = { takePhoto(context, imageCapture, executor, onImageCaptured, onError) },
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Image(
                    painter = painterResource(R.drawable.outline_photo_camera_24),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun CapturePreview(
    uri: Uri,
    amount: String,
    onAmountChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onRetake: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.8f)
                .clip(RoundedCornerShape(32.dp))
                .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = uri,
                contentDescription = "Captured Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.label_entering_amount),
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            letterSpacing = 1.sp
        )

        TextField(
            value = amount,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                    onAmountChange(newValue)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "0",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 64.sp
                    ),
                    color = Color.White.copy(alpha = 0.3f)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 64.sp,
                textAlign = TextAlign.Center
            ),
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onRetake,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    stringResource(R.string.btn_retake_photo),
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                modifier = Modifier
                    .weight(2f)
                    .height(56.dp),
                onClick = onConfirm,
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.btn_confirm),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black)
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
        val strokeWidth = 12f
        val cornerSize = 60f
        
        // Top Left
        drawPath(
            path = Path().apply {
                moveTo(0f, cornerSize)
                lineTo(0f, 0f)
                lineTo(cornerSize, 0f)
            },
            color = color,
            style = Stroke(width = strokeWidth)
        )
        
        // Top Right
        drawPath(
            path = Path().apply {
                moveTo(size.width - cornerSize, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, cornerSize)
            },
            color = color,
            style = Stroke(width = strokeWidth)
        )
        
        // Bottom Left
        drawPath(
            path = Path().apply {
                moveTo(0f, size.height - cornerSize)
                lineTo(0f, size.height)
                lineTo(cornerSize, size.height)
            },
            color = color,
            style = Stroke(width = strokeWidth)
        )
        
        // Bottom Right
        drawPath(
            path = Path().apply {
                moveTo(size.width - cornerSize, size.height)
                lineTo(size.width, size.height)
                lineTo(size.width, size.height - cornerSize)
            },
            color = color,
            style = Stroke(width = strokeWidth)
        )
    }
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    executor: ExecutorService,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val photoFile = File(
        context.cacheDir,
        "snap_${System.currentTimeMillis()}.jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onImageCaptured(Uri.fromFile(photoFile))
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}
