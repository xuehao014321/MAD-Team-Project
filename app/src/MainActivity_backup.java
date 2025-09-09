package com.example.assignment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ImageView creditInfoIconMain;
    private TextView mainUserName;
    private TextView mainUserEmail;
    private String currentUser = ""; // Add current user variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Get username (no longer passed from LoginActivity)
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("username")) {
            currentUser = intent.getStringExtra("username");
            Log.d(TAG, "Received username: '" + currentUser + "'");
        } else {
            // Set default user or let user choose
            currentUser = "Username"; // Default username
            Log.d(TAG, "Using default username: '" + currentUser + "'");
        }
        
        // Set ScrollView padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupUserData();
        setupClickListeners();
    }

    private void initializeViews() {
        creditInfoIconMain = findViewById(R.id.credit_info_icon_main);
        mainUserName = findViewById(R.id.main_user_name);
        mainUserEmail = findViewById(R.id.main_user_email);
        
        // Add null value check
        if (mainUserName == null) {
            Log.e(TAG, "mainUserName TextView not found!");
        } else {
            Log.d(TAG, "mainUserName TextView found successfully");
        }
        if (mainUserEmail == null) {
            Log.e(TAG, "mainUserEmail TextView not found!");
        } else {
            Log.d(TAG, "mainUserEmail TextView found successfully");
        }
    }


    private void setupUserData() {
        if (currentUser != null && !currentUser.isEmpty()) {
            Log.d(TAG, "Setting up data for user: '" + currentUser + "'");
            
            if (mainUserName != null) {
                mainUserName.setText(currentUser);
                Log.d(TAG, "Set username text to: " + currentUser);
            }
            
            // Set default email for now - will be replaced with database values
            if (mainUserEmail != null) {
                mainUserEmail.setText("user@example.com");
            }
        }
    }
} 
