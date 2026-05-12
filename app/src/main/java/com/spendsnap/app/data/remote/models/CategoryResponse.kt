package com.spendsnap.app.data.remote.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryResponse(
    @SerialName("_id")
    val id: String,
    val name: String,
    val kind: String = "expense",
    val icon: String = "other",
    val color: String? = null,
    val isDefault: Boolean = false,
    val isMostUsed: Boolean = false
)

@Serializable
data class CategoryRequest(
    val name: String,
    val kind: String,
    val icon: String,
    val color: String? = null
)

@Serializable
data class CategoryIconResponse(
    val slug: String,
    val label: String,
    val icon: String
)
