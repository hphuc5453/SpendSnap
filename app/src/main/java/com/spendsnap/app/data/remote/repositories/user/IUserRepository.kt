package com.spendsnap.app.data.remote.repositories.user

import com.spendsnap.app.data.remote.models.UserResponse
import com.spendsnap.app.data.remote.services.ApiResult

interface IUserRepository {
    fun getCachedUser(): UserResponse?
    suspend fun getMe(): ApiResult<UserResponse>
    suspend fun updateLanguage(language: String): ApiResult<UserResponse>
    suspend fun clearCache()
}