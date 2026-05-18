package com.spendsnap.app.data.remote.services.currency

import com.spendsnap.app.data.remote.models.CurrencyResponse
import com.spendsnap.app.data.remote.services.ApiResult

interface ICurrencyService {
    suspend fun getCurrencies(): ApiResult<List<CurrencyResponse>>
}
