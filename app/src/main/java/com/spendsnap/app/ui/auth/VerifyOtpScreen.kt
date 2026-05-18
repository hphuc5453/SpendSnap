package com.spendsnap.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendsnap.app.R
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.ui.components.AppStatusDialog
import com.spendsnap.app.ui.components.DialogType
import com.spendsnap.app.ui.components.LoadingDialog
import com.spendsnap.app.ui.shared.HeaderSection
import com.spendsnap.app.view_models.ForgotPasswordViewModel

private const val OTP_LENGTH = 6

@Composable
fun VerifyOtpScreen(
    onBack: () -> Unit,
    onVerified: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val resetState by viewModel.resetState.collectAsState()
    val sendCodeState by viewModel.sendCodeState.collectAsState()
    val isLoading = resetState is ApiResult.Loading
    val isResending = sendCodeState is ApiResult.Loading

    var otp by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }

    LoadingDialog(isLoading = isLoading)
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
        message = stringResource(R.string.reset_password_success),
        onDismiss = {
            showSuccessDialog = false
            viewModel.clear()
            onVerified()
        }
    )

    LaunchedEffect(resetState) {
        when (val state = resetState) {
            is ApiResult.Success -> {
                showSuccessDialog = true
                viewModel.resetResetState()
            }
            is ApiResult.Error -> {
                errorMessage = state.exception.message
                showErrorDialog = true
                viewModel.resetResetState()
            }
            else -> {}
        }
    }

    LaunchedEffect(sendCodeState) {
        when (val state = sendCodeState) {
            is ApiResult.Error -> {
                errorMessage = state.exception.message
                showErrorDialog = true
                viewModel.resetSendCodeState()
            }
            is ApiResult.Success -> {
                viewModel.resetSendCodeState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HeaderSection(stringResource(R.string.btn_verify), onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.verify_check_your),
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = stringResource(R.string.verify_inbox),
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.verify_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            OtpInput(
                otp = otp,
                onOtpChange = {
                    otp = it
                    if (otpError.isNotEmpty()) otpError = ""
                },
                focusRequester = focusRequester
            )

            if (otpError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = otpError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (otp.length != OTP_LENGTH) {
                        otpError = context.getString(R.string.otp_error_invalid)
                        return@Button
                    }
                    viewModel.resetPassword(otp)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = stringResource(R.string.btn_verify),
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.otp_didnt_receive),
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(
                    onClick = { viewModel.sendCode() },
                    enabled = !isResending
                ) {
                    Text(
                        text = stringResource(R.string.btn_resend_code),
                        color = Color(0xFFFF5C00),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun OtpInput(
    otp: String,
    onOtpChange: (String) -> Unit,
    focusRequester: FocusRequester
) {
    BasicTextField(
        value = otp,
        onValueChange = { value ->
            val sanitized = value.filter(Char::isDigit).take(OTP_LENGTH)
            onOtpChange(sanitized)
        },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        textStyle = TextStyle(color = Color.Transparent),
        cursorBrush = SolidColor(Color.Transparent),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(OTP_LENGTH) { index ->
                    OtpCell(
                        digit = otp.getOrNull(index)?.toString(),
                        modifier = Modifier.size(48.dp, 56.dp)
                    )
                }
            }
        }
    )
}

@Composable
private fun OtpCell(digit: String?, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val filled = !digit.isNullOrEmpty()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1C1C1E))
            .border(
                width = 1.dp,
                color = if (filled) primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit ?: "0",
            color = if (filled) Color.White else Color.Gray.copy(alpha = 0.4f),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
    }
}
