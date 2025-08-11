package com.mydashboardapp.auth.di

import com.mydashboardapp.auth.data.firebase.FirebaseAuthRepository
import com.mydashboardapp.auth.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Firebase flavor dependency injection module for authentication
 * 
 * This module provides Firebase-based authentication implementation.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    
    /**
     * Binds the Firebase Auth implementation as the primary AuthRepository
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        firebaseAuthRepository: FirebaseAuthRepository
    ): AuthRepository
    
    companion object {
        /**
         * Provides Firebase Auth instance
         */
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth {
            return FirebaseAuth.getInstance()
        }
    }
}
