package com.spendsnap.app.data.remote.models

import kotlinx.serialization.Serializable

@Serializable
data class CurrencyResponse(
    val code: String,
    val symbol: String,
    val name: String,
    val locale: String = ""
)
