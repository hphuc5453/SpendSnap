package com.spendsnap.app.data.remote.repositories.categories

import com.spendsnap.app.data.local.dao.CategoryDao
import com.spendsnap.app.data.local.dao.CategoryIconDao
import com.spendsnap.app.data.local.entities.toCategoryEntity
import com.spendsnap.app.data.local.entities.toCategoryIconEntity
import com.spendsnap.app.data.local.entities.toCategoryIconResponse
import com.spendsnap.app.data.local.entities.toCategoryResponse
import com.spendsnap.app.data.remote.models.CategoryIconResponse
import com.spendsnap.app.data.remote.models.CategoryRequest
import com.spendsnap.app.data.remote.models.CategoryResponse
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.data.remote.services.categories.CategoryService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryService: CategoryService,
    private val categoryDao: CategoryDao,
    private val categoryIconDao: CategoryIconDao
) : ICategoryRepository {

    private var memoryCache: List<CategoryResponse>? = null
    private var iconsCache: List<CategoryIconResponse>? = null

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
            invalidateCategories()
        }
        return result
    }

    override suspend fun invalidateCategories() {
        memoryCache = null
        categoryDao.clearCategories()
    }

    override suspend fun getCategoryIcons(): ApiResult<List<CategoryIconResponse>> {
        iconsCache?.let { return ApiResult.Success(it) }

        val roomCached = categoryIconDao.getCategoryIcons()
        if (roomCached.isNotEmpty()) {
            iconsCache = roomCached.map { it.toCategoryIconResponse() }
            return ApiResult.Success(iconsCache!!)
        }

        val result = categoryService.getCategoryIcons()
        if (result is ApiResult.Success) {
            iconsCache = result.data
            categoryIconDao.insertCategoryIcons(result.data.map { it.toCategoryIconEntity() })
        }
        return result
    }
}
