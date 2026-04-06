package com.spendsnap.app.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendsnap.app.R
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.ui.components.LoadingDialog
import com.spendsnap.app.ui.components.MessageDialog
import com.spendsnap.app.ui.theme.inputBackground

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSignupClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    
    // State cho Error Dialog
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val loginState by viewModel.loginState.collectAsState()
    val isLoading = loginState is ApiResult.Loading

    // Dialog Components
    LoadingDialog(isLoading = isLoading)
    MessageDialog(
        show = showErrorDialog,
        message = errorMessage,
        onDismiss = { showErrorDialog = false }
    )

    // Xử lý các side-effects từ trạng thái API
    LaunchedEffect(loginState) {
        when (loginState) {
            is ApiResult.Success -> {
                onLoginSuccess()
                viewModel.resetLoginState()
            }
            is ApiResult.Error -> {
                val error = (loginState as ApiResult.Error).exception
                errorMessage = error.message
                showErrorDialog = true
                viewModel.resetLoginState()
            }
            else -> {}
        }
    }

    val handleLogin = {
        var isValid = true
        if (email.isEmpty()) {
            emailError = context.getString(R.string.error_email)
            isValid = false
        } else {
            emailError = ""
        }

        if (password.isEmpty()) {
            passwordError = context.getString(R.string.error_password)
            isValid = false
        } else {
            passwordError = ""
        }

        if (isValid) {
            viewModel.signIn(email, password)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(Color(0xFF000000))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.login_tagline),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Email Field
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.label_email),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = email,
                onValueChange = { 
                    email = it
                    if (emailError.isNotEmpty()) emailError = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(30.dp)),
                placeholder = {
                    Text(
                        stringResource(R.string.placeholder_email),
                        color = Color.DarkGray
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = inputBackground,
                    unfocusedContainerColor = inputBackground,
                    disabledContainerColor = inputBackground,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                enabled = !isLoading
            )
            if (emailError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = emailError,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.label_password),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { 
                    password = it
                    if (passwordError.isNotEmpty()) passwordError = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(30.dp)),
                placeholder = {
                    Text(
                        stringResource(R.string.placeholder_password),
                        color = Color.DarkGray
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Image(
                            painter = painterResource(id = R.drawable.outline_visibility_24),
                            contentDescription = null,
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = inputBackground,
                    unfocusedContainerColor = inputBackground,
                    disabledContainerColor = inputBackground,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                ),
                singleLine = true,
                enabled = !isLoading
            )
            if (passwordError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = passwordError,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
        
        // Forgot Password Button aligned to the end
        TextButton(
            onClick = { },
            modifier = Modifier.align(Alignment.End),
            enabled = !isLoading
        ) {
            Text(
                text = stringResource(R.string.btn_forgot_password),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Login Button
        Button(
            onClick = handleLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = !isLoading
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.btn_login),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        Row(
            modifier = Modifier.padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.label_new_user), color = Color.Gray)
            TextButton(
                onClick = onSignupClick,
                enabled = !isLoading
            ) {
                Text(
                    text = stringResource(R.string.btn_create_account),
                    color = Color(0xFFFF5C00),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SocialButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = { },
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontWeight = FontWeight.SemiBold)
        }
    }
}
