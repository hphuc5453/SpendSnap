package com.spendsnap.app.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsnap.app.data.remote.repositories.IAuthRepository
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.shared.encodePassword
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {

    // Email + newPassword được giữ giữa các screen (ForgotPassword → NewPassword → VerifyOTP).
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _newPassword = MutableStateFlow("")

    private val _sendCodeState = MutableStateFlow<ApiResult<Unit>?>(null)
    val sendCodeState: StateFlow<ApiResult<Unit>?> = _sendCodeState.asStateFlow()

    private val _resetState = MutableStateFlow<ApiResult<Unit>?>(null)
    val resetState: StateFlow<ApiResult<Unit>?> = _resetState.asStateFlow()

    fun setEmail(value: String) { _email.value = value.trim() }

    fun setNewPassword(value: String) { _newPassword.value = value }

    /** Gửi 6-digit OTP về email. Cooldown 60s do BE enforce. */
    fun sendCode() {
        val target = _email.value
        if (target.isBlank()) return
        viewModelScope.launch {
            _sendCodeState.value = ApiResult.Loading(true)
            _sendCodeState.value = authRepository.forgotPassword(target)
        }
    }

    /**
     * Verify OTP + đặt password mới trong cùng 1 request. BE chấp nhận tối đa 5 lần sai
     * trước khi vô hiệu hoá OTP.
     */
    fun resetPassword(otp: String) {
        val target = _email.value
        val password = _newPassword.value
        if (target.isBlank() || password.isBlank() || otp.length != 6) return
        viewModelScope.launch {
            _resetState.value = ApiResult.Loading(true)
            _resetState.value = authRepository.resetPassword(
                email = target,
                otp = otp,
                newPassword = encodePassword(password)
            )
        }
    }

    fun resetSendCodeState() { _sendCodeState.value = null }
    fun resetResetState() { _resetState.value = null }

    fun clear() {
        _email.value = ""
        _newPassword.value = ""
        _sendCodeState.value = null
        _resetState.value = null
    }
}
