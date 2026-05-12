package com.spendsnap.app.data.remote.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

data class TransactionRequest(
    val amount: Double,
    val categoryId: String,
    val file: File? = null
)

@Serializable
data class TransactionCategory(
    @SerialName("_id")
    val id: String,
    val name: String = "",
    val icon: String = "other",
    val color: String? = null,
    val kind: String = "expense"
)

@Serializable
data class TransactionResponse(
    val _id: String,
    val categoryId: TransactionCategory? = null,
    val amount: Double,
    val imageUrl: String? = null,
    val createdAt: String
)

@Serializable
data class TransactionsListResponse(
    val transactions: List<TransactionResponse> = emptyList(),
    val totalSpent: Double = 0.0
)
