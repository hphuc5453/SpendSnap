package com.spendsnap.app.data.remote.models

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(val message: String? = null,
    val data: T? = null
)
