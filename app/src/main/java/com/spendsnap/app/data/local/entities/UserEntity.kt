package com.spendsnap.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spendsnap.app.data.remote.models.UserResponse

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val avatar: String? = null,
    val language: String? = null,
    val currency: String? = null
)

fun UserEntity.toUserResponse() = UserResponse(
    id = id,
    name = name,
    email = email,
    avatar = avatar,
    language = language,
    currency = currency
)

fun UserResponse.toEntity() = UserEntity(
    id = id,
    name = name,
    email = email,
    avatar = avatar,
    language = language,
    currency = currency
)
