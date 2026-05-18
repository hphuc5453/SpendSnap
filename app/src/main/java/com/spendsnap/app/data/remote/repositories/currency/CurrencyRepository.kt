package com.spendsnap.app.data.remote.repositories.currency

import com.spendsnap.app.data.remote.models.CurrencyResponse
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.data.remote.services.currency.CurrencyService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRepository @Inject constructor(
    private val currencyService: CurrencyService
) : ICurrencyRepository {

    private var cache: List<CurrencyResponse>? = null

    override suspend fun getCurrencies(): ApiResult<List<CurrencyResponse>> {
        cache?.let { return ApiResult.Success(it) }
        val result = currencyService.getCurrencies()
        if (result is ApiResult.Success) cache = result.data
        return result
    }
}
