package com.spendsnap.app.data.remote.services.transactions

import com.spendsnap.app.data.remote.models.TransactionRequest
import com.spendsnap.app.data.remote.services.ApiResult
import java.io.File

interface ITransactionService {
    suspend fun createTransaction(request: TransactionRequest): ApiResult<Unit>
}