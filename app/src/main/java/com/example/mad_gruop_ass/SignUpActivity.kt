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

class SignUpActivity : ComponentActivity() {
    
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var progressBar: ProgressBar
    
    // 添加重复检查状态变量
    private var isCheckingUsername = false
    private var isCheckingEmail = false
    private var isCheckingPhone = false
    
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
        // 用户名重复检查
        usernameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val username = s.toString().trim()
                if (username.isNotEmpty() && username.length >= 3) {
                    checkUsernameAvailability(username)
                } else if (username.isNotEmpty()) {
                    usernameEditText.error = "Username must be at least 3 characters"
                } else {
                    usernameEditText.error = null
                }
            }
        })
        
        // 手机号格式验证和重复检查
        phoneEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val phone = s.toString().trim()
                if (phone.isNotEmpty()) {
                    if (!isValidPhoneNumber(phone)) {
                        phoneEditText.error = "Phone number must start with 0 and contain 10-11 digits"
                    } else {
                        phoneEditText.error = null
                        checkPhoneAvailability(phone)
                    }
                } else {
                    phoneEditText.error = null
                }
            }
        })
        
        // 邮箱格式验证和重复检查
        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().trim()
                if (email.isNotEmpty()) {
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        emailEditText.error = "Please enter a valid email address"
                    } else {
                        emailEditText.error = null
                        checkEmailAvailability(email)
                    }
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
                    confirmPasswordEditText.error = "Passwords do not match"
                } else {
                    confirmPasswordEditText.error = null
                }
            }
        })
    }
    
    // 检查用户名是否可用
    private fun checkUsernameAvailability(username: String) {
        if (isCheckingUsername) return
        isCheckingUsername = true
        
        lifecycleScope.launch {
            try {
                val request = CheckUsernameRequest(username)
                val response = RetrofitClient.apiService.checkUsername(request)
                isCheckingUsername = false
                
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result?.available == true) {
                        usernameEditText.error = null
                    } else {
                        usernameEditText.error = "Username is already taken"
                    }
                } else {
                    Log.e("SignUpActivity", "Check username failed: ${response.code()}")
                }
            } catch (e: Exception) {
                isCheckingUsername = false
                Log.e("SignUpActivity", "Check username exception", e)
            }
        }
    }
    
    // 检查邮箱是否可用
    private fun checkEmailAvailability(email: String) {
        if (isCheckingEmail) return
        isCheckingEmail = true
        
        lifecycleScope.launch {
            try {
                val request = CheckEmailRequest(email)
                val response = RetrofitClient.apiService.checkEmail(request)
                isCheckingEmail = false
                
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result?.available == true) {
                        emailEditText.error = null
                    } else {
                        emailEditText.error = "Email is already registered"
                    }
                } else {
                    Log.e("SignUpActivity", "Check email failed: ${response.code()}")
                }
            } catch (e: Exception) {
                isCheckingEmail = false
                Log.e("SignUpActivity", "Check email exception", e)
            }
        }
    }
    
    // 检查手机号是否可用
    private fun checkPhoneAvailability(phone: String) {
        if (isCheckingPhone) return
        isCheckingPhone = true
        
        lifecycleScope.launch {
            try {
                val request = CheckPhoneRequest(phone)
                val response = RetrofitClient.apiService.checkPhone(request)
                isCheckingPhone = false
                
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result?.available == true) {
                        phoneEditText.error = null
                    } else {
                        phoneEditText.error = "Phone number is already registered"
                    }
                } else {
                    Log.e("SignUpActivity", "Check phone failed: ${response.code()}")
                }
            } catch (e: Exception) {
                isCheckingPhone = false
                Log.e("SignUpActivity", "Check phone exception", e)
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
        
        // 记录网络信息用于调试
        NetworkUtils.logNetworkInfo(this)
        
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
                Log.d("SignUpActivity", "Sending registration request: ")
                Log.d("SignUpActivity", "Request URL: users")
                
                // 调用注册API
                val response = RetrofitClient.apiService.signUp(signUpRequest)
                
                showLoading(false)
                signUpButton.isEnabled = true
                signUpButton.text = "Sign Up"
                
                Log.d("SignUpActivity", "Response code: ")
                Log.d("SignUpActivity", "Response message: ")
                Log.d("SignUpActivity", "Response body: ")
                
                if (response.isSuccessful) {
                    val signUpResponse = response.body()
                    if (signUpResponse != null && signUpResponse.success) {
                        Log.d("SignUpActivity", "Registration successful: ")
                        
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
                        Log.e("SignUpActivity", "Registration failed: ")
                        Toast.makeText(
                            this@SignUpActivity, 
                            "Registration failed: $errorMessage", 
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    // 尝试解析错误响应
                    val errorBody = response.errorBody()?.string()
                    Log.e("SignUpActivity", "Error response body: $errorBody")
                    
                    val errorMessage = try {
                        // 尝试解析错误响应中的消息
                        val errorResponse = com.google.gson.Gson().fromJson(errorBody, SignUpResponse::class.java)
                        errorResponse?.message ?: "Server error: ${response.code()}"
                    } catch (e: Exception) {
                        "Server error: ${response.code()}"
                    }
                    
                    Log.e("SignUpActivity", "Registration failed: $errorMessage")
                    
                    // 根据错误类型显示具体的错误提示并高亮对应字段
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
        
        when {
            // 邮箱重复错误
            errorMessage.contains("email", ignoreCase = true) && 
            (errorMessage.contains("duplicate", ignoreCase = true) || 
             errorMessage.contains("already", ignoreCase = true) ||
             errorMessage.contains("exists", ignoreCase = true)) -> {
                emailEditText.error = "This email is already registered"
                emailEditText.requestFocus()
                showErrorDialog("Email Already Exists", 
                    "The email address you entered is already in use.\\n\\n" +
                    "Please try with a different email address.")
            }
            
            // 用户名重复错误
            errorMessage.contains("username", ignoreCase = true) && 
            (errorMessage.contains("duplicate", ignoreCase = true) || 
             errorMessage.contains("already", ignoreCase = true) ||
             errorMessage.contains("exists", ignoreCase = true)) -> {
                usernameEditText.error = "This username is already taken"
                usernameEditText.requestFocus()
                showErrorDialog("Username Already Exists", 
                    "The username you entered is already taken.\\n\\n" +
                    "Please choose a different username.")
            }
            
            // 手机号重复错误
            errorMessage.contains("phone", ignoreCase = true) && 
            (errorMessage.contains("duplicate", ignoreCase = true) || 
             errorMessage.contains("already", ignoreCase = true) ||
             errorMessage.contains("exists", ignoreCase = true)) -> {
                phoneEditText.error = "This phone number is already registered"
                phoneEditText.requestFocus()
                showErrorDialog("Phone Number Already Exists", 
                    "The phone number you entered is already in use.\\n\\n" +
                    "Please try with a different phone number.")
            }
            
            // 邮箱格式错误
            errorMessage.contains("email", ignoreCase = true) && 
            (errorMessage.contains("format", ignoreCase = true) || 
             errorMessage.contains("invalid", ignoreCase = true)) -> {
                emailEditText.error = "Please enter a valid email address"
                emailEditText.requestFocus()
                showErrorDialog("Invalid Email Format", 
                    "The email format you entered is incorrect.\\n\\n" +
                    "Please enter a valid email address.\\n" +
                    "Example: user@example.com")
            }
            
            // 密码相关错误
            errorMessage.contains("password", ignoreCase = true) -> {
                passwordEditText.error = "Password requirements not met"
                passwordEditText.requestFocus()
                showErrorDialog("Password Requirements Not Met", 
                    "Your password doesn't meet the security requirements.\\n\\n" +
                    "Password must be at least 6 characters long\\n" +
                    "and contain both letters and numbers.")
            }
            
            // 手机号格式错误
            errorMessage.contains("phone", ignoreCase = true) && 
            (errorMessage.contains("format", ignoreCase = true) || 
             errorMessage.contains("invalid", ignoreCase = true)) -> {
                phoneEditText.error = "Please enter a valid phone number"
                phoneEditText.requestFocus()
                showErrorDialog("Invalid Phone Number Format", 
                    "The phone number format is incorrect.\\n\\n" +
                    "Phone number must start with 0\\n" +
                    "and contain 10-11 digits.")
            }
            
            // 其他服务器错误
            else -> {
                showErrorDialog("Registration Failed", 
                    "We're sorry, but your registration could not be completed.\\n\\n" +
                    "Error: \$errorMessage\\n\\n" +
                    "Please check your information and try again.")
            }
        }
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
            usernameEditText.error = "Please enter username"
            usernameEditText.requestFocus()
            return false
        }
        
        if (username.length < 3) {
            usernameEditText.error = "Username must be at least 3 characters"
            usernameEditText.requestFocus()
            return false
        }
        
        // 验证邮箱
        if (email.isEmpty()) {
            emailEditText.error = "Please enter email address"
            emailEditText.requestFocus()
            return false
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Please enter a valid email address"
            emailEditText.requestFocus()
            return false
        }
        
        // 验证手机号
        if (phone.isEmpty()) {
            phoneEditText.error = "Please enter phone number"
            phoneEditText.requestFocus()
            return false
        }
        
        if (!isValidPhoneNumber(phone)) {
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
}




