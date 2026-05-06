package com.spendsnap.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spendsnap.app.data.remote.models.CategoryResponse

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val type: String,
    val limitBudget: Double
)

fun CategoryEntity.toCategoryResponse() = CategoryResponse(
    id = id,
    name = name,
    type = type,
    limitBudget = limitBudget
)

fun CategoryResponse.toCategoryEntity() = CategoryEntity(
    id = id,
    name = name,
    type = type,
    limitBudget = limitBudget
)
