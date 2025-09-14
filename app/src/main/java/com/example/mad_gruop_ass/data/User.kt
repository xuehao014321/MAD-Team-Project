package com.example.mad_gruop_ass.data

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("phone")
    val phone: String?,
    
    @SerializedName("gender")
    val gender: String?,
    
    @SerializedName("avatar_url")
    val avatarUrl: String?,
    
    @SerializedName("created_at")
    val createdAt: String?,
    
    @SerializedName("distance")
    val distance: Double?
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: User?
)

// 用户注册请求数据类
data class SignUpRequest(
    val username: String,
    val email: String,
    val password: String,
    val phone: String,
    val gender: String,
    val avatar_url: String,
    val created_at: String,
    val distance: Double
)

// 用户注册响应数据类
data class SignUpResponse(
    val success: Boolean,
    val message: String,
    val user: User?
)

// 检查用户名请求
data class CheckUsernameRequest(
    val username: String
)

// 检查邮箱请求
data class CheckEmailRequest(
    val email: String
)

// 检查手机号请求
data class CheckPhoneRequest(
    val phone: String
)

// 重复检查响应
data class DuplicateCheckResponse(
    val exists: Boolean,
    val message: String
)

