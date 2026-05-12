package com.spendsnap.app.data.remote.services.transactions

import com.spendsnap.app.data.remote.clients.TransactionClient
import com.spendsnap.app.data.remote.models.TransactionRequest
import com.spendsnap.app.data.remote.models.TransactionsListResponse
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.data.remote.services.BaseService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class TransactionService @Inject constructor(
    private val transactionClient: TransactionClient
) : BaseService(), ITransactionService {
    override suspend fun createTransaction(request: TransactionRequest): ApiResult<Unit> {
        val plainText = "text/plain".toMediaTypeOrNull()
        val amountPart = request.amount.toString().toRequestBody(plainText)
        val categoryIdPart = request.categoryId.toRequestBody(plainText)

        val imagePart = request.file?.let { file ->
            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", file.name, requestFile)
        }

        return safeApiCall { transactionClient.createTransaction(amountPart, categoryIdPart, imagePart) }
    }

    override suspend fun getTransactions(): ApiResult<TransactionsListResponse> {
        return safeApiCall { transactionClient.getTransactions() }
    }
}
