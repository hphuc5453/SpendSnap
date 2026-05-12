package com.spendsnap.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spendsnap.app.data.remote.models.CategoryIconResponse

@Entity(tableName = "category_icons")
data class CategoryIconEntity(
    @PrimaryKey
    val slug: String,
    val label: String,
    val icon: String
)

fun CategoryIconEntity.toCategoryIconResponse() = CategoryIconResponse(
    slug = slug,
    label = label,
    icon = icon
)

fun CategoryIconResponse.toCategoryIconEntity() = CategoryIconEntity(
    slug = slug,
    label = label,
    icon = icon
)
