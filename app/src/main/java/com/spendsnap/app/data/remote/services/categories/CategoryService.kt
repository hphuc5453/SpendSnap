package com.spendsnap.app.data.remote.services.categories

import com.spendsnap.app.data.remote.clients.CategoryClient
import com.spendsnap.app.data.remote.models.CategoryRequest
import com.spendsnap.app.data.remote.models.CategoryResponse
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.data.remote.services.BaseService
import javax.inject.Inject

class CategoryService @Inject constructor(
    private val categoryClient: CategoryClient
) : BaseService(), ICategoryService {

    override suspend fun getCategories(): ApiResult<List<CategoryResponse>> {
        return safeApiCall { categoryClient.getCategory() }
    }

    override suspend fun createCategory(request: CategoryRequest): ApiResult<Unit> {
        return safeApiCall { categoryClient.createCategory(request) }
    }
}
