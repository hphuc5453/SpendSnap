package com.spendsnap.app.shared

import android.util.Base64
import java.security.MessageDigest

/**
 * SHA-256 + Base64-encode mật khẩu trước khi gửi BE. Phải đồng nhất ở mọi luồng:
 * signin / signup / reset-password — nếu không sẽ không khớp hash đã lưu.
 */
fun encodePassword(password: String): String {
    return try {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
        Base64.encodeToString(hash, Base64.NO_WRAP)
    } catch (_: Exception) {
        password
    }
}
