package com.spendsnap.app.data.remote.services.user

import com.spendsnap.app.data.remote.clients.UserClient
import com.spendsnap.app.data.remote.models.UpdateLanguageRequest
import com.spendsnap.app.data.remote.models.UserResponse
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.data.remote.services.BaseService
import javax.inject.Inject

class UserService @Inject constructor(private val userClient: UserClient) : BaseService(), IUserService {
    override suspend fun getMe(): ApiResult<UserResponse> {
        return safeApiCall { userClient.getMe() }
    }

    override suspend fun updateLanguage(language: String): ApiResult<UserResponse> {
        return safeApiCall { userClient.updateLanguage(UpdateLanguageRequest(language)) }
    }
}