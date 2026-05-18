package com.spendsnap.app.data.remote.repositories.currency

import com.spendsnap.app.data.remote.models.CurrencyResponse
import com.spendsnap.app.data.remote.services.ApiResult

interface ICurrencyRepository {
    suspend fun getCurrencies(): ApiResult<List<CurrencyResponse>>
}
