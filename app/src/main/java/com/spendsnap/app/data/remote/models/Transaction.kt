package com.spendsnap.app.data.remote.models

import kotlinx.serialization.Serializable
import java.io.File

data class TransactionRequest (
    val file: File,
    val amount: Double
)

@Serializable
data class TransactionResponse (
    val _id: String,
    val amount: Double,
    val imageUrl: String? = null,
    val createdAt: String
)