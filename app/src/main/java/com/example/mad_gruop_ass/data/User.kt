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