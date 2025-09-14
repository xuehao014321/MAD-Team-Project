package com.example.mad_gruop_ass.api

import com.example.mad_gruop_ass.ApiClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val DEFAULT_BASE_URL = "http://192.168.0.103:5000/api/"  // 自动更新
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    
    /**
     * 获取当前的API基础URL，优先使用ApiClient的动态URL
     */
    private fun getCurrentBaseUrl(): String {
        return try {
            val apiClientUrl = ApiClient.getCurrentBaseUrl()
            if (apiClientUrl.endsWith("/api")) {
                "$apiClientUrl/"
            } else {
                "$apiClientUrl/"
            }
        } catch (e: Exception) {
            DEFAULT_BASE_URL
        }
    }
    
    /**
     * 创建Retrofit实例，使用动态URL
     */
    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(getCurrentBaseUrl())
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy { createRetrofit().create(ApiService::class.java) }
    
    /**
     * 重新创建API服务实例（当URL变更时使用）
     */
    fun refreshApiService(): ApiService {
        return createRetrofit().create(ApiService::class.java)
    }
}

