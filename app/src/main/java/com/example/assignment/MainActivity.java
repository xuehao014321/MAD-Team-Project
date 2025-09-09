package com.example.assignment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize view components
        initViews();
        
        // Setup click listeners
        setupClickListeners();

    }

    private void initViews() {
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegister();
            }
        });
    }

    private void handleLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Input validation
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress bar and disable login button
        showLoading(true);

        // Authenticate user with database
        ApiClient.getUserByUsername(username, new ApiClient.UserCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLoading(false);
                        
                        // Verify password
                        if (user.getPassword().equals(password)) {
                            // Login successful
                            Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            
                            // Calculate credit from API data
                            RentalDataManager.getUserCreditFromAPI(MainActivity.this, user.getUserId(), user.getUsername(), 
                                new RentalDataManager.CreditCallback() {
                                    @Override
                                    public void onSuccess(int credit) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                // Navigate to dashboard page with API-calculated credit
                                                Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                                                intent.putExtra("username", username);
                                                intent.putExtra("user_id", user.getUserId());
                                                intent.putExtra("email", user.getEmail());
                                                intent.putExtra("phone", user.getPhone());
                                                intent.putExtra("gender", user.getGender());
                                                intent.putExtra("distance", user.getDistance());
                                                // Use API-calculated credit points
                                                intent.putExtra("credit", credit);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                                    }
                                    
                                    @Override
                                    public void onError(String error) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                // Fallback to local credit calculation
                                                int localCredit = RentalDataManager.getUserCredit(MainActivity.this, user.getUsername());
                                                Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                                                intent.putExtra("username", username);
                                                intent.putExtra("user_id", user.getUserId());
                                                intent.putExtra("email", user.getEmail());
                                                intent.putExtra("phone", user.getPhone());
                                                intent.putExtra("gender", user.getGender());
                                                intent.putExtra("distance", user.getDistance());
                                                intent.putExtra("credit", localCredit);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                                    }
                                });
                        } else {
                            // Wrong password
                            Toast.makeText(MainActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLoading(false);
                        Toast.makeText(MainActivity.this, "Login failed: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void handleRegister() {
        // Navigate to registration activity or show registration dialog
        Toast.makeText(this, "Registration feature to be implemented", Toast.LENGTH_SHORT).show();
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);
            loginButton.setText("Logging in...");
        } else {
            progressBar.setVisibility(View.GONE);
            loginButton.setEnabled(true);
            loginButton.setText("Login");
        }
    }

}
