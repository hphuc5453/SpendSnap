package com.spendsnap.app.di

import com.spendsnap.app.data.remote.repositories.AuthRepository
import com.spendsnap.app.data.remote.repositories.IAuthRepository
import com.spendsnap.app.data.remote.repositories.transactions.ITransactionRepository
import com.spendsnap.app.data.remote.repositories.transactions.TransactionRepository
import com.spendsnap.app.data.remote.repositories.user.IUserRepository
import com.spendsnap.app.data.remote.repositories.user.UserRepository
import com.spendsnap.app.data.remote.services.auth.AuthService
import com.spendsnap.app.data.remote.services.auth.IAuthService
import com.spendsnap.app.data.remote.services.transactions.ITransactionService
import com.spendsnap.app.data.remote.services.transactions.TransactionService
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

    @Binds
    @Singleton
    abstract fun bindTransactionService(
        transactionService: TransactionService
    ): ITransactionService

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        transactionRepository: TransactionRepository
    ) : ITransactionRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepository: UserRepository
    ) : IUserRepository
}
