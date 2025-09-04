package com.example.groupass;

import android.content.Context;
import android.content.SharedPreferences;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class UserManager {
    private static final String PREF_NAME = "NeighborLinkUsers";
    private static final String CSV_LOADED_KEY = "csv_data_loaded";
    private SharedPreferences sharedPreferences;
    private Context context;
    
    public UserManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        // Load CSV data if not already loaded
        loadCSVDataIfNeeded();
    }
    
    // Register a new user
    public boolean registerUser(String username, String email, String password) {
        // Normalize username (trim whitespace and convert to lowercase)
        username = username.trim().toLowerCase();
        
        // Check if username already exists
        if (userExists(username)) {
            return false;
        }
        
        // Hash the password for security
        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            return false;
        }
        
        // Save user data
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(username + "_email", email);
        editor.putString(username + "_password", hashedPassword);
        editor.apply();
        
        return true;
    }
    
    // Authenticate user login
    public boolean authenticateUser(String username, String password) {
        // Normalize username (trim whitespace and convert to lowercase)
        username = username.trim().toLowerCase();
        
        if (!userExists(username)) {
            return false;
        }
        
        String storedHashedPassword = sharedPreferences.getString(username + "_password", null);
        if (storedHashedPassword == null) {
            return false;
        }
        
        String hashedInputPassword = hashPassword(password);
        return hashedInputPassword != null && hashedInputPassword.equals(storedHashedPassword);
    }
    
    // Check if user exists
    public boolean userExists(String username) {
        // Normalize username (trim whitespace and convert to lowercase)
        username = username.trim().toLowerCase();
        return sharedPreferences.contains(username + "_password");
    }
    
    // Get user email
    public String getUserEmail(String username) {
        // Normalize username (trim whitespace and convert to lowercase)
        username = username.trim().toLowerCase();
        return sharedPreferences.getString(username + "_email", null);
    }
    
    // Debug method to get all stored users
    public java.util.Set<String> getAllStoredUsers() {
        java.util.Set<String> usernames = new java.util.HashSet<>();
        java.util.Map<String, ?> allEntries = sharedPreferences.getAll();
        
        for (String key : allEntries.keySet()) {
            if (key.endsWith("_password")) {
                String username = key.substring(0, key.length() - "_password".length());
                usernames.add(username);
            }
        }
        
        return usernames;
    }
    
    // Clear all user data (for testing/refresh purposes)
    private void clearAllUserData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
    
    // Force reload CSV data (for testing/debugging)
    public void forceReloadCSV() {
        clearAllUserData();
        loadCSVDataIfNeeded();
    }
    
    // Load CSV data into SharedPreferences if not already loaded
    private void loadCSVDataIfNeeded() {
        // Check if CSV data has already been loaded
        boolean csvLoaded = sharedPreferences.getBoolean(CSV_LOADED_KEY, false);
        if (csvLoaded) {
            return; // Data already loaded
        }
        
        // Clear any existing user data to ensure fresh load
        clearAllUserData();
        
        try {
            InputStream inputStream = context.getAssets().open("neighborlink_database.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            boolean isFirstLine = true;
            
            SharedPreferences.Editor editor = sharedPreferences.edit();
            
            while ((line = reader.readLine()) != null) {
                // Skip header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                // Parse CSV line: Username,Password,Credit,Item,Type,Distance,Status,Description,Like,Favor,Gmail
                String[] parts = line.split(",");
                if (parts.length >= 11) {
                    String username = parts[0].trim().toLowerCase(); // Normalize username
                    String password = parts[1].trim(); // Plain text password from CSV
                    String email = parts[10].trim(); // Gmail is at index 10
                    
                    // Hash the plain text password before storing
                    String hashedPassword = hashPassword(password);
                    if (hashedPassword != null) {
                        editor.putString(username + "_email", email);
                        editor.putString(username + "_password", hashedPassword);
                    }
                }
            }
            
            // Mark CSV data as loaded
            editor.putBoolean(CSV_LOADED_KEY, true);
            editor.apply();
            
            reader.close();
            inputStream.close();
            
        } catch (IOException e) {
            e.printStackTrace();
            // If CSV loading fails, that's okay - users can still register normally
        }
    }
    
    // Hash password using SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
