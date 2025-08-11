package com.mydashboardapp.auth.di

import com.mydashboardapp.auth.data.stub.StubAuthRepository
import com.mydashboardapp.auth.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Stub flavor dependency injection module for authentication
 * 
 * This module provides a mock/stub implementation for testing and development.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    
    /**
     * Binds the Stub Auth implementation as the primary AuthRepository
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        stubAuthRepository: StubAuthRepository
    ): AuthRepository
}
