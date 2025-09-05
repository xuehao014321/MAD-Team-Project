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
    private String currentUser = ""; // 添加当前用户变量
    private CSVFileManager csvManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        csvManager = new CSVFileManager(this);
        
        // 获取用户名（现在不再从LoginActivity传递）
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("username")) {
            currentUser = intent.getStringExtra("username");
            Log.d(TAG, "Received username: '" + currentUser + "'");
        } else {
            // 设置默认用户或让用户选择
            currentUser = "DefaultUser"; // 或者可以从CSV文件中选择第一个用户
            Log.d(TAG, "Using default username: '" + currentUser + "'");
        }
        
        // 设置ScrollView的边距
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
        
        // 添加空值检查
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
            
            // Get Gmail from CSV data
            Log.d(TAG, "Calling getUserGmail for: '" + currentUser + "'");
            String userGmail = csvManager.getUserGmail(currentUser);
            Log.d(TAG, "Retrieved Gmail: '" + userGmail + "'");
            
            if (mainUserEmail != null) {
                mainUserEmail.setText(userGmail);
                Log.d(TAG, "Set email text to: " + userGmail);
            } else {
                Log.e(TAG, "mainUserEmail is null, cannot set text");
            }
        } else {
            Log.w(TAG, "No current user set or user is empty");
        }
    }

    private void setupClickListeners() {
        Button profileButton = findViewById(R.id.profile_button);
        if (profileButton != null) {
            profileButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, MyProfileActivity.class);
                    intent.putExtra("username", currentUser); // 传递当前登录的用户名
                    startActivity(intent);
                }
            });
        } else {
            Log.e(TAG, "profileButton not found!");
        }

        // 信用信息图标
        if (creditInfoIconMain != null) {
            creditInfoIconMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "4 stars or above can borrow books from our platform", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Log.e(TAG, "creditInfoIconMain not found!");
        }
    }
}
