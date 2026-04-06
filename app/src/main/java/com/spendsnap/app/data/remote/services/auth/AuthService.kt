package com.spendsnap.app.data.remote.services.auth

import com.spendsnap.app.data.remote.clients.AuthClient
import com.spendsnap.app.data.remote.models.AuthResponse
import com.spendsnap.app.data.remote.models.LoginRequest
import com.spendsnap.app.data.remote.models.SignupRequest
import com.spendsnap.app.data.remote.models.UserResponse
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.data.remote.services.BaseService
import javax.inject.Inject

class AuthService @Inject constructor(
    private val authClient: AuthClient
) : BaseService(), IAuthService {

    override suspend fun signIn(request: LoginRequest): ApiResult<AuthResponse> {
        return safeApiCall { authClient.signIn(request) }
    }

    override suspend fun signUp(request: SignupRequest): ApiResult<UserResponse> {
        return safeApiCall { authClient.signup(request) }
    }
}
