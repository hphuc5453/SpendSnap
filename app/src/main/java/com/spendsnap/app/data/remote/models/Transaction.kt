package com.spendsnap.app.data.remote.models

import java.io.File

data class TransactionRequest (
    val file: File,
    val amount: Double
)

data class TransactionResponse (
    val _id: String
)