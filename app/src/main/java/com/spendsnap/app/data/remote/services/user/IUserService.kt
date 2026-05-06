package com.spendsnap.app.data.remote.services.user

import com.spendsnap.app.data.remote.models.UserResponse
import com.spendsnap.app.data.remote.services.ApiResult

interface IUserService {
    suspend fun getMe(): ApiResult<UserResponse>
    suspend fun updateLanguage(language: String): ApiResult<UserResponse>
}