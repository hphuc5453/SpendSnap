package com.spendsnap.app.data.remote.services.statistics

import com.spendsnap.app.data.remote.clients.StatisticsClient
import com.spendsnap.app.data.remote.models.StatisticsOverviewResponse
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.data.remote.services.BaseService
import javax.inject.Inject

class StatisticsService @Inject constructor(
    private val statisticsClient: StatisticsClient
) : BaseService(), IStatisticsService {

    override suspend fun getOverview(yearMonth: String?): ApiResult<StatisticsOverviewResponse> {
        return safeApiCall { statisticsClient.getOverview(yearMonth) }
    }
}
