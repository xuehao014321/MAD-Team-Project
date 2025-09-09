package com.example.mad_gruop_ass.api

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.mad_gruop_ass.data.LoginRequest
import com.example.mad_gruop_ass.data.LoginResponse
import com.example.mad_gruop_ass.data.SignUpRequest
import com.example.mad_gruop_ass.data.SignUpResponse
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object ApiClient {
    private const val TAG = "ApiClient"
    private const val BASE_URL = "http://192.168.0.104:5000/api"
    
    private val client = OkHttpClient()
    private val gson = Gson()
    private val mainHandler = Handler(Looper.getMainLooper())
    
    // 登录回调接口
    interface LoginCallback {
        fun onSuccess(response: LoginResponse)
        fun onError(error: String)
    }
    
    // 注册回调接口
    interface SignUpCallback {
        fun onSuccess(response: SignUpResponse)
        fun onError(error: String)
    }
    
    /**
     * 用户登录
     */
    fun login(loginRequest: LoginRequest, callback: LoginCallback) {
        val requestBody = gson.toJson(loginRequest)
            .toRequestBody("application/json".toMediaType())
        
        val request = Request.Builder()
            .url("$BASE_URL/login")
            .post(requestBody)
            .build()
        
        Log.d(TAG, "发送登录请求: ${gson.toJson(loginRequest)}")
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "登录请求失败", e)
                mainHandler.post { callback.onError("网络请求失败: ${e.message}") }
            }
            
            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string() ?: ""
                    Log.d(TAG, "登录响应: $responseBody")
                    
                    val loginResponse = gson.fromJson(responseBody, LoginResponse::class.java)
                    
                    if (response.isSuccessful && loginResponse.success) {
                        mainHandler.post { callback.onSuccess(loginResponse) }
                    } else {
                        val errorMessage = loginResponse.message ?: "登录失败"
                        mainHandler.post { callback.onError(errorMessage) }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "解析登录响应失败", e)
                    mainHandler.post { callback.onError("数据解析失败: ${e.message}") }
                }
            }
        })
    }
    
    /**
     * 用户注册
     */
    fun signUpUser(signUpRequest: SignUpRequest, callback: SignUpCallback) {
        val requestBody = gson.toJson(signUpRequest)
            .toRequestBody("application/json".toMediaType())
        
        val request = Request.Builder()
            .url("$BASE_URL/users")
            .post(requestBody)
            .build()
        
        Log.d(TAG, "发送注册请求: ${gson.toJson(signUpRequest)}")
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "注册请求失败", e)
                mainHandler.post { callback.onError("网络请求失败: ${e.message}") }
            }
            
            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string() ?: ""
                    Log.d(TAG, "注册响应: $responseBody")
                    
                    val signUpResponse = gson.fromJson(responseBody, SignUpResponse::class.java)
                    
                    if (response.isSuccessful && signUpResponse.success) {
                        mainHandler.post { callback.onSuccess(signUpResponse) }
                    } else {
                        val errorMessage = signUpResponse.message ?: "注册失败"
                        mainHandler.post { callback.onError(errorMessage) }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "解析注册响应失败", e)
                    mainHandler.post { callback.onError("数据解析失败: ${e.message}") }
                }
            }
        })
    }
}