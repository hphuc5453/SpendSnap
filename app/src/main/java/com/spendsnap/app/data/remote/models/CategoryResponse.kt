package com.spendsnap.app.data.remote.models

data class CategoryResponse(
    val id: Int,
    val name: String,
    val type: String,
    val limitBudget: Double
)

data class CategoryRequest(
    val name: String,
    val type: String,
    val limitBudget: Double
)