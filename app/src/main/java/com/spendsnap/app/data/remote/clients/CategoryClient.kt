package com.spendsnap.app.data.remote.clients

import com.spendsnap.app.data.remote.models.BaseResponse
import com.spendsnap.app.data.remote.models.CategoryRequest
import com.spendsnap.app.data.remote.models.CategoryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CategoryClient {

    @GET("/category")
    suspend fun getCategory(): Response<BaseResponse<List<CategoryResponse>>>

    @POST("/category")
    suspend fun createCategory(@Body category: CategoryRequest): Response<BaseResponse<Unit>>
}