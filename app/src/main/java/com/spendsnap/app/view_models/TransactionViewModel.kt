package com.spendsnap.app.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsnap.app.data.remote.models.TransactionRequest
import com.spendsnap.app.data.remote.models.TransactionResponse
import com.spendsnap.app.data.remote.repositories.transactions.ITransactionRepository
import com.spendsnap.app.data.remote.services.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: ITransactionRepository
) : ViewModel() {

    private val _createTransactionState = MutableStateFlow<ApiResult<Unit>?>(null)
    val createTransactionState: StateFlow<ApiResult<Unit>?> = _createTransactionState.asStateFlow()

    private val _transactionsState = MutableStateFlow<ApiResult<List<TransactionResponse>>?>(null)
    val transactionsState: StateFlow<ApiResult<List<TransactionResponse>>?> = _transactionsState.asStateFlow()

    fun getTransactions() {
        viewModelScope.launch {
            _transactionsState.value = ApiResult.Loading(true)
            val result = transactionRepository.getTransactions()
            _transactionsState.value = result
        }
    }

    fun createTransaction(imageFile: File, amount: Double) {
        viewModelScope.launch {
            _createTransactionState.value = ApiResult.Loading(true)
            val result = transactionRepository.createTransaction(
                TransactionRequest(
                    imageFile,
                    amount
                )
            )
            _createTransactionState.value = result
        }
    }
}