package com.example.assignment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;



public class DashboardActivity extends AppCompatActivity {
    
    private static final String TAG = "DashboardActivity";
    
    // UI components
    private TextView mainUserName;
    private TextView mainUserEmail;
    private TextView quickCredit;
    private TextView quickRentals;
    private Button profileButton;
    private ImageView creditInfoIconMain;
    
    // User data
    private String username;
    private String email;
    private String gender;
    private String phone;
    private int userId;
    private int creditPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "DashboardActivity onCreate");
        
        // Get intent data
        getIntentData();
        
        // Initialize views
        initViews();
        
        // Setup user data
        setupUserData();
        
        // Setup click listeners
        setupClickListeners();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        email = intent.getStringExtra("email");
        gender = intent.getStringExtra("gender");
        phone = intent.getStringExtra("phone");
        userId = intent.getIntExtra("user_id", 0);
        creditPoints = intent.getIntExtra("credit", 0);
        
        Log.d(TAG, "Received data - username: " + username + ", email: " + email + ", gender: " + gender);
        Log.d(TAG, "Initial credit from intent: " + creditPoints);
    }

    private void initViews() {
        mainUserName = findViewById(R.id.main_user_name);
        mainUserEmail = findViewById(R.id.main_user_email);
        quickCredit = findViewById(R.id.quick_credit);
        quickRentals = findViewById(R.id.quick_rentals);
        profileButton = findViewById(R.id.profile_button);
        creditInfoIconMain = findViewById(R.id.credit_info_icon_main);
        
        Log.d(TAG, "Views initialized");
    }
    
    private void setupUserData() {
        // Display username and email
        mainUserName.setText(username);
        mainUserEmail.setText(email);
        
        Log.d(TAG, "Setting up user data for: " + username);
        
        // Display credit points
        quickCredit.setText(String.valueOf(creditPoints));
        
        // Display available items count from API
        RentalDataManager.getAvailableItemsCountFromAPI(this, userId, new RentalDataManager.AvailableItemsCallback() {
            @Override
            public void onSuccess(int availableCount) {
                runOnUiThread(() -> {
                    quickRentals.setText(String.valueOf(availableCount));
                    Log.d(TAG, "Available items count updated: " + availableCount);
                });
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to get available items count: " + error);
                // Fallback to old method if API fails
                int activeRentals = RentalDataManager.getItemsBorrowedCount(DashboardActivity.this, username);
                quickRentals.setText(String.valueOf(activeRentals));
            }
        });
        
        Log.d(TAG, "User data setup completed");
    }

    private void setupClickListeners() {
        // Setup "View My Profile" button click event
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Profile button clicked");
                Intent intent = new Intent(DashboardActivity.this, MyProfileActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("email", email);
                intent.putExtra("gender", gender);
                intent.putExtra("phone", phone);
                intent.putExtra("user_id", userId);
                intent.putExtra("credit", creditPoints);
                startActivity(intent);
            }
        });
        
        // Setup credit info icon click event - refresh with API data
        creditInfoIconMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Test API credit calculation
                testApiCreditCalculation();
            }
        });
        

        
        Log.d(TAG, "Click listeners setup completed");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data when returning to activity
        // Update credit points with API data
        refreshCreditFromAPI();
        
        // Update available items count with latest data from API
        RentalDataManager.getAvailableItemsCountFromAPI(this, userId, new RentalDataManager.AvailableItemsCallback() {
            @Override
            public void onSuccess(int availableCount) {
                runOnUiThread(() -> {
                    quickRentals.setText(String.valueOf(availableCount));
                    Log.d(TAG, "Available items count refreshed: " + availableCount);
                });
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to refresh available items count: " + error);
                // Fallback to old method if API fails
                int activeRentals = RentalDataManager.getItemsBorrowedCount(DashboardActivity.this, username);
                quickRentals.setText(String.valueOf(activeRentals));
            }
        });
        
        // Update any dynamic content if needed
    }
    
    /**
     * Refresh credit from API data
     */
    private void refreshCreditFromAPI() {
        Log.d(TAG, "=== REFRESHING CREDIT FROM API ===");
        RentalDataManager.getUserCreditFromAPI(this, userId, username, new RentalDataManager.CreditCallback() {
            @Override
            public void onSuccess(int credit) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        creditPoints = credit;
                        quickCredit.setText(String.valueOf(creditPoints));
                        Log.d(TAG, "✅ Credit refreshed from API: " + credit);
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Failed to refresh credit from API: " + error);
                // Fallback to local calculation
                int localCredit = RentalDataManager.getUserCredit(DashboardActivity.this, username);
                creditPoints = localCredit;
                quickCredit.setText(String.valueOf(creditPoints));
            }
        });
    }
    
    /**
     * Test API credit calculation
     */
    private void testApiCreditCalculation() {
        Log.d(TAG, "🧪 === TESTING API CREDIT CALCULATION === 🧪");
        Toast.makeText(this, "Testing API credit calculation...", Toast.LENGTH_SHORT).show();
        
        RentalDataManager.getUserCreditFromAPI(this, userId, username, new RentalDataManager.CreditCallback() {
            @Override
            public void onSuccess(int credit) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        creditPoints = credit;
                        quickCredit.setText(String.valueOf(creditPoints));
                        
                        // Show detailed info
                        Toast.makeText(DashboardActivity.this, 
                            "🎯 API credit calculation completed!\n" +
                            "👤 User: " + username + "\n" +
                            "🎉 Latest credit: " + credit + " points\n" +
                            "📦 Based on API item data calculation", 
                            Toast.LENGTH_LONG).show();
                        
                        Log.d(TAG, "✅ API credit test completed: " + credit);
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DashboardActivity.this, 
                            "❌ API credit calculation failed: " + error, 
                            Toast.LENGTH_LONG).show();
                        
                        Log.e(TAG, "❌ API credit test failed: " + error);
                    }
                });
            }
        });
    }
    

}
