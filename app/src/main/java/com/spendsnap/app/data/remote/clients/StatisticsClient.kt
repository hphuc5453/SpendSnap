package com.spendsnap.app.data.remote.clients

import com.spendsnap.app.data.remote.models.BaseResponse
import com.spendsnap.app.data.remote.models.StatisticsOverviewResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface StatisticsClient {

    @GET("/statistics/overview")
    suspend fun getOverview(
        @Query("yearMonth") yearMonth: String? = null
    ): Response<BaseResponse<StatisticsOverviewResponse>>
}
