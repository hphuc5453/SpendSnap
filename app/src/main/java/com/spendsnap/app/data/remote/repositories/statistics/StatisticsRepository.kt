package com.spendsnap.app.data.remote.repositories.statistics

import com.spendsnap.app.data.remote.models.StatisticsOverviewResponse
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.data.remote.services.statistics.StatisticsService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepository @Inject constructor(
    private val statisticsService: StatisticsService
) : IStatisticsRepository {

    override suspend fun getOverview(yearMonth: String?): ApiResult<StatisticsOverviewResponse> {
        return statisticsService.getOverview(yearMonth)
    }
}
