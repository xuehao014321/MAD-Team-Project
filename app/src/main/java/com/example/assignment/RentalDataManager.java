package com.example.assignment;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;

public class RentalDataManager {
    
    private static final String PREF_NAME = "rental_data";
    private static final String KEY_ADDED_RECORDS = "added_records";
    
    public static void addRentalRecord(Context context, RentalRecord record) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // Get existing records count
        int count = prefs.getInt("record_count", 0);
        
        // Save the new record
        editor.putString("record_" + count + "_username", record.getUsername());
        editor.putString("record_" + count + "_item", record.getItemName());
        editor.putString("record_" + count + "_type", record.getType());
        editor.putString("record_" + count + "_status", record.getStatus());
        editor.putString("record_" + count + "_description", record.getDescription());
        editor.putString("record_" + count + "_distance", record.getDistance());
        editor.putInt("record_" + count + "_credit", record.getCredit());
        
        // Increment count
        editor.putInt("record_count", count + 1);
        editor.apply();
    }
    
    public static List<RentalRecord> getAllRentalRecords(Context context) {
        List<RentalRecord> csvRecords = CSVReader.readRentalRecords(context);
        List<RentalRecord> addedRecords = getAddedRecords(context);
        
        // Combine CSV records with added records
        List<RentalRecord> allRecords = new ArrayList<>();
        allRecords.addAll(csvRecords);
        allRecords.addAll(addedRecords);
        
        return allRecords;
    }
    
    public static List<RentalRecord> getRentalHistoryForUser(Context context, String username) {
        List<RentalRecord> allRecords = getAllRentalRecords(context);
        List<RentalRecord> userRecords = new ArrayList<>();
        
        for (RentalRecord record : allRecords) {
            if (record.getUsername().equals(username)) {
                userRecords.add(record);
            }
        }
        
        return userRecords;
    }
    
    public static int getItemsLentCount(Context context, String username) {
        List<RentalRecord> userRecords = getRentalHistoryForUser(context, username);
        int count = 0;
        
        for (RentalRecord record : userRecords) {
            if ("Lend".equals(record.getType())) {
                count++;
            }
        }
        
        return count;
    }
    
    public static int getItemsBorrowedCount(Context context, String username) {
        List<RentalRecord> userRecords = getRentalHistoryForUser(context, username);
        int count = 0;
        
        for (RentalRecord record : userRecords) {
            if ("Borrow".equals(record.getType())) {
                count++;
            }
        }
        
        return count;
    }
    
    public static int getUserCredit(Context context, String username) {
        List<RentalRecord> allRecords = getAllRentalRecords(context);
        
        for (RentalRecord record : allRecords) {
            if (record.getUsername().equals(username)) {
                return record.getCredit();
            }
        }
        
        return 100;
    }
    
    private static List<RentalRecord> getAddedRecords(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        List<RentalRecord> records = new ArrayList<>();
        
        int count = prefs.getInt("record_count", 0);
        
        for (int i = 0; i < count; i++) {
            RentalRecord record = new RentalRecord();
            record.setUsername(prefs.getString("record_" + i + "_username", ""));
            record.setItemName(prefs.getString("record_" + i + "_item", ""));
            record.setType(prefs.getString("record_" + i + "_type", ""));
            record.setStatus(prefs.getString("record_" + i + "_status", ""));
            record.setDescription(prefs.getString("record_" + i + "_description", ""));
            record.setDistance(prefs.getString("record_" + i + "_distance", ""));
            record.setCredit(prefs.getInt("record_" + i + "_credit", 100));
            records.add(record);
        }
        
        return records;
    }
}
