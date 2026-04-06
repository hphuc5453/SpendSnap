package com.spendsnap.app.di

import com.spendsnap.app.data.remote.repositories.AuthRepository
import com.spendsnap.app.data.remote.repositories.IAuthRepository
import com.spendsnap.app.data.remote.services.auth.AuthService
import com.spendsnap.app.data.remote.services.auth.IAuthService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthService(
        authService: AuthService
    ): IAuthService

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepository: AuthRepository
    ): IAuthRepository
}
