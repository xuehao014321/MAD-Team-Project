package com.example.assignment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.List;

public class MyProfileActivity extends AppCompatActivity {

    private static final String TAG = "MyProfileActivity";
    private TextView userName;
    private TextView userEmail;
    private TextView userGender;
    private RatingBar creditRating;
    private TextView creditPoints;
    private Button logoutButton;
    private TextView backButton;
    private ImageView creditInfoIcon;
    private LinearLayout itemsContainer;

    private String currentUser = "";
    private String currentEmail = "";
    private String currentGender = "";
    private String currentPhone = "";
    private int currentUserId = 0;
    private int currentCredit = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("username")) {
            currentUser = intent.getStringExtra("username");
            currentEmail = intent.getStringExtra("email");
            currentGender = intent.getStringExtra("gender");
            currentPhone = intent.getStringExtra("phone");
            currentUserId = intent.getIntExtra("user_id", 0);
            currentCredit = intent.getIntExtra("credit", 0);
            
            Log.d(TAG, "Received data - username: " + currentUser + ", email: " + currentEmail + ", gender: " + currentGender + ", user_id: " + currentUserId);
        } else {
            Toast.makeText(this, "Error, Please login again", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupUserData();
        setupClickListeners();
        loadUserItems();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh credit when returning to activity (same as Dashboard)
        refreshCreditFromAPI();
    }
    
    /**
     * Refresh credit from API (same logic as Dashboard)
     */
    private void refreshCreditFromAPI() {
        Log.d(TAG, "=== REFRESHING CREDIT FROM API IN PROFILE ===");
        
        if (currentUserId > 0 && currentUser != null && !currentUser.isEmpty()) {
            RentalDataManager.getUserCreditFromAPI(this, currentUserId, currentUser, new RentalDataManager.CreditCallback() {
                @Override
                public void onSuccess(int credit) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            creditPoints.setText(credit + " points");
                            
                            // Update credit rating
                            float rating = Math.min(5.0f, credit / 20.0f);
                            creditRating.setRating(rating);
                            
                            Log.d(TAG, "✅ Credit refreshed from API in profile: " + credit);
                        }
                    });
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Failed to refresh credit from API in profile: " + error);
                    // Fallback to local calculation
                    int localCredit = RentalDataManager.getUserCredit(MyProfileActivity.this, currentUser);
                    creditPoints.setText(localCredit + " points");
                    
                    // Update credit rating
                    float rating = Math.min(5.0f, localCredit / 20.0f);
                    creditRating.setRating(rating);
                }
            });
        }
    }

    private void initializeViews() {
        userName = findViewById(R.id.user_name);
        userEmail = findViewById(R.id.user_email);
        userGender = findViewById(R.id.user_gender);
        creditRating = findViewById(R.id.credit_rating);
        creditPoints = findViewById(R.id.credit_points);
        logoutButton = findViewById(R.id.logout_button);
        backButton = findViewById(R.id.back_button);
        creditInfoIcon = findViewById(R.id.credit_info_icon);
        itemsContainer = findViewById(R.id.items_container);
    }

    private void setupUserData() {
        // Log current user information for debugging
        Log.d(TAG, "Current user from preference: " + currentUser);
        
        // Try to get user data passed from intent (as User object)
        User user = (User) getIntent().getSerializableExtra("user_data");
        
        // If no User object, try to get individual fields from intent
        if (user == null) {
            String username = getIntent().getStringExtra("username");
            String email = getIntent().getStringExtra("email");
            String gender = getIntent().getStringExtra("gender");
            String phone = getIntent().getStringExtra("phone");
            int userId = getIntent().getIntExtra("user_id", -1);
            int credit = getIntent().getIntExtra("credit", 50);
            
            if (username != null && !username.isEmpty()) {
                // Create a user object from the individual fields
                user = new User();
                user.setUsername(username);
                user.setEmail(email);
                user.setGender(gender);
                user.setPhone(phone);
                user.setUserId(userId);
                user.setCredit(credit);
                
                Log.d(TAG, "User data created from intent fields: " + username + " (ID: " + userId + ")");
            } else if (currentUser != null && !currentUser.isEmpty()) {
                // Fallback to current user from preferences
                user = new User();
                user.setUsername(currentUser);
                user.setEmail(currentEmail != null ? currentEmail : "No email set");
                user.setGender(currentGender != null ? currentGender : "No gender set");
                user.setUserId(currentUserId); // Use currentUserId from intent
                user.setCredit(currentCredit);
                
                Log.d(TAG, "User data created from preferences: " + currentUser + " (ID: " + currentUserId + ")");
            }
        } else {
            Log.d(TAG, "User data received from intent: " + user.getUsername());
        }
        
        // Create final reference for inner class access
        final User finalUser = user;
        
        if (finalUser != null) {
            // Set user information
            userName.setText(finalUser.getUsername());
            userEmail.setText(finalUser.getEmail() != null ? finalUser.getEmail() : "No email set");
            userGender.setText(finalUser.getGender() != null ? finalUser.getGender() : "No gender set");
            
            // Get rental history for this user
            List<RentalRecord> userHistory = RentalDataManager.getRentalHistoryForUser(this, finalUser.getUsername());
            Log.d(TAG, "Total rental records for user " + finalUser.getUsername() + ": " + userHistory.size());
            
            // Test: Query item counts for all users
            RentalDataManager.testUserItemCounts(this);
            
            // Log each record
            for (RentalRecord record : userHistory) {
                Log.d(TAG, "Record - Type: " + record.getType() + ", Item: " + record.getItemName() + ", User: " + record.getUsername());
            }
            
            // Use API-based credit calculation (same as Dashboard)
            RentalDataManager.getUserCreditFromAPI(MyProfileActivity.this, finalUser.getUserId(), finalUser.getUsername(), new RentalDataManager.CreditCallback() {
                @Override
                public void onSuccess(int apiCredit) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "✅ API credit calculated: " + apiCredit + " for user: " + finalUser.getUsername());
                            creditPoints.setText(apiCredit + " points");
                            
                            // Set credit rating
                            float rating = Math.min(5.0f, apiCredit / 20.0f);
                            creditRating.setRating(rating);
                        }
                    });
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Failed to get API credit, using local calculation: " + error);
                    // Fallback to local calculation
                    int localCredit = RentalDataManager.getUserCredit(MyProfileActivity.this, finalUser.getUsername());
                    Log.d(TAG, "Local credit calculated: " + localCredit + " for user: " + finalUser.getUsername());
                    creditPoints.setText(localCredit + " points");
                    
                    // Set credit rating
                    float rating = Math.min(5.0f, localCredit / 20.0f);
                    creditRating.setRating(rating);
                }
            });
            
            Log.d(TAG, "User data loaded successfully");
        } else {
            Log.e(TAG, "No user data available from any source");
            Toast.makeText(this, "No user data available", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadUserItems() {
        Log.d(TAG, "Loading items for user ID: " + currentUserId);
        
        ApiClient.getItemsByUserId(currentUserId, new ApiClient.ItemsListCallback() {
            @Override
            public void onSuccess(List<Item> items) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Successfully loaded " + items.size() + " items for user ID " + currentUserId);
                        for (Item item : items) {
                            Log.d(TAG, "Item: ID=" + item.getItemId() + ", UserID=" + item.getUserId() + ", Title=" + item.getTitle());
                        }
                        displayUserItems(items);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "Failed to load user items: " + error);
                        // Show static placeholder items on error
                        displayPlaceholderItems();
                    }
                });
            }
        });
    }

    private void displayUserItems(List<Item> items) {
        // Clear existing items from the items container
        itemsContainer.removeAllViews();
        
        // Create rows of items (2 items per row)
        LinearLayout currentRow = null;
        for (int i = 0; i < items.size(); i++) {
            if (i % 2 == 0) {
                // Create new row
                currentRow = new LinearLayout(this);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                rowParams.setMargins(0, 0, 0, dpToPx(16));
                currentRow.setLayoutParams(rowParams);
                
                // Add to items container
                itemsContainer.addView(currentRow);
            }
            
            Item item = items.get(i);
            CardView itemCard = createItemCard(item, i % 2 == 0);
            currentRow.addView(itemCard);
        }
        
        // If we have an odd number of items, add a placeholder card
        if (items.size() % 2 == 1) {
            CardView placeholderCard = createPlaceholderCard(false);
            currentRow.addView(placeholderCard);
        }
        
        // If no items, show a message
        if (items.size() == 0) {
            showNoItemsMessage();
        }
    }

    private void displayPlaceholderItems() {
        // Create some example items for demonstration
        Log.d(TAG, "Displaying placeholder items due to API error");
        // The static items in the layout will be shown by default
    }

    private CardView createItemCard(Item item, boolean isLeftCard) {
        CardView cardView = new CardView(this);
        
        // Card layout parameters
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            0, dpToPx(200)
        );
        cardParams.weight = 1;
        
        if (isLeftCard) {
            cardParams.setMargins(0, 0, dpToPx(8), 0);
        } else {
            cardParams.setMargins(dpToPx(8), 0, 0, 0);
        }
        
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(dpToPx(12));
        cardView.setCardElevation(dpToPx(4));
        
        // Create inner layout
        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        
        // Item image placeholder
        ImageView itemImage = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0
        );
        imageParams.weight = 1;
        imageParams.setMargins(0, 0, 0, dpToPx(8));
        itemImage.setLayoutParams(imageParams);
        
        // Try to load image or show placeholder
        String imageUrl = item.getImageUrl();
        Log.d(TAG, "Item " + item.getItemId() + " image URL: " + imageUrl);
        
        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals("null")) {
            // For now, show a different color to indicate we have an image URL
            itemImage.setBackgroundColor(0xFF81C784); // Light green to indicate image available
            itemImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            
            // Add a text overlay to show we found an image URL
            TextView imageIndicator = new TextView(this);
            imageIndicator.setText("📷 Image");
            imageIndicator.setTextColor(0xFFFFFFFF);
            imageIndicator.setTextSize(12);
            imageIndicator.setGravity(android.view.Gravity.CENTER);
            imageIndicator.setBackgroundColor(0x80000000); // Semi-transparent black
            
            // Create a frame layout to overlay the text
            FrameLayout imageFrame = new FrameLayout(this);
            imageFrame.setLayoutParams(imageParams);
            imageFrame.addView(itemImage);
            
            FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            );
            textParams.gravity = android.view.Gravity.CENTER;
            imageIndicator.setLayoutParams(textParams);
            imageFrame.addView(imageIndicator);
            
            innerLayout.addView(imageFrame);
        } else {
            // No image URL available
            itemImage.setBackgroundColor(0xFFE0E0E0); // Gray placeholder
            itemImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            innerLayout.addView(itemImage);
        }
        
        // Item title
        TextView itemTitle = new TextView(this);
        itemTitle.setText(item.getTitle());
        itemTitle.setTextSize(12);
        itemTitle.setMaxLines(2);
        itemTitle.setTextColor(0xFF333333);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, 0, 0, dpToPx(4));
        itemTitle.setLayoutParams(titleParams);
        
        // Item price
        TextView itemPrice = new TextView(this);
        itemPrice.setText("RM" + String.format("%.0f", item.getPrice()));
        itemPrice.setTextSize(16);
        itemPrice.setTextColor(0xFF333333);
        itemPrice.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        priceParams.setMargins(0, 0, 0, dpToPx(8));
        itemPrice.setLayoutParams(priceParams);
        
        // Bottom info layout
        LinearLayout bottomLayout = new LinearLayout(this);
        bottomLayout.setOrientation(LinearLayout.HORIZONTAL);
        bottomLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        
        // User info section
        LinearLayout userInfoLayout = new LinearLayout(this);
        userInfoLayout.setOrientation(LinearLayout.HORIZONTAL);
        userInfoLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams userInfoParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        userInfoParams.weight = 1;
        userInfoLayout.setLayoutParams(userInfoParams);
        
        ImageView userIcon = new ImageView(this);
        LinearLayout.LayoutParams userIconParams = new LinearLayout.LayoutParams(
            dpToPx(16), dpToPx(16)
        );
        userIconParams.setMargins(0, 0, dpToPx(4), 0);
        userIcon.setLayoutParams(userIconParams);
        userIcon.setBackgroundColor(0xFFCCCCCC);
        
        TextView userNameText = new TextView(this);
        userNameText.setText(currentUser);
        userNameText.setTextSize(10);
        userNameText.setTextColor(0xFF666666);
        
        userInfoLayout.addView(userIcon);
        userInfoLayout.addView(userNameText);
        
        // Views/Likes section
        LinearLayout statsLayout = new LinearLayout(this);
        statsLayout.setOrientation(LinearLayout.HORIZONTAL);
        statsLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        
        ImageView viewsIcon = new ImageView(this);
        LinearLayout.LayoutParams viewsIconParams = new LinearLayout.LayoutParams(
            dpToPx(12), dpToPx(12)
        );
        viewsIconParams.setMargins(0, 0, dpToPx(2), 0);
        viewsIcon.setLayoutParams(viewsIconParams);
        viewsIcon.setBackgroundColor(0xFFFF4444);
        
        TextView viewsText = new TextView(this);
        viewsText.setText(String.valueOf(item.getViews()));
        viewsText.setTextSize(10);
        viewsText.setTextColor(0xFF666666);
        
        statsLayout.addView(viewsIcon);
        statsLayout.addView(viewsText);
        
        bottomLayout.addView(userInfoLayout);
        bottomLayout.addView(statsLayout);
        
        // Add all views to inner layout (image already added above)
        innerLayout.addView(itemTitle);
        innerLayout.addView(itemPrice);
        innerLayout.addView(bottomLayout);
        
        cardView.addView(innerLayout);
        
        return cardView;
    }

    private CardView createPlaceholderCard(boolean isLeftCard) {
        CardView cardView = new CardView(this);
        
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            0, dpToPx(200)
        );
        cardParams.weight = 1;
        
        if (isLeftCard) {
            cardParams.setMargins(0, 0, dpToPx(8), 0);
        } else {
            cardParams.setMargins(dpToPx(8), 0, 0, 0);
        }
        
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(dpToPx(12));
        cardView.setCardElevation(dpToPx(4));
        
        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        innerLayout.setGravity(android.view.Gravity.CENTER);
        
        TextView placeholderText = new TextView(this);
        placeholderText.setText("More items coming soon...");
        placeholderText.setTextSize(14);
        placeholderText.setTextColor(0xFF999999);
        
        innerLayout.addView(placeholderText);
        cardView.addView(innerLayout);
        
        return cardView;
    }

    private void showNoItemsMessage() {
        TextView noItemsText = new TextView(this);
        noItemsText.setText("No items found. Start sharing your items with neighbors!");
        noItemsText.setTextSize(16);
        noItemsText.setTextColor(0xFF666666);
        noItemsText.setGravity(android.view.Gravity.CENTER);
        
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.setMargins(0, dpToPx(20), 0, dpToPx(20));
        noItemsText.setLayoutParams(textParams);
        
        // Add to items container
        itemsContainer.addView(noItemsText);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void setupClickListeners() {
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        creditInfoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreditInfoDialog();
            }
        });
    }

    private void showCreditInfoDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Credit Information")
                .setMessage("Credit points are earned by lending items and being a good neighbor. Higher credit gives you better rental options!")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MyProfileActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
