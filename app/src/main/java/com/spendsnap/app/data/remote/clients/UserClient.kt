package com.spendsnap.app.data.remote.clients

import com.spendsnap.app.data.remote.models.BaseResponse
import com.spendsnap.app.data.remote.models.UserResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST

interface UserClient {
    @GET("users/profile")
    suspend fun getMe(): Response<BaseResponse<UserResponse>>

    @POST
    suspend fun updateAvatar()
}