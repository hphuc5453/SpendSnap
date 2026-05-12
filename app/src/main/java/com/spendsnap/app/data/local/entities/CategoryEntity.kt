package com.spendsnap.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spendsnap.app.data.remote.models.CategoryResponse

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val kind: String,
    val icon: String,
    val color: String? = null,
    val isDefault: Boolean = false
)

fun CategoryEntity.toCategoryResponse() = CategoryResponse(
    id = id,
    name = name,
    kind = kind,
    icon = icon,
    color = color,
    isDefault = isDefault
)

fun CategoryResponse.toCategoryEntity() = CategoryEntity(
    id = id,
    name = name,
    kind = kind,
    icon = icon,
    color = color,
    isDefault = isDefault
)
