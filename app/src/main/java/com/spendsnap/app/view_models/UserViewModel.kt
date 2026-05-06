package com.spendsnap.app.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsnap.app.data.remote.models.UserResponse
import com.spendsnap.app.data.remote.repositories.user.IUserRepository
import com.spendsnap.app.data.remote.services.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(private val userRepository: IUserRepository) : ViewModel() {
    private val _userState = MutableStateFlow<ApiResult<UserResponse>?>(
        userRepository.getCachedUser()?.let { ApiResult.Success(it) }
    )
    val user: StateFlow<ApiResult<UserResponse>?> = _userState.asStateFlow()

    private val _updateLanguageState = MutableStateFlow<ApiResult<UserResponse>?>(null)
    val updateLanguageState: StateFlow<ApiResult<UserResponse>?> = _updateLanguageState.asStateFlow()

    fun getMe() {
        if (_userState.value is ApiResult.Success) return
        viewModelScope.launch {
            _userState.value = ApiResult.Loading(true)
            val result = userRepository.getMe()
            _userState.value = result
        }
    }

    fun updateLanguage(language: String) {
        viewModelScope.launch {
            _updateLanguageState.value = ApiResult.Loading(true)
            val result = userRepository.updateLanguage(language)
            _updateLanguageState.value = result
            if (result is ApiResult.Success) {
                _userState.value = result
            }
        }
    }
}