package com.spendsnap.app.data.remote.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class SignupRequest(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val user: UserResponse
)

@Serializable
data class UserResponse(
    @SerialName("_id")
    val id: String,
    val name: String,
    val email: String,
    val avatar: String? = null,
    val language: String? = null
)

@Serializable
data class UpdateLanguageRequest(
    val language: String
)
