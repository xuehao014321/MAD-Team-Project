package com.example.mad_gruop_ass.api

import com.example.mad_gruop_ass.data.LoginRequest
import com.example.mad_gruop_ass.data.LoginResponse
import com.example.mad_gruop_ass.data.SignUpRequest
import com.example.mad_gruop_ass.data.SignUpResponse
import com.example.mad_gruop_ass.data.User
import com.example.mad_gruop_ass.data.CheckUsernameRequest
import com.example.mad_gruop_ass.data.CheckEmailRequest
import com.example.mad_gruop_ass.data.CheckPhoneRequest
import com.example.mad_gruop_ass.data.DuplicateCheckResponse
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
     * 检查用户名是否已存在
     */
    @POST("users/check-username")
    suspend fun checkUsername(@Body checkUsernameRequest: CheckUsernameRequest): Response<DuplicateCheckResponse>
    
    /**
     * 检查邮箱是否已存在
     */
    @POST("users/check-email")
    suspend fun checkEmail(@Body checkEmailRequest: CheckEmailRequest): Response<DuplicateCheckResponse>
    
    /**
     * 检查手机号是否已存在
     */
    @POST("users/check-phone")
    suspend fun checkPhone(@Body checkPhoneRequest: CheckPhoneRequest): Response<DuplicateCheckResponse>
    
    /**
     * 测试连接
     */
    @GET("test")
    suspend fun testConnection(): Response<Map<String, String>>
}
