package com.spendsnap.app.data.remote.repositories.statistics

import com.spendsnap.app.data.remote.models.StatisticsOverviewResponse
import com.spendsnap.app.data.remote.services.ApiResult

interface IStatisticsRepository {
    suspend fun getOverview(yearMonth: String? = null): ApiResult<StatisticsOverviewResponse>
}
