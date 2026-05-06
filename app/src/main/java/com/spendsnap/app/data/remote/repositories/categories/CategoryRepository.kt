package com.spendsnap.app.data.remote.repositories.categories

import com.spendsnap.app.data.local.dao.CategoryDao
import com.spendsnap.app.data.local.entities.toCategoryEntity
import com.spendsnap.app.data.local.entities.toCategoryResponse
import com.spendsnap.app.data.remote.models.CategoryRequest
import com.spendsnap.app.data.remote.models.CategoryResponse
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.data.remote.services.categories.CategoryService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryService: CategoryService,
    private val categoryDao: CategoryDao
) : ICategoryRepository {

    private var memoryCache: List<CategoryResponse>? = null

    override fun getCachedCategories(): List<CategoryResponse>? = memoryCache

    override suspend fun getCategories(): ApiResult<List<CategoryResponse>> {
        memoryCache?.let { return ApiResult.Success(it) }

        val roomCached = categoryDao.getCategories()
        if (roomCached.isNotEmpty()) {
            memoryCache = roomCached.map { it.toCategoryResponse() }
            return ApiResult.Success(memoryCache!!)
        }

        val result = categoryService.getCategories()
        if (result is ApiResult.Success) {
            memoryCache = result.data
            categoryDao.insertCategories(result.data.map { it.toCategoryEntity() })
        }
        return result
    }

    override suspend fun createCategory(request: CategoryRequest): ApiResult<Unit> {
        val result = categoryService.createCategory(request)
        if (result is ApiResult.Success) {
            memoryCache = null
            categoryDao.clearCategories()
        }
        return result
    }
}
