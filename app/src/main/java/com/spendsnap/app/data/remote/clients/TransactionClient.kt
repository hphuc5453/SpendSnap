package com.spendsnap.app.data.remote.clients

import com.spendsnap.app.data.remote.models.BaseResponse
import com.spendsnap.app.data.remote.models.TransactionsListResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface TransactionClient {
    @Multipart
    @POST("/transactions/create")
    suspend fun createTransaction(
        @Part("amount") amount: RequestBody,
        @Part("categoryId") categoryId: RequestBody,
        @Part("currency") currency: RequestBody? = null,
        @Part image: MultipartBody.Part? = null
    ): Response<BaseResponse<Unit>>

    @GET("/transactions")
    suspend fun getTransactions(): Response<BaseResponse<TransactionsListResponse>>
}
