package com.spendsnap.app.data.remote.repositories.transactions

import com.spendsnap.app.data.remote.models.TransactionRequest
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.data.remote.services.transactions.TransactionService
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionService: TransactionService
): ITransactionRepository {
    override suspend fun createTransaction(request: TransactionRequest): ApiResult<Unit> {
        return transactionService.createTransaction(request)
    }
}