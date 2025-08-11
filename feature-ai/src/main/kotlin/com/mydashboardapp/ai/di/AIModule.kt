package com.mydashboardapp.ai.di

import com.mydashboardapp.ai.data.service.*
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AIModule {
    
    @Binds
    abstract fun bindAIService(
        aiServiceImpl: AIServiceImpl
    ): AIService
    
    companion object {
        
        @Provides
        @Singleton
        @Named("ai_okhttp")
        fun provideAIOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                )
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()
        }
        
        @Provides
        @Singleton
        fun provideJson(): Json {
            return Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
        }
        
        @Provides
        @Singleton
        @Named("openai_retrofit")
        fun provideOpenAIRetrofit(
            @Named("ai_okhttp") okHttpClient: OkHttpClient,
            json: Json
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl("https://api.openai.com/v1/")
                .client(okHttpClient)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
        }
        
        @Provides
        @Singleton
        @Named("anthropic_retrofit")
        fun provideAnthropicRetrofit(
            @Named("ai_okhttp") okHttpClient: OkHttpClient,
            json: Json
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl("https://api.anthropic.com/v1/")
                .client(okHttpClient)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
        }
        
        @Provides
        @Singleton
        @Named("google_retrofit")
        fun provideGoogleRetrofit(
            @Named("ai_okhttp") okHttpClient: OkHttpClient,
            json: Json
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1/")
                .client(okHttpClient)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
        }
        
        @Provides
        @Singleton
        fun provideOpenAIApi(@Named("openai_retrofit") retrofit: Retrofit): OpenAIApi {
            return retrofit.create(OpenAIApi::class.java)
        }
        
        @Provides
        @Singleton
        fun provideAnthropicApi(@Named("anthropic_retrofit") retrofit: Retrofit): AnthropicApi {
            return retrofit.create(AnthropicApi::class.java)
        }
        
        @Provides
        @Singleton
        fun provideGoogleAIApi(@Named("google_retrofit") retrofit: Retrofit): GoogleAIApi {
            return retrofit.create(GoogleAIApi::class.java)
        }
    }
}
