package com.example.mad_gruop_ass

import android.app.AlertDialog
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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.mad_gruop_ass.R
import com.example.mad_gruop_ass.api.RetrofitClient
import com.example.mad_gruop_ass.data.SignUpRequest
import com.example.mad_gruop_ass.data.SignUpResponse
import com.example.mad_gruop_ass.data.CheckUsernameRequest
import com.example.mad_gruop_ass.data.CheckEmailRequest
import com.example.mad_gruop_ass.data.CheckPhoneRequest
import com.example.mad_gruop_ass.utils.NetworkUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.random.Random
import kotlinx.coroutines.delay

class SignUpActivity : ComponentActivity() {
    
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var loginLink: TextView
    
    // 添加提示文本视图
    private lateinit var usernameHintText: TextView
    private lateinit var emailHintText: TextView
    private lateinit var phoneHintText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        
        initializeViews()
        setupGenderSpinner()
        setupTextWatchers()
        setupSignUpButton()
        setupLoginLink()
        
        // 添加测试按钮（临时调试用）
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
        loginLink = findViewById(R.id.loginLink)
        
        // 初始化提示文本视图
        usernameHintText = findViewById(R.id.usernameHintText)
        emailHintText = findViewById(R.id.emailHintText)
        phoneHintText = findViewById(R.id.phoneHintText)
    }
    
    private fun setupGenderSpinner() {
        val genderOptions = resources.getStringArray(R.array.gender_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = adapter
    }
    
    private fun setupLoginLink() {
        loginLink.setOnClickListener {
            // 返回登录界面
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
    
    private fun setupTextWatchers() {
        // 用户名格式验证（移除实时重复检查）
        usernameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val username = s.toString().trim()
                
                // 清除邮箱的错误状态
                emailEditText.background = getDrawable(R.drawable.input_background)
                
                if (username.isNotEmpty()) {
                    if (username.length < 3) {
                        showUsernameHint("Username must be at least 3 characters", false)
                        usernameEditText.error = "Username must be at least 3 characters"
                    } else {
                        showUsernameHint("✓ Username format is valid", true)
                        usernameEditText.error = null
                    }
                } else {
                    clearUsernameHint()
                    usernameEditText.error = null
                }
            }
        })
        
        // 手机号格式验证（移除实时重复检查）
        phoneEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val phone = s.toString().trim()
                
                // 清除邮箱的错误状态
                emailEditText.background = getDrawable(R.drawable.input_background)
                
                if (phone.isNotEmpty()) {
                    if (!isValidPhoneNumber(phone)) {
                        showPhoneHint("Phone number must start with 0 and contain 10-11 digits", false)
                        phoneEditText.error = "Phone number must start with 0 and contain 10-11 digits"
                    } else {
                        showPhoneHint("✓ Phone number format is valid", true)
                        phoneEditText.error = null
                    }
                } else {
                    clearPhoneHint()
                    phoneEditText.error = null
                }
            }
        })
        
        // 邮箱格式验证（移除实时重复检查）
        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().trim()
                
                // 清除错误状态（包括红色边框）
                emailEditText.background = getDrawable(R.drawable.input_background)
                
                if (email.isNotEmpty()) {
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        showEmailHint("Please enter a valid email address", false)
                        emailEditText.error = "Please enter a valid email address"
                    } else {
                        showEmailHint("✓ Email format is valid", true)
                        emailEditText.error = null
                    }
                } else {
                    clearEmailHint()
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
                    confirmPasswordEditText.error = "Passwords do not match"
                } else {
                    confirmPasswordEditText.error = null
                }
            }
        })
    }
    
    // 显示用户名提示
    private fun showUsernameHint(message: String, isSuccess: Boolean) {
        usernameHintText.text = message
        usernameHintText.setTextColor(
            if (isSuccess) getColor(R.color.neighborlink_bright_green) 
            else getColor(R.color.hint_color)
        )
        usernameHintText.visibility = View.VISIBLE
    }
    
    // 清除用户名提示
    private fun clearUsernameHint() {
        usernameHintText.visibility = View.GONE
    }
    
    // 显示邮箱提示
    private fun showEmailHint(message: String, isSuccess: Boolean) {
        emailHintText.text = message
        emailHintText.setTextColor(
            if (isSuccess) getColor(R.color.neighborlink_bright_green) 
            else getColor(R.color.hint_color)
        )
        emailHintText.visibility = View.VISIBLE
    }
    
    // 清除邮箱提示
    private fun clearEmailHint() {
        emailHintText.visibility = View.GONE
    }
    
    // 显示手机号提示
    private fun showPhoneHint(message: String, isSuccess: Boolean) {
        phoneHintText.text = message
        phoneHintText.setTextColor(
            if (isSuccess) getColor(R.color.neighborlink_bright_green) 
            else getColor(R.color.hint_color)
        )
        phoneHintText.visibility = View.VISIBLE
    }
    
    // 清除手机号提示
    private fun clearPhoneHint() {
        phoneHintText.visibility = View.GONE
    }
    
    // 测试服务器连接
    private fun testServerConnection() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.testConnection()
                if (response.isSuccessful) {
                    Toast.makeText(this@SignUpActivity, "Server connection successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@SignUpActivity, "Server connection failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SignUpActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
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
            Toast.makeText(this, "No network connection, please check your network settings", Toast.LENGTH_LONG).show()
            return
        }
        
        // 显示加载状态
        showLoading(true)
        signUpButton.isEnabled = false
        signUpButton.text = "Signing Up..."
        
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
                Log.d("SignUpActivity", "Testing server connection...")
                val connectionResponse = RetrofitClient.apiService.testConnection()
                
                if (!connectionResponse.isSuccessful) {
                    showLoading(false)
                    signUpButton.isEnabled = true
                    signUpButton.text = "Sign Up"
                    Toast.makeText(
                        this@SignUpActivity,
                        "Unable to connect to server, please check:\n1. Server is running\n2. Network connection\n3. Firewall settings",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }
                
                Log.d("SignUpActivity", "Server connection successful, starting registration...")
                
                // 调用注册API
                val response = RetrofitClient.apiService.signUp(signUpRequest)
                
                showLoading(false)
                signUpButton.isEnabled = true
                signUpButton.text = "Sign Up"
                
                if (response.isSuccessful) {
                    val signUpResponse = response.body()
                    if (signUpResponse != null && signUpResponse.success) {
                        Log.d("SignUpActivity", "Registration successful")
                        
                        // 显示成功消息
                        Toast.makeText(
                            this@SignUpActivity, 
                            "Registration successful! Please login with your credentials.", 
                            Toast.LENGTH_LONG
                        ).show()
                        
                        // 注册成功后跳转到登录页面
                        val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMessage = signUpResponse?.message ?: "Registration Failed"
                        Log.e("SignUpActivity", "Registration failed: $errorMessage")
                        Toast.makeText(
                            this@SignUpActivity, 
                            "Registration failed: $errorMessage", 
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    // 处理注册失败的情况
                    val errorBody = response.errorBody()?.string()
                    Log.e("SignUpActivity", "Error response body: $errorBody")
                    
                    val errorMessage = try {
                        val errorResponse = com.google.gson.Gson().fromJson(errorBody, SignUpResponse::class.java)
                        errorResponse?.message ?: "Server error: ${response.code()}"
                    } catch (e: Exception) {
                        "Server error: ${response.code()}"
                    }
                    
                    Log.e("SignUpActivity", "Registration failed: $errorMessage")
                    
                    // 根据错误类型显示具体的错误提示
                    handleRegistrationError(errorMessage, errorBody)
                }
            } catch (e: Exception) {
                showLoading(false)
                signUpButton.isEnabled = true
                signUpButton.text = "Sign Up"
                Log.e("SignUpActivity", "Registration request exception", e)
                Toast.makeText(
                    this@SignUpActivity, 
                    "Connection failed: \n\nPlease check:\n1. Server is running\n2. Network connection\n3. Firewall settings", 
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    /**
     * 处理注册错误，根据错误类型显示具体提示并高亮对应字段
     */
    private fun handleRegistrationError(errorMessage: String, errorBody: String?) {
        // 清除所有字段的错误状态
        clearAllFieldErrors()
        
        // 为邮箱输入框添加红色边框
        emailEditText.background = getDrawable(R.drawable.input_background_error)
        emailEditText.requestFocus()
        
        // 显示错误提示
        showEmailHint("✗ This email is already registered", false)
        emailEditText.error = "This email is already registered"
        
        // 显示弹窗提示
        showErrorDialog("Registration Failed", 
            "This email address is already registered. Please use a different email address.")
    }
    
    /**
     * 清除所有输入字段的错误状态
     */
    private fun clearAllFieldErrors() {
        usernameEditText.error = null
        emailEditText.error = null
        phoneEditText.error = null
        passwordEditText.error = null
        confirmPasswordEditText.error = null
        
        // 恢复正常的输入框背景
        emailEditText.background = getDrawable(R.drawable.input_background)
        
        // 清除所有提示
        clearUsernameHint()
        clearEmailHint()
        clearPhoneHint()
    }
    
    /**
     * 显示错误对话框
     */
    private fun showErrorDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
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
            showUsernameHint("Please enter username", false)
            usernameEditText.error = "Please enter username"
            usernameEditText.requestFocus()
            return false
        }
        
        if (username.length < 3) {
            showUsernameHint("Username must be at least 3 characters", false)
            usernameEditText.error = "Username must be at least 3 characters"
            usernameEditText.requestFocus()
            return false
        }
        
        // 验证邮箱
        if (email.isEmpty()) {
            showEmailHint("Please enter email address", false)
            emailEditText.error = "Please enter email address"
            emailEditText.requestFocus()
            return false
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showEmailHint("Please enter a valid email address", false)
            emailEditText.error = "Please enter a valid email address"
            emailEditText.requestFocus()
            return false
        }
        
        // 验证手机号
        if (phone.isEmpty()) {
            showPhoneHint("Please enter phone number", false)
            phoneEditText.error = "Please enter phone number"
            phoneEditText.requestFocus()
            return false
        }
        
        if (!isValidPhoneNumber(phone)) {
            showPhoneHint("Phone number must start with 0 and contain 10-11 digits", false)
            phoneEditText.error = "Phone number must start with 0 and contain 10-11 digits"
            phoneEditText.requestFocus()
            return false
        }
        
        // 验证性别
        if (gender == "--Please Select--") {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show()
            return false
        }
        
        // 验证密码
        if (password.isEmpty()) {
            passwordEditText.error = "Please enter password"
            passwordEditText.requestFocus()
            return false
        }
        
        if (password.length < 6) {
            passwordEditText.error = "Password must be at least 6 characters"
            passwordEditText.requestFocus()
            return false
        }
        
        // 验证确认密码
        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.error = "Please confirm password"
            confirmPasswordEditText.requestFocus()
            return false
        }
        
        if (password != confirmPassword) {
            confirmPasswordEditText.error = "Passwords do not match"
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
            signUpButton.text = "Signing Up..."
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
    
    override fun onDestroy() {
        super.onDestroy()
        // 取消所有检查任务
        // usernameCheckJob?.cancel()
        // emailCheckJob?.cancel()
        // phoneCheckJob?.cancel()
    }
}

