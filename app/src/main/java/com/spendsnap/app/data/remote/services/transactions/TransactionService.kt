package com.spendsnap.app.data.remote.services.transactions

import com.spendsnap.app.data.remote.clients.TransactionClient
import com.spendsnap.app.data.remote.models.TransactionRequest
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
        val imageFile = request.file
        val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

        val amountPart = request.amount.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        return safeApiCall { transactionClient.createTransaction(imagePart, amountPart) }
    }
}