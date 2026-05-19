package com.spendsnap.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.spendsnap.app.data.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDatabase: AppDatabase
) {
    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
    }

    // Lưu Access Token
    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = token
        }
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN]
    }

    suspend fun clearAuth() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
        }
    }

    /**
     * Xóa toàn bộ session của user hiện tại: token + mọi bảng Room.
     * Gọi khi logout, và như safety-net trước khi đăng nhập user mới
     * (phòng TH app crash giữa logout & login khiến dữ liệu user cũ còn sót).
     */
    suspend fun clearAllUserData() {
        withContext(Dispatchers.IO) {
            appDatabase.clearAllTables()
        }
        clearAuth()
    }
}