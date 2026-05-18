package com.spendsnap.app.data.remote.clients

import com.spendsnap.app.data.remote.models.BaseResponse
import com.spendsnap.app.data.remote.models.CurrencyResponse
import retrofit2.Response
import retrofit2.http.GET

interface CurrencyClient {

    @GET("/currency")
    suspend fun getCurrencies(): Response<BaseResponse<List<CurrencyResponse>>>
}
