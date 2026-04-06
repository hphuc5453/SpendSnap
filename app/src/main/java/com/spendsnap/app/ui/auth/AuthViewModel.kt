package com.spendsnap.app.ui.auth

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsnap.app.data.local.AuthManager
import com.spendsnap.app.data.remote.models.AuthResponse
import com.spendsnap.app.data.remote.models.LoginRequest
import com.spendsnap.app.data.remote.models.SignupRequest
import com.spendsnap.app.data.remote.models.UserResponse
import com.spendsnap.app.data.remote.repositories.IAuthRepository
import com.spendsnap.app.data.remote.services.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: IAuthRepository,
    private val authManager: AuthManager
) : ViewModel() {

    //login
    private val _loginState = MutableStateFlow<ApiResult<AuthResponse>?>(null)
    val loginState: StateFlow<ApiResult<AuthResponse>?> = _loginState.asStateFlow()

    //logout
    private val _logoutState = MutableStateFlow(false)
    val logoutState: StateFlow<Boolean> = _logoutState.asStateFlow()

    //signup
    private val _signupState = MutableStateFlow<ApiResult<UserResponse>?>(null)
    val signupState: StateFlow<ApiResult<UserResponse>?> = _signupState.asStateFlow()


    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = ApiResult.Loading(true)
            val result = authRepository.signIn(LoginRequest(email, encodePassword(password)))
            
            // Nếu thành công, lưu token vào DataStore
            if (result is ApiResult.Success) {
                authManager.saveAccessToken(result.data.accessToken)
            }
            
            _loginState.value = result
        }
    }

    fun signup(name: String, email: String, password: String) {
        viewModelScope.launch {
            _signupState.value = ApiResult.Loading(true)
            
            // Mã hóa mật khẩu bằng SHA-256 trước khi gửi
            val encodedPassword = encodePassword(password)
            
            val result = authRepository.signUp(SignupRequest(name, email, encodedPassword))
            _signupState.value = result
        }
    }

    private fun encodePassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(hash, Base64.NO_WRAP)
        } catch (_: Exception) {
            password // Trả về plain text nếu có lỗi (hoặc xử lý tùy ý)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authManager.clearAuth()
            _logoutState.value = true
        }
    }

    fun resetLoginState() {
        _loginState.value = null
        _signupState.value = null
    }
}
