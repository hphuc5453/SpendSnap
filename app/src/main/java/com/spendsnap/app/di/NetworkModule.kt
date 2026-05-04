package com.spendsnap.app.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.spendsnap.app.BuildConfig
import com.spendsnap.app.data.local.AuthManager
import com.spendsnap.app.data.remote.clients.AuthClient
import com.spendsnap.app.data.remote.clients.TransactionClient
import com.spendsnap.app.data.remote.clients.UserClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(authManager: AuthManager): Interceptor {
        return Interceptor { chain ->
            // Lấy token từ DataStore
            val token = runBlocking {
                authManager.accessToken.firstOrNull()
            }

            val requestBuilder = chain.request().newBuilder()
            
            // Nếu có token thì gắn vào Header
            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // Gắn Interceptor tự động thêm Token
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthClient(retrofit: Retrofit): AuthClient {
        return retrofit.create(AuthClient::class.java)
    }

    @Provides
    @Singleton
    fun provideTransactionClient(retrofit: Retrofit): TransactionClient {
        return retrofit.create(TransactionClient::class.java)
    }

    @Provides
    @Singleton
    fun provideUserClient(retrofit: Retrofit): UserClient {
        return retrofit.create(UserClient::class.java)
    }
}
