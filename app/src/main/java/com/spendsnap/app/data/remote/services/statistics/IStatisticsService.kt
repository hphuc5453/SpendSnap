package com.spendsnap.app.data.remote.services.statistics

import com.spendsnap.app.data.remote.models.StatisticsOverviewResponse
import com.spendsnap.app.data.remote.services.ApiResult

interface IStatisticsService {
    suspend fun getOverview(yearMonth: String? = null): ApiResult<StatisticsOverviewResponse>
}
