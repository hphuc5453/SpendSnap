package com.spendsnap.app.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsnap.app.data.remote.models.TransactionRequest
import com.spendsnap.app.data.remote.models.TransactionsListResponse
import com.spendsnap.app.data.remote.repositories.transactions.ITransactionRepository
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.data.remote.socket.InvalidatedResource
import com.spendsnap.app.data.remote.socket.SocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: ITransactionRepository,
    private val socketManager: SocketManager
) : ViewModel() {

    private val _createTransactionState = MutableStateFlow<ApiResult<Unit>?>(null)
    val createTransactionState: StateFlow<ApiResult<Unit>?> = _createTransactionState.asStateFlow()

    private val _transactionsState = MutableStateFlow<ApiResult<TransactionsListResponse>?>(null)
    val transactionsState: StateFlow<ApiResult<TransactionsListResponse>?> = _transactionsState.asStateFlow()

    init {
        viewModelScope.launch {
            socketManager.invalidations.collect { resources ->
                if (InvalidatedResource.TRANSACTIONS in resources &&
                    _transactionsState.value is ApiResult.Success
                ) {
                    getTransactions()
                }
            }
        }
    }

    fun getTransactions() {
        viewModelScope.launch {
            _transactionsState.value = ApiResult.Loading(true)
            val result = transactionRepository.getTransactions()
            _transactionsState.value = result
        }
    }

    fun createTransaction(
        amount: Double,
        categoryId: String,
        currency: String? = null,
        imageFile: File? = null
    ) {
        viewModelScope.launch {
            _createTransactionState.value = ApiResult.Loading(true)
            val result = transactionRepository.createTransaction(
                TransactionRequest(
                    amount = amount,
                    categoryId = categoryId,
                    currency = currency,
                    file = imageFile
                )
            )
            _createTransactionState.value = result
        }
    }

    fun resetCreateState() {
        _createTransactionState.value = null
    }
}
