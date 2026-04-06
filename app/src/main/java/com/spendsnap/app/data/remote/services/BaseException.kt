package com.spendsnap.app.data.remote.services

open class BaseException(
    override val message: String,
    val code: Int? = null,
    val type: ErrorType = ErrorType.UNKNOWN
) : Exception(message)

enum class ErrorType {
    NETWORK,    // Lỗi kết nối, timeout
    HTTP,       // Lỗi 4xx, 5xx
    EMPTY_BODY, // Phản hồi thành công nhưng body null
    UNKNOWN     // Lỗi không xác định
}
