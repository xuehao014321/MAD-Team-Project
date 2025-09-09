package com.example.mad_gruop_ass

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.mad_gruop_ass.R

class MainActivity : ComponentActivity() {
    
    private lateinit var welcomeTextView: TextView
    private lateinit var logoutButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        setupClickListeners()
        
        // 获取传递过来的用户信息
        val username = intent.getStringExtra("username") ?: "User"
        welcomeTextView.text = "Welcome, $username!"
    }
    
    private fun initViews() {
        welcomeTextView = findViewById(R.id.welcomeTextView)
        logoutButton = findViewById(R.id.logoutButton)
    }
    
    private fun setupClickListeners() {
        logoutButton.setOnClickListener {
            logout()
        }
    }
    
    private fun logout() {
        // 跳转回登录界面
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
