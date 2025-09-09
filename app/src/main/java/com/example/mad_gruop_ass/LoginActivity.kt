package com.example.mad_gruop_ass

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.mad_gruop_ass.R
import com.example.mad_gruop_ass.api.RetrofitClient
import com.example.mad_gruop_ass.data.LoginRequest
import com.example.mad_gruop_ass.data.User
import com.example.mad_gruop_ass.utils.NetworkUtils
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpButton: Button
    private lateinit var debugButton: Button
    private lateinit var progressBar: ProgressBar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        initViews()
        setupClickListeners()
    }
    
    private fun initViews() {
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        signUpButton = findViewById(R.id.signUpButton)
        debugButton = findViewById(R.id.debugButton)
        progressBar = findViewById(R.id.progressBar)
    }
    
    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            performLogin()
        }
        
        signUpButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        
        debugButton.setOnClickListener {
            testServerConnection()
        }
    }
    
    private fun performLogin() {
        val identifier = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        
        // Validate inputs
        if (!validateInputs(identifier, password)) {
            return
        }
        
        // Check network connectivity
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No network connection available", Toast.LENGTH_LONG).show()
            return
        }
        
        // Log network info for debugging
        NetworkUtils.logNetworkInfo(this)
        
        // Show loading
        showLoading(true)
        
        // 使用简单的逻辑：获取所有用户，然后查找匹配的Gmail和密码
        lifecycleScope.launch {
            try {
                // 获取所有用户
                val response = RetrofitClient.apiService.getAllUsers()
                
                if (response.isSuccessful) {
                    val users: List<User> = response.body() ?: emptyList()
                    
                    // 查找匹配的用户
                    val matchedUser = users.find { user ->
                        // 支持邮箱或手机号登录
                        val emailMatch = user.email.equals(identifier, ignoreCase = true)
                        val phoneMatch = user.phone == identifier
                        (emailMatch || phoneMatch) && user.password == password
                    }
                    
                    showLoading(false)
                    
                    if (matchedUser != null) {
                        // 登录成功
                        Toast.makeText(
                            this@LoginActivity,
                            "登录成功! 欢迎 ${matchedUser.username}",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        // 跳转到主界面
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("username", matchedUser.username)
                        intent.putExtra("email", matchedUser.email)
                        startActivity(intent)
                        finish()
                    } else {
                        // 登录失败
                        Toast.makeText(
                            this@LoginActivity,
                            "邮箱/手机号或密码错误，请检查后重试",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    showLoading(false)
                    Toast.makeText(
                        this@LoginActivity,
                        "服务器错误: ${response.code()}，请稍后重试",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(
                    this@LoginActivity,
                    "网络错误: ${e.message}，请检查网络连接后重试",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    
    private fun testServerConnection() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No network connection available", Toast.LENGTH_LONG).show()
            return
        }
        
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.testConnection()
                showLoading(false)
                
                if (response.isSuccessful) {
                    val result = response.body()
                    Toast.makeText(
                        this@LoginActivity,
                        "✅ Server connection successful!\nServer response: ${result?.get("message") ?: "OK"}",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "❌ Server responded with error: ${response.code()}\n${response.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(
                    this@LoginActivity,
                    "❌ Connection failed: ${e.message}\n\nPlease check:\n1. Server is running\n2. You're on the same network\n3. Firewall settings",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun validateInputs(identifier: String, password: String): Boolean {
        if (identifier.isEmpty()) {
            usernameEditText.error = "Please enter your email or phone number"
            usernameEditText.requestFocus()
            return false
        }
        
        if (password.isEmpty()) {
            passwordEditText.error = "Please enter your password"
            passwordEditText.requestFocus()
            return false
        }
        
        // Check if identifier is either valid email or valid phone
        val isValidEmail = Patterns.EMAIL_ADDRESS.matcher(identifier).matches()
        val isValidPhone = identifier.matches(Regex("^[0-9]{10,15}$"))
        
        if (!isValidEmail && !isValidPhone) {
            usernameEditText.error = "Please enter a valid email address or phone number"
            usernameEditText.requestFocus()
            return false
        }
        
        if (password.length < 6) {
            passwordEditText.error = "Password must be at least 6 characters"
            passwordEditText.requestFocus()
            return false
        }
        
        return true
    }
    
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            loginButton.isEnabled = false
            signUpButton.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            loginButton.isEnabled = true
            signUpButton.isEnabled = true
        }
    }
    
    private fun isPhoneNumber(identifier: String): Boolean {
        // Check if it's a phone number (starts with 0 and contains only digits)
        return identifier.startsWith("0") && identifier.all { it.isDigit() }
    }
    
    private suspend fun findEmailByPhone(phone: String): String? {
        // For now, since we don't have a getUsers endpoint,
        // we'll assume phone numbers can be used directly as identifiers
        // In a real app, you'd need a specific API endpoint for this
        return phone
    }
}
