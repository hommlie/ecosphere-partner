package com.ecosphere.partner.core.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RefreshRetrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkBindModule {

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(
        impl: NetworkMonitorImpl
    ): NetworkMonitor
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson =
        GsonBuilder().create()

    @Provides
    @Singleton
    fun provideGsonConverterFactory(
        gson: Gson
    ): GsonConverterFactory =
        GsonConverterFactory.create(gson)

    @Provides
    @Singleton
    @RefreshRetrofit
    fun provideRefreshRetrofit(
        factory: GsonConverterFactory
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(AppConfig.BASE_URL)
            .addConverterFactory(factory)
            .build()

    @Provides
    @Singleton
    fun provideAuthenticatorApi(
        @RefreshRetrofit retrofit: Retrofit
    ): AuthApiInterface =
        retrofit.create(AuthApiInterface::class.java)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        loggingInterceptor: LoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(
                NetworkConstants.CONNECT_TIMEOUT,
                NetworkConstants.TIME_UNIT)
            .readTimeout(
                NetworkConstants.READ_TIMEOUT,
                NetworkConstants.TIME_UNIT)
            .writeTimeout(
                NetworkConstants.WRITE_TIMEOUT,
                NetworkConstants.TIME_UNIT)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor.create())
            .authenticator(tokenAuthenticator)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        factory: GsonConverterFactory
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(AppConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(factory)
            .build()

    @Provides
    @Singleton
    fun provideApiService(
        retrofit: Retrofit
    ): ApiService =
        retrofit.create(ApiService::class.java)
}