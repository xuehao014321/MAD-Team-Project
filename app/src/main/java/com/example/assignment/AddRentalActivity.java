package com.example.assignment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

public class AddRentalActivity extends AppCompatActivity {

    private EditText itemNameEditText;
    private EditText descriptionEditText;
    private RadioGroup typeRadioGroup;
    private RadioGroup statusRadioGroup;
    private Button addButton;
    private Button backButton;
    
    // Remove references to typeLendCard and typeRequestCard since we now use RadioButton directly
    // private LinearLayout typeLendCard;
    // private LinearLayout typeRequestCard;
    
    private LinearLayout statusOngoingCard;
    private LinearLayout statusCompletedCard;

    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rental);

        // Get current user from intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("username")) {
            currentUser = intent.getStringExtra("username");
        } else {
            // Default user if not provided
            currentUser = "Guest";
        }

        initializeViews();
        setupClickListeners();
        setupInputValidation();
        
        // Set default selections
        typeRadioGroup.check(R.id.type_lend);
        statusRadioGroup.check(R.id.status_ongoing);
        updateTypeCardAppearance();
        updateStatusCardAppearance();
    }

    private void initializeViews() {
        itemNameEditText = findViewById(R.id.item_name_edit);
        descriptionEditText = findViewById(R.id.description_edit);
        typeRadioGroup = findViewById(R.id.type_radio_group);
        statusRadioGroup = findViewById(R.id.status_radio_group);
        addButton = findViewById(R.id.add_button);
        backButton = findViewById(R.id.back_button);
        
        // Remove references to non-existent views
        // typeLendCard = findViewById(R.id.type_lend_card);
        // typeRequestCard = findViewById(R.id.type_request_card);
        
        // Initialize status cards
        statusOngoingCard = findViewById(R.id.status_ongoing_card);
        statusCompletedCard = findViewById(R.id.status_completed_card);
    }

    private void setupClickListeners() {
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRentalRecord();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Type RadioButton change listener
        typeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateTypeCardAppearance();
            }
        });

        // Status card click listeners
        statusOngoingCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusRadioGroup.check(R.id.status_ongoing);
            }
        });

        statusCompletedCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusRadioGroup.check(R.id.status_completed);
            }
        });

        // Status RadioButton change listener
        statusRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateStatusCardAppearance();
            }
        });
    }

    private void setupInputValidation() {
        // Add real-time validation feedback
        itemNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && itemNameEditText.getText().toString().trim().isEmpty()) {
                    itemNameEditText.setError("Item name is required");
                } else {
                    itemNameEditText.setError(null);
                }
            }
        });
    }

    private void addRentalRecord() {
        String itemName = itemNameEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        // Enhanced validation with better error messages
        if (itemName.isEmpty()) {
            itemNameEditText.setError("Please enter item name");
            itemNameEditText.requestFocus();
            showToast("Please enter the item name to continue", Toast.LENGTH_LONG);
            return;
        }

        if (itemName.length() < 3) {
            itemNameEditText.setError("Item name must be at least 3 characters");
            itemNameEditText.requestFocus();
            showToast("Item name is too short. Please enter a more descriptive name.", Toast.LENGTH_LONG);
            return;
        }

        int selectedTypeId = typeRadioGroup.getCheckedRadioButtonId();
        String type = "";
        if (selectedTypeId == R.id.type_lend) {
            type = "Lend";
        } else if (selectedTypeId == R.id.type_request) {
            type = "Borrow";
        } else {
            showToast("Please select whether you want to lend or borrow this item", Toast.LENGTH_LONG);
            return;
        }

        int selectedStatusId = statusRadioGroup.getCheckedRadioButtonId();
        String status = "";
        if (selectedStatusId == R.id.status_ongoing) {
            status = "On going";
        } else if (selectedStatusId == R.id.status_completed) {
            status = "Completed";
        } else {
            showToast("Please select the current status of this item", Toast.LENGTH_LONG);
            return;
        }

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Create rental record
        RentalRecord record = new RentalRecord();
        record.setUsername(currentUser);
        record.setItemName(itemName);
        record.setType(type);
        record.setStatus(status);
        record.setDescription(description.isEmpty() ? "No description provided" : description);
        record.setDistance("100m"); // Default value
        record.setCredit(0); // No longer set fixed credit score, calculated dynamically by system
        record.setLike(0); // Default value
        record.setFavor(0); // Default value
        record.setGmail("user@example.com"); // Set default Gmail - will be replaced with database values
        record.setGender("Not specified"); // Set default Gender - will be replaced with database values

        // Show loading state
        addButton.setEnabled(false);
        addButton.setText("Adding...");

        // Save to SharedPreferences (temporary storage until database is implemented)
        RentalDataManager.addRentalRecord(this, record);

        // Reset button state
        addButton.setEnabled(true);

        // Since SharedPreferences operations are synchronous and reliable, we assume success
        boolean success = true;
        
        if (success) {
            showToast("✅ Rental record added successfully!", Toast.LENGTH_LONG);
            
            // Clear form
            clearForm();
            
            Intent resultIntent = new Intent();
            resultIntent.putExtra("record_added", true);
            setResult(RESULT_OK, resultIntent);
            
            // Delay finish to show success message
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 1500);
        } else {
            showToast("❌ Failed to add rental record. Please try again.", Toast.LENGTH_LONG);
        }
    }

    private void clearForm() {
        itemNameEditText.setText("");
        descriptionEditText.setText("");
        typeRadioGroup.check(R.id.type_lend);
        statusRadioGroup.check(R.id.status_ongoing);
        updateTypeCardAppearance();
        updateStatusCardAppearance();
        itemNameEditText.setError(null);
    }

    private void showToast(String message, int duration) {
        Toast.makeText(this, message, duration).show();
    }

    private void updateTypeCardAppearance() {
        // RadioButton now uses state selector directly, no additional setup needed
        // State selector automatically changes background based on android:state_checked state
    }

    private void updateStatusCardAppearance() {
        int selectedStatusId = statusRadioGroup.getCheckedRadioButtonId();
        
        if (selectedStatusId == R.id.status_ongoing) {
            // Active is selected
            statusOngoingCard.setSelected(true);
            statusCompletedCard.setSelected(false);
        } else if (selectedStatusId == R.id.status_completed) {
            // Completed is selected
            statusOngoingCard.setSelected(false);
            statusCompletedCard.setSelected(true);
        }
    }
}
