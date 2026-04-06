package com.spendsnap.app.data.remote.repositories

import com.spendsnap.app.data.remote.models.AuthResponse
import com.spendsnap.app.data.remote.models.LoginRequest
import com.spendsnap.app.data.remote.models.SignupRequest
import com.spendsnap.app.data.remote.models.UserResponse
import com.spendsnap.app.data.remote.services.ApiResult

interface IAuthRepository {
    suspend fun signIn(request: LoginRequest): ApiResult<AuthResponse>
    suspend fun signUp(request: SignupRequest): ApiResult<UserResponse>
}
