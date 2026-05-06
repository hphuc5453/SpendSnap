package com.spendsnap.app.data.remote.repositories.user

import com.spendsnap.app.data.local.dao.UserDao
import com.spendsnap.app.data.local.entities.toEntity
import com.spendsnap.app.data.local.entities.toUserResponse
import com.spendsnap.app.data.remote.models.UserResponse
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.data.remote.services.user.UserService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userService: UserService,
    private val userDao: UserDao
) : IUserRepository {

    private var memoryCache: UserResponse? = null

    override fun getCachedUser(): UserResponse? = memoryCache

    override suspend fun getMe(): ApiResult<UserResponse> {
        memoryCache?.let { return ApiResult.Success(it) }

        val roomCached = userDao.getUser()
        if (roomCached != null) {
            memoryCache = roomCached.toUserResponse()
            return ApiResult.Success(memoryCache!!)
        }

        val result = userService.getMe()
        if (result is ApiResult.Success) {
            memoryCache = result.data
            userDao.insertUser(result.data.toEntity())
        }
        return result
    }

    override suspend fun updateLanguage(language: String): ApiResult<UserResponse> {
        val result = userService.updateLanguage(language)
        if (result is ApiResult.Success) {
            memoryCache = result.data
            userDao.insertUser(result.data.toEntity())
        }
        return result
    }

    override suspend fun clearCache() {
        memoryCache = null
        userDao.clearUser()
    }
}