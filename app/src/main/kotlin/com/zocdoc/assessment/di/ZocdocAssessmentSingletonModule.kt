package com.zocdoc.assessment.di

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.zocdoc.assessment.BuildConfig
import com.zocdoc.assessment.data.api.ZocdocAssessmentApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ZocdocAssessmentSingletonModule {

    @Provides
    @Singleton
    fun providesJson() = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun providesMediaType() = "application/json; charset=UTF-8".toMediaType()

    @Provides
    @Singleton
    fun providesOkHttpLoggingInterceptor(): HttpLoggingInterceptor? =
        if (BuildConfig.DEBUG) HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        else null

    @Provides
    @Singleton
    fun providesOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor?
    ) = OkHttpClient.Builder().apply {
        if (loggingInterceptor != null) addInterceptor(loggingInterceptor)
    }.build()

    @Provides
    @Singleton
    fun providesRetrofitClient(
        json: Json,
        mediaType: MediaType,
        okHttpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("https://interview.zocdoc.com/api/1/FEE/")
        .addConverterFactory(json.asConverterFactory(mediaType))
        .build()

    @Provides
    @Singleton
    fun providesApi(
        retrofit: Retrofit
    ): ZocdocAssessmentApi = retrofit.create(ZocdocAssessmentApi::class.java)

    @Provides
    @Singleton
    fun providesImageLoader(
        @ApplicationContext context: PlatformContext,
        okHttpClient: OkHttpClient
    ) = ImageLoader.Builder(context)
        .components {
            add(
                OkHttpNetworkFetcherFactory(
                    callFactory = okHttpClient
                )
            )
        }.build()
}