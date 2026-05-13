package com.spendsnap.app.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsnap.app.data.remote.models.StatisticsOverviewResponse
import com.spendsnap.app.data.remote.repositories.statistics.IStatisticsRepository
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.data.remote.socket.InvalidatedResource
import com.spendsnap.app.data.remote.socket.SocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsRepository: IStatisticsRepository,
    private val socketManager: SocketManager
) : ViewModel() {

    private val _overviewState = MutableStateFlow<ApiResult<StatisticsOverviewResponse>?>(null)
    val overviewState: StateFlow<ApiResult<StatisticsOverviewResponse>?> = _overviewState.asStateFlow()

    private var lastYearMonth: String? = null

    init {
        viewModelScope.launch {
            socketManager.invalidations.collect { resources ->
                if (InvalidatedResource.STATISTICS in resources &&
                    _overviewState.value is ApiResult.Success
                ) {
                    getOverview(lastYearMonth)
                }
            }
        }
    }

    fun getOverview(yearMonth: String? = null) {
        lastYearMonth = yearMonth
        viewModelScope.launch {
            _overviewState.value = ApiResult.Loading(true)
            _overviewState.value = statisticsRepository.getOverview(yearMonth)
        }
    }
}
