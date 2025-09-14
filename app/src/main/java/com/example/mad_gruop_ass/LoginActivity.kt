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
import com.example.mad_gruop_ass.utils.NetworkUtils
import com.example.mad_gruop_ass.utils.UserSessionManager

class LoginActivity : ComponentActivity() {
    
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var userSessionManager: UserSessionManager
    
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
        progressBar = findViewById(R.id.progressBar)
        userSessionManager = UserSessionManager(this)
    }
    
    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            performLogin()
        }
        
        signUpButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
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
        
        // Authenticate user with API using email or phone
        ApiClient.getUserByEmailOrPhone(identifier, object : ApiClient.UserCallback {
            override fun onSuccess(user: User) {
                runOnUiThread {
                    showLoading(false)

                    // Verify password
                    if (user.password == password) {
                        // Login successful
                        Toast.makeText(this@LoginActivity, "Login successful! Welcome ${user.username}", Toast.LENGTH_SHORT).show()

                        // Save user info to session
                        userSessionManager.saveUserInfo(
                            userId = user.userId,
                            username = user.username,
                            email = user.email,
                            avatarUrl = user.avatarUrl,
                            phone = user.phone,
                            gender = user.gender
                        )

                        // Reset all items is_liked field to 0 after login success
                        ApiClient.resetAllItemsIsLiked(object : ApiClient.ResetLikesCallback {
                            override fun onSuccess(message: String) {
                                Log.d("LoginActivity", "重置is_liked成功: $message")
                                proceedWithCreditCalculation(user)
                            }
                            
                            override fun onError(error: String) {
                                Log.e("LoginActivity", "重置is_liked失败: $error")
                                // 即使重置失败，也继续正常的登录流程
                                proceedWithCreditCalculation(user)
                            }
                        })
                    } else {
                        // Wrong password
                        Toast.makeText(this@LoginActivity, "Invalid password", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    showLoading(false)
                    Toast.makeText(this@LoginActivity, "Login failed: $error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    
    private fun proceedWithCreditCalculation(user: User) {
        // Calculate credit using simplified formula: 50 + (records × 10)
        CreditManager.calculateUserCredit(this@LoginActivity, user.username,
            object : CreditManager.CreditCalculationCallback {
                override fun onSuccess(credit: Int) {
                    runOnUiThread {
                        navigateToDashboard(user, credit)
                    }
                }

                override fun onError(error: String) {
                    runOnUiThread {
                        // Use base credit score on error
                        navigateToDashboard(user, 50)
                    }
                }
            })
    }
    
    private fun navigateToDashboard(user: User, credit: Int) {
        val intent = Intent(this@LoginActivity, UserDashboardActivity::class.java).apply {
            putExtra("username", user.username)
            putExtra("user_id", user.userId)
            putExtra("email", user.email)
            putExtra("phone", user.phone)
            putExtra("gender", user.gender)
            putExtra("distance", user.getDistance())
            putExtra("credit", credit)
        }
        startActivity(intent)
        finish()
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
    
}

