package com.example.assignment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MyProfileActivity extends AppCompatActivity {

    private static final String TAG = "MyProfileActivity";
    private TextView userName;
    private TextView userEmail;
    private TextView userGender;
    private RatingBar creditRating;
    private TextView creditPoints;
    private TextView itemsLentCount;
    private TextView itemsBorrowedCount;
    private TextView reviewsCount;
    private Button logoutButton;
    private Button addRentalButton;
    private LinearLayout rentalHistoryContainer;
    private TextView backButton;
    private ImageView creditInfoIcon;

    private String currentUser = ""; // 改为空字符串，确保必须从Intent获取
    private CSVFileManager csvManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        csvManager = new CSVFileManager(this);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("username")) {
            currentUser = intent.getStringExtra("username");
            Log.d(TAG, "Received username: " + currentUser);
        } else {
            // 如果没有传递用户名，显示错误并返回
            Toast.makeText(this, "用户信息错误，请重新登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupUserData();
        setupClickListeners();
        loadRentalHistory();
    }

    private void initializeViews() {
        userName = findViewById(R.id.user_name);
        userEmail = findViewById(R.id.user_email);
        userGender = findViewById(R.id.user_gender);
        creditRating = findViewById(R.id.credit_rating);
        creditPoints = findViewById(R.id.credit_points);
        itemsLentCount = findViewById(R.id.items_lent_count);
        itemsBorrowedCount = findViewById(R.id.items_borrowed_count);
        reviewsCount = findViewById(R.id.reviews_count);
        logoutButton = findViewById(R.id.logout_button);
        addRentalButton = findViewById(R.id.add_rental_button);
        rentalHistoryContainer = findViewById(R.id.rental_history_container);
        backButton = findViewById(R.id.back_button);
        creditInfoIcon = findViewById(R.id.credit_info_icon);
    }

    private void setupUserData() {
        Log.d(TAG, "Setting up data for user: " + currentUser);
        userName.setText(currentUser);

        // Get Gmail from CSV data
        String userGmail = csvManager.getUserGmail(currentUser);
        userEmail.setText(userGmail);
        Log.d(TAG, "User Gmail: " + userGmail);

        // Get Gender from CSV data
        String userGenderValue = csvManager.getUserGender(currentUser);
        Log.d(TAG, "Retrieved Gender: " + userGenderValue);
        
        if (userGender != null) {
            userGender.setText(userGenderValue);
            Log.d(TAG, "Gender TextView set to: " + userGenderValue);
        } else {
            Log.e(TAG, "Gender TextView is null!");
        }

        int userCredit = csvManager.getUserCredit(currentUser);
        creditPoints.setText(userCredit + " points");
        Log.d(TAG, "User Credit: " + userCredit);

        float rating = Math.min(5.0f, userCredit / 20.0f);
        creditRating.setRating(rating);

        int lentCount = csvManager.getItemsLentCount(currentUser);
        int borrowedCount = csvManager.getItemsBorrowedCount(currentUser);

        itemsLentCount.setText(String.valueOf(lentCount));
        itemsBorrowedCount.setText(String.valueOf(borrowedCount));
        reviewsCount.setText("5");
        
        Log.d(TAG, "Profile data setup completed for: " + currentUser);
        Log.d(TAG, "Final display - Name: " + currentUser + ", Email: " + userGmail + ", Gender: " + userGenderValue + ", Credit: " + userCredit);
    }

    private void loadRentalHistory() {
        rentalHistoryContainer.removeAllViews();

        List<RentalRecord> records = csvManager.getRecordsForUser(currentUser);

        if (records.isEmpty()) {
            TextView noRecordsText = new TextView(this);
            noRecordsText.setText("No rental records");
            noRecordsText.setTextSize(14);
            noRecordsText.setTextColor(getResources().getColor(android.R.color.darker_gray));
            noRecordsText.setPadding(0, 16, 0, 16);
            rentalHistoryContainer.addView(noRecordsText);
        } else {
            for (RentalRecord record : records) {
                TextView recordText = new TextView(this);
                recordText.setText(record.getDisplayText());
                recordText.setTextSize(14);
                recordText.setTextColor(getResources().getColor(android.R.color.darker_gray));
                recordText.setPadding(0, 8, 0, 8);
                rentalHistoryContainer.addView(recordText);
            }
        }
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MyProfileActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MyProfileActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        addRentalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyProfileActivity.this, AddRentalActivity.class);
                intent.putExtra("username", currentUser);
                startActivityForResult(intent, 1);
            }
        });

        creditInfoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MyProfileActivity.this, "4 stars or above can borrow books from our platform", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadRentalHistory();
            setupUserData();
        }
    }
}
