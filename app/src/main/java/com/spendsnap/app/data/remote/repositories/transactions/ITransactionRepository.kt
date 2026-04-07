package com.spendsnap.app.data.remote.repositories.transactions

import com.spendsnap.app.data.remote.models.TransactionRequest
import com.spendsnap.app.data.remote.services.ApiResult
import java.io.File

interface ITransactionRepository {
    suspend fun createTransaction(request: TransactionRequest): ApiResult<Unit>
}