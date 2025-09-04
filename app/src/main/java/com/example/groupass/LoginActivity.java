package com.example.groupass;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button signUpButton;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);
        
        // Initialize UserManager
        userManager = new UserManager(this);
        
        // Force reload CSV data to ensure correct password format
        userManager.forceReloadCSV();

        // Set click listeners
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Sign Up page
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void handleLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Basic validation
        if (username.isEmpty()) {
            usernameEditText.setError("Username is required");
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            return;
        }

        
        // Authenticate user
        if (userManager.authenticateUser(username, password)) {
            // Login successful
            Toast.makeText(this, "Login successful! Welcome " + username, Toast.LENGTH_SHORT).show();
            
            // Navigate to main app
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
        } else {
            // Login failed
            if (userManager.userExists(username)) {
                passwordEditText.setError("Incorrect password");
                Toast.makeText(this, "Incorrect password. Please try again.", Toast.LENGTH_SHORT).show();
            } else {
                usernameEditText.setError("Username not found");
                Toast.makeText(this, "Username not found. Please sign up first.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
