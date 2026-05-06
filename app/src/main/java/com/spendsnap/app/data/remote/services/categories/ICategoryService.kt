package com.spendsnap.app.data.remote.services.categories

import com.spendsnap.app.data.remote.models.CategoryRequest
import com.spendsnap.app.data.remote.models.CategoryResponse
import com.spendsnap.app.data.remote.services.ApiResult

interface ICategoryService {
    suspend fun getCategories(): ApiResult<List<CategoryResponse>>
    suspend fun createCategory(request: CategoryRequest): ApiResult<Unit>
}
