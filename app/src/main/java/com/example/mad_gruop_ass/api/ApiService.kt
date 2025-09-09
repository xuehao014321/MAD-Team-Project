package com.example.mad_gruop_ass.api

import com.example.mad_gruop_ass.data.LoginRequest
import com.example.mad_gruop_ass.data.LoginResponse
import com.example.mad_gruop_ass.data.SignUpRequest
import com.example.mad_gruop_ass.data.SignUpResponse
import com.example.mad_gruop_ass.data.User
import retrofit2.Response
import retrofit2.http.*

/**
 * API服务接口
 * 定义所有与后端服务器通信的API端点
 */
interface ApiService {
    
    /**
     * 用户登录
     */
    @POST("login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
    
    /**
     * 用户注册
     */
    @POST("users")
    suspend fun signUp(@Body signUpRequest: SignUpRequest): Response<SignUpResponse>
    
    /**
     * 获取所有用户
     */
    @GET("users")
    suspend fun getAllUsers(): Response<List<User>>
    
    /**
     * 测试连接
     */
    @GET("test")
    suspend fun testConnection(): Response<Map<String, String>>
}