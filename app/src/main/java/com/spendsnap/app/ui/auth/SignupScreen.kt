package com.spendsnap.app.ui.auth

import android.util.Patterns
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendsnap.app.R
import com.spendsnap.app.data.remote.models.UserResponse
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.ui.components.LoadingDialog
import com.spendsnap.app.ui.components.MessageDialog
import com.spendsnap.app.ui.theme.inputBackground
import com.spendsnap.app.view_models.AuthViewModel

@Composable
fun SignupScreen(
    onSignupSuccess: (data: UserResponse) -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    val signupState by viewModel.signupState.collectAsState()
    val isLoading = signupState is ApiResult.Loading

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

    // Xử lý các side-effects từ trạng thái API
    LaunchedEffect(signupState) {
        when (signupState) {
            is ApiResult.Success -> {
                onSignupSuccess((signupState as ApiResult.Success).data)
                viewModel.resetLoginState()
            }
            is ApiResult.Error -> {
                val error = (signupState as ApiResult.Error).exception
                errorMessage = error.message
                showErrorDialog = true
                viewModel.resetLoginState()
            }
            else -> {}
        }
    }

    val handleSignup = {
        var isValid = true

        if (name.isBlank()) {
            nameError = context.getString(R.string.error_name_required)
            isValid = false
        } else {
            nameError = ""
        }

        if (email.isBlank()) {
            emailError = context.getString(R.string.error_email_required)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = context.getString(R.string.error_email)
            isValid = false
        } else {
            emailError = ""
        }

        if (password.isBlank()) {
            passwordError = context.getString(R.string.error_password_required)
            isValid = false
        } else {
            passwordError = ""
        }

        if (confirmPassword != password) {
            confirmPasswordError = context.getString(R.string.error_password_mismatch)
            isValid = false
        } else {
            confirmPasswordError = ""
        }

        if (isValid) {
            viewModel.signup(name, email, password)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_category_24),
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.signup_title),
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.signup_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Name Field
        SignupField(
            label = stringResource(R.string.label_name),
            value = name,
            onValueChange = { 
                name = it
                if (nameError.isNotEmpty()) nameError = ""
            },
            placeholder = stringResource(R.string.placeholder_name),
            icon = Icons.Default.Person,
            error = nameError
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Email Field
        SignupField(
            label = stringResource(R.string.label_email),
            value = email,
            onValueChange = { 
                email = it
                if (emailError.isNotEmpty()) emailError = ""
            },
            placeholder = stringResource(R.string.placeholder_signup_email),
            icon = Icons.Default.Email,
            error = emailError
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Password Field
        SignupField(
            label = stringResource(R.string.label_password),
            value = password,
            onValueChange = { 
                password = it
                if (passwordError.isNotEmpty()) passwordError = ""
            },
            placeholder = stringResource(R.string.placeholder_password),
            icon = Icons.Default.Lock,
            isPassword = true,
            error = passwordError
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Confirm Password Field
        SignupField(
            label = stringResource(R.string.label_confirm_password),
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it
                if (confirmPasswordError.isNotEmpty()) confirmPasswordError = ""
            },
            placeholder = stringResource(R.string.placeholder_password),
            icon = Icons.Default.CheckCircle,
            isPassword = true,
            error = confirmPasswordError
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Create Account Button
        Button(
            onClick = handleSignup,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.btn_create_account),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.label_already_have_account), 
                color = Color.Gray
            )
            TextButton(onClick = onBackToLogin) {
                Text(
                    text = stringResource(R.string.btn_login), 
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SignupField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    error: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(30.dp)),
            placeholder = { Text(placeholder, color = Color.DarkGray) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp)) },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
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
            singleLine = true
        )
        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}
