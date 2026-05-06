package com.spendsnap.app.data.remote.clients

import com.spendsnap.app.data.remote.models.BaseResponse
import com.spendsnap.app.data.remote.models.UpdateLanguageRequest
import com.spendsnap.app.data.remote.models.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface UserClient {
    @GET("users/profile")
    suspend fun getMe(): Response<BaseResponse<UserResponse>>

    @PATCH("users/profile")
    suspend fun updateLanguage(@Body request: UpdateLanguageRequest): Response<BaseResponse<UserResponse>>
}