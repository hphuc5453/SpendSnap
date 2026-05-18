package com.spendsnap.app.data.remote.services.currency

import com.spendsnap.app.data.remote.clients.CurrencyClient
import com.spendsnap.app.data.remote.models.CurrencyResponse
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.data.remote.services.BaseService
import javax.inject.Inject

class CurrencyService @Inject constructor(
    private val currencyClient: CurrencyClient
) : BaseService(), ICurrencyService {

    override suspend fun getCurrencies(): ApiResult<List<CurrencyResponse>> {
        return safeApiCall { currencyClient.getCurrencies() }
    }
}
