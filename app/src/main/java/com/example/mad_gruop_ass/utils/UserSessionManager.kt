package com.example.mad_gruop_ass.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class UserSessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val PREF_NAME = "user_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_AVATAR_URL = "avatar_url"
        private const val KEY_PHONE = "phone"
        private const val KEY_GENDER = "gender"
    }
    
    /**
     * 保存用户登录信息
     */
    fun saveUserInfo(userId: Int, username: String, email: String, avatarUrl: String?, phone: String?, gender: String?) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putInt(KEY_USER_ID, userId)
        editor.putString(KEY_USERNAME, username)
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_AVATAR_URL, avatarUrl)
        editor.putString(KEY_PHONE, phone)
        editor.putString(KEY_GENDER, gender)
        editor.apply()
    }
    
    /**
     * 检查是否已登录
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    /**
     * 获取用户ID
     */
    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }
    
    /**
     * 获取用户名
     */
    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }
    
    /**
     * 获取邮箱
     */
    fun getEmail(): String? {
        return prefs.getString(KEY_EMAIL, null)
    }
    
    /**
     * 获取头像URL
     */
    fun getAvatarUrl(): String? {
        return prefs.getString(KEY_AVATAR_URL, null)
    }
    
    /**
     * 获取手机号
     */
    fun getPhone(): String? {
        return prefs.getString(KEY_PHONE, null)
    }
    
    /**
     * 获取性别
     */
    fun getGender(): String? {
        return prefs.getString(KEY_GENDER, null)
    }
    
    /**
     * 清除用户会话（登出）
     */
    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
} 