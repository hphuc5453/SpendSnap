package com.spendsnap.app.data.remote.services

import com.spendsnap.app.data.remote.models.BaseResponse
import kotlinx.serialization.json.Json
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val exception: BaseException) : ApiResult<Nothing>()
    data class Loading(val isLoading: Boolean) : ApiResult<Nothing>()
}

abstract class BaseService {

    // Khởi tạo Json instance để parse error body
    private val json = Json { ignoreUnknownKeys = true }

    protected suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<BaseResponse<T>>
    ): ApiResult<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ApiResult.Success(body.data!!)
                } else {
                    ApiResult.Error(
                        BaseException(
                            message = "Phản hồi trống từ máy chủ",
                            code = response.code(),
                            type = ErrorType.EMPTY_BODY
                        )
                    )
                }
            } else {
                // Khi request thất bại, lấy JSON từ errorBody()
                val errorBodyString = response.errorBody()?.string()
                
                // Cố gắng parse message từ server
                val serverMessage = try {
                    if (!errorBodyString.isNullOrEmpty()) {
                        json.decodeFromString<BaseResponse<Unit>>(errorBodyString).message
                    } else null
                } catch (_: Exception) {
                    null
                }

                val defaultErrorMessage = when (response.code()) {
                    401 -> "Phiên đăng nhập hết hạn"
                    403 -> "Không có quyền truy cập"
                    404 -> "Không tìm thấy dữ liệu"
                    in 500..599 -> "Lỗi hệ thống máy chủ"
                    else -> "Lỗi không xác định (${response.code()})"
                }

                ApiResult.Error(
                    BaseException(
                        message = serverMessage ?: defaultErrorMessage,
                        code = response.code(),
                        type = ErrorType.HTTP
                    )
                )
            }
        } catch (e: Exception) {
            val exception = when (e) {
                is UnknownHostException -> BaseException("Không có kết nối Internet", type = ErrorType.NETWORK)
                is SocketTimeoutException -> BaseException("Kết nối quá hạn (Timeout)", type = ErrorType.NETWORK)
                is IOException -> BaseException("Lỗi đường truyền mạng", type = ErrorType.NETWORK)
                else -> BaseException("Lỗi hệ thống: ${e.localizedMessage}", type = ErrorType.UNKNOWN)
            }
            ApiResult.Error(exception)
        }
    }
}
