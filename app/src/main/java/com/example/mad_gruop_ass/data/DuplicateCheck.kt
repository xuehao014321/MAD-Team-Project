package com.example.mad_gruop_ass.data

// 用户名重复检查请求数据类
data class CheckUsernameRequest(
    val username: String
)

// 邮箱重复检查请求数据类
data class CheckEmailRequest(
    val email: String
)

// 手机号重复检查请求数据类
data class CheckPhoneRequest(
    val phone: String
)

// 重复检查响应数据类
data class DuplicateCheckResponse(
    val success: Boolean,
    val message: String,
    val exists: Boolean,
    val available: Boolean = !exists  // 添加available字段，表示是否可用
)
