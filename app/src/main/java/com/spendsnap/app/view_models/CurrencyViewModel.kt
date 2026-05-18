package com.spendsnap.app.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsnap.app.data.remote.models.CurrencyResponse
import com.spendsnap.app.data.remote.repositories.currency.ICurrencyRepository
import com.spendsnap.app.data.remote.services.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val currencyRepository: ICurrencyRepository
) : ViewModel() {

    private val _currenciesState = MutableStateFlow<ApiResult<List<CurrencyResponse>>?>(null)
    val currenciesState: StateFlow<ApiResult<List<CurrencyResponse>>?> = _currenciesState.asStateFlow()

    fun getCurrencies() {
        viewModelScope.launch {
            _currenciesState.value = ApiResult.Loading(true)
            _currenciesState.value = currencyRepository.getCurrencies()
        }
    }
}
