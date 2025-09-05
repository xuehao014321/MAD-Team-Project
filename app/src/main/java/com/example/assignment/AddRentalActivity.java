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

public class AddRentalActivity extends AppCompatActivity {

    private EditText itemNameEditText;
    private EditText descriptionEditText;
    private RadioGroup typeRadioGroup;
    private RadioGroup statusRadioGroup;
    private Button addButton;
    private Button backButton;

    private String currentUser;
    private CSVFileManager csvManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rental);

        // Initialize CSV manager
        csvManager = new CSVFileManager(this);

        // Get current user from intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("username")) {
            currentUser = intent.getStringExtra("username");
        } else {
            currentUser = "Unknown";
        }

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        itemNameEditText = findViewById(R.id.item_name_edit);
        descriptionEditText = findViewById(R.id.description_edit);
        typeRadioGroup = findViewById(R.id.type_radio_group);
        statusRadioGroup = findViewById(R.id.status_radio_group);
        addButton = findViewById(R.id.add_button);
        backButton = findViewById(R.id.back_button);
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
    }

    private void addRentalRecord() {
        String itemName = itemNameEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        if (itemName.isEmpty()) {
            Toast.makeText(this, "Please enter item name", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedTypeId = typeRadioGroup.getCheckedRadioButtonId();
        String type = "";
        if (selectedTypeId == R.id.type_lend) {
            type = "Lend";
        } else if (selectedTypeId == R.id.type_request) {
            type = "Borrow";
        } else {
            Toast.makeText(this, "Please select type", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedStatusId = statusRadioGroup.getCheckedRadioButtonId();
        String status = "";
        if (selectedStatusId == R.id.status_ongoing) {
            status = "On going";
        } else if (selectedStatusId == R.id.status_completed) {
            status = "Completed";
        } else {
            Toast.makeText(this, "Please select status", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Create rental record
        RentalRecord record = new RentalRecord();
        record.setUsername(currentUser);
        record.setItemName(itemName);
        record.setType(type);
        record.setStatus(status);
        record.setDescription(description);
        record.setDistance("100m"); // Default value
        record.setCredit(100); // Default value
        record.setLike(0); // Default value
        record.setFavor(0); // Default value
        record.setGmail(csvManager.getUserGmail(currentUser)); // Set Gmail from CSVFileManager
        record.setGender(csvManager.getUserGender(currentUser)); // Set Gender from CSVFileManager

        // Save to CSV file
        boolean success = csvManager.addRecord(record);

        if (success) {
            Toast.makeText(this, "Rental record added successfully to CSV", Toast.LENGTH_SHORT).show();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("record_added", true);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Failed to add record to CSV", Toast.LENGTH_SHORT).show();
        }
    }
}
