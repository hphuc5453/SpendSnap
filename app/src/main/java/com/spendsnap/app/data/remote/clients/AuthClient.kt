package com.spendsnap.app.data.remote.clients

import com.spendsnap.app.data.remote.models.AuthResponse
import com.spendsnap.app.data.remote.models.BaseResponse
import com.spendsnap.app.data.remote.models.LoginRequest
import com.spendsnap.app.data.remote.models.SignupRequest
import com.spendsnap.app.data.remote.models.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthClient {
    @POST("auth/signin")
    suspend fun signIn(@Body request: LoginRequest): Response<BaseResponse<AuthResponse>>

    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<BaseResponse<UserResponse>>
}