package com.example.mad_gruop_ass

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.mad_gruop_ass.R
import com.example.mad_gruop_ass.api.RetrofitClient
import com.example.mad_gruop_ass.data.SignUpRequest
import com.example.mad_gruop_ass.data.SignUpResponse
import com.example.mad_gruop_ass.utils.NetworkUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.random.Random

class SignUpActivity : ComponentActivity() {
    
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var progressBar: ProgressBar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        
        initializeViews()
        setupGenderSpinner()
        setupTextWatchers()
        setupSignUpButton()
    }
    
    private fun initializeViews() {
        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        genderSpinner = findViewById(R.id.genderSpinner)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        signUpButton = findViewById(R.id.signUpButton)
        progressBar = findViewById(R.id.progressBar)
    }
    
    private fun setupGenderSpinner() {
        val genderOptions = resources.getStringArray(R.array.gender_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = adapter
    }
    
    private fun setupTextWatchers() {
        // 手机号格式实时验证
        phoneEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val phone = s.toString().trim()
                if (phone.isNotEmpty() && !isValidPhoneNumber(phone)) {
                    phoneEditText.error = "手机号必须以0开头，包含10-11位数字"
                } else {
                    phoneEditText.error = null
                }
            }
        })
        
        // 邮箱格式实时验证
        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().trim()
                if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailEditText.error = "请输入有效的邮箱地址"
                } else {
                    emailEditText.error = null
                }
            }
        })
        
        // 密码确认实时验证
        confirmPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val password = passwordEditText.text.toString()
                val confirmPassword = s.toString()
                if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                    confirmPasswordEditText.error = "密码不匹配"
                } else {
                    confirmPasswordEditText.error = null
                }
            }
        })
    }
    
    private fun setupSignUpButton() {
        signUpButton.setOnClickListener {
            performSignUp()
        }
    }
    
    private fun performSignUp() {
        if (!validateInputs()) {
            return
        }
        
        val username = usernameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()
        val gender = genderSpinner.selectedItem.toString()
        val password = passwordEditText.text.toString()
        
        // 检查网络连接
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "无网络连接，请检查网络设置", Toast.LENGTH_LONG).show()
            return
        }
        
        // 记录网络信息用于调试
        NetworkUtils.logNetworkInfo(this)
        
        // 显示加载状态
        showLoading(true)
        signUpButton.isEnabled = false
        signUpButton.text = "注册中..."
        
        // 创建注册请求
        val signUpRequest = SignUpRequest(
            username = username,
            email = email,
            password = password,
            phone = phone,
            gender = gender,
            avatar_url = "https://i.pravatar.cc/150?img=",
            created_at = getCurrentTimeISO(),
            distance = generateRandomDistance()
        )
        
        // 使用RetrofitClient和协程调用API进行注册
        lifecycleScope.launch {
            try {
                // 先测试服务器连接
                Log.d("SignUpActivity", "测试服务器连接...")
                val connectionResponse = RetrofitClient.apiService.testConnection()
                
                if (!connectionResponse.isSuccessful) {
                    showLoading(false)
                    signUpButton.isEnabled = true
                    signUpButton.text = "注册"
                    Toast.makeText(
                        this@SignUpActivity,
                        " 无法连接到服务器，请检查:\n1. 服务器是否运行\n2. 网络连接\n3. 防火墙设置",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }
                
                Log.d("SignUpActivity", " 服务器连接成功，开始注册...")
                Log.d("SignUpActivity", "发送注册请求: ")
                Log.d("SignUpActivity", "请求URL: users")
                
                // 调用注册API
                val response = RetrofitClient.apiService.signUp(signUpRequest)
                
                showLoading(false)
                signUpButton.isEnabled = true
                signUpButton.text = "注册"
                
                Log.d("SignUpActivity", "响应代码: ")
                Log.d("SignUpActivity", "响应消息: ")
                Log.d("SignUpActivity", "响应体: ")
                
                if (response.isSuccessful) {
                    val signUpResponse = response.body()
                    if (signUpResponse != null && signUpResponse.success) {
                        Log.d("SignUpActivity", " 注册成功: ")
                        
                        // 显示成功消息
                        Toast.makeText(
                            this@SignUpActivity, 
                            " ", 
                            Toast.LENGTH_LONG
                        ).show()
                        
                        // 注册成功后跳转到登录页面
                        val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMessage = signUpResponse?.message ?: "注册失败"
                        Log.e("SignUpActivity", " 注册失败: ")
                        Toast.makeText(
                            this@SignUpActivity, 
                            " ", 
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    // 尝试解析错误响应
                    val errorBody = response.errorBody()?.string()
                    Log.e("SignUpActivity", "错误响应体: ")
                    
                    val errorMessage = try {
                        // 尝试解析错误响应中的消息
                        val errorResponse = com.google.gson.Gson().fromJson(errorBody, SignUpResponse::class.java)
                        errorResponse?.message ?: "服务器错误 : "
                    } catch (e: Exception) {
                        "服务器错误 : "
                    }
                    
                    Log.e("SignUpActivity", " 注册失败: ")
                    Toast.makeText(
                        this@SignUpActivity, 
                        " ", 
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                showLoading(false)
                signUpButton.isEnabled = true
                signUpButton.text = "注册"
                Log.e("SignUpActivity", " 注册请求异常", e)
                Toast.makeText(
                    this@SignUpActivity, 
                    " 连接失败: \n\n请检查:\n1. 服务器是否运行\n2. 网络连接\n3. 防火墙设置", 
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun validateInputs(): Boolean {
        val username = usernameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()
        val gender = genderSpinner.selectedItem.toString()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()
        
        // 验证用户名
        if (username.isEmpty()) {
            usernameEditText.error = "请输入用户名"
            usernameEditText.requestFocus()
            return false
        }
        
        if (username.length < 3) {
            usernameEditText.error = "用户名至少3个字符"
            usernameEditText.requestFocus()
            return false
        }
        
        // 验证邮箱
        if (email.isEmpty()) {
            emailEditText.error = "请输入邮箱地址"
            emailEditText.requestFocus()
            return false
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "请输入有效的邮箱地址"
            emailEditText.requestFocus()
            return false
        }
        
        // 验证手机号
        if (phone.isEmpty()) {
            phoneEditText.error = "请输入手机号"
            phoneEditText.requestFocus()
            return false
        }
        
        if (!isValidPhoneNumber(phone)) {
            phoneEditText.error = "手机号必须以0开头，包含10-11位数字"
            phoneEditText.requestFocus()
            return false
        }
        
        // 验证性别
        if (gender == "--Please Select--") {
            Toast.makeText(this, "请选择性别", Toast.LENGTH_SHORT).show()
            return false
        }
        
        // 验证密码
        if (password.isEmpty()) {
            passwordEditText.error = "请输入密码"
            passwordEditText.requestFocus()
            return false
        }
        
        if (password.length < 6) {
            passwordEditText.error = "密码长度至少6位"
            passwordEditText.requestFocus()
            return false
        }
        
        // 验证确认密码
        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.error = "请确认密码"
            confirmPasswordEditText.requestFocus()
            return false
        }
        
        if (password != confirmPassword) {
            confirmPasswordEditText.error = "密码不匹配"
            confirmPasswordEditText.requestFocus()
            return false
        }
        
        return true
    }
    
    private fun isValidPhoneNumber(phone: String): Boolean {
        // 验证手机号格式：以0开头，总共10-11位数字
        val phoneRegex = Regex("^0\\d{9,10}$")
        return phoneRegex.matches(phone)
    }
    
    private fun showLoading(show: Boolean) {
        if (show) {
            progressBar.visibility = View.VISIBLE
            signUpButton.isEnabled = false
            signUpButton.text = "注册中..."
        } else {
            progressBar.visibility = View.GONE
            signUpButton.isEnabled = true
            signUpButton.text = "Sign Up"
        }
    }

    /**
     * 获取当前时间的ISO格式字符串（马来西亚时间 UTC+8）
     * 格式: 2025-09-09T14:08:44.000Z
     */
    private fun getCurrentTimeISO(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur") // 马来西亚时区 UTC+8
        return dateFormat.format(Date())
    }
    
    /**
     * 生成0.0-10.0范围内的随机浮点数
     */
    private fun generateRandomDistance(): Double {
        return Random.nextDouble(0.0, 10.0)
    }
}
