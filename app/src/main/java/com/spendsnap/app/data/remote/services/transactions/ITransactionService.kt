package com.spendsnap.app.data.remote.services.transactions

import com.spendsnap.app.data.remote.models.TransactionRequest
import com.spendsnap.app.data.remote.models.TransactionResponse
import com.spendsnap.app.data.remote.services.ApiResult

interface ITransactionService {
    suspend fun createTransaction(request: TransactionRequest): ApiResult<Unit>

    suspend fun getTransactions(): ApiResult<List<TransactionResponse>>
}