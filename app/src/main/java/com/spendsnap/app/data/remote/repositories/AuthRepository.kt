package com.spendsnap.app.data.remote.repositories

import com.spendsnap.app.data.remote.models.AuthResponse
import com.spendsnap.app.data.remote.models.LoginRequest
import com.spendsnap.app.data.remote.models.SignupRequest
import com.spendsnap.app.data.remote.models.UserResponse
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.data.remote.services.auth.IAuthService
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authService: IAuthService
) : IAuthRepository {

    override suspend fun signIn(request: LoginRequest): ApiResult<AuthResponse> {
        return authService.signIn(request)
    }

    override suspend fun signUp(request: SignupRequest): ApiResult<UserResponse> {
        return authService.signUp(request)
    }
}
