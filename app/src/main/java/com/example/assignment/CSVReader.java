package com.example.assignment;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {
    
    public static List<RentalRecord> readRentalRecords(Context context) {
        List<RentalRecord> records = new ArrayList<>();
        
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("neighborlink_database.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                String[] data = line.split(",");
                if (data.length >= 12) {
                    try {
                        RentalRecord record = new RentalRecord();
                        record.setUsername(data[0]);
                        record.setPassword(data[1]);
                        record.setCredit(Integer.parseInt(data[2]));
                        record.setItemName(data[3]);
                        record.setType(data[4]);
                        record.setDistance(data[5]);
                        record.setStatus(data[6]);
                        record.setDescription(data[7]);
                        record.setLike(Integer.parseInt(data[8]));
                        record.setFavor(Integer.parseInt(data[9]));
                        record.setGmail(data[10]);
                        record.setGender(data[11]);
                        records.add(record);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            reader.close();
            inputStream.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return records;
    }
    
    public static List<RentalRecord> getRentalHistoryForUser(Context context, String username) {
        List<RentalRecord> allRecords = readRentalRecords(context);
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
        List<RentalRecord> allRecords = readRentalRecords(context);
        
        for (RentalRecord record : allRecords) {
            if (record.getUsername().equals(username)) {
                return record.getCredit();
            }
        }
        
        return 100;
    }
    
    public static boolean validateUser(Context context, String username, String password) {
        List<RentalRecord> allRecords = readRentalRecords(context);
        
        for (RentalRecord record : allRecords) {
            if (record.getUsername().equals(username) && record.getPassword().equals(password)) {
                return true;
            }
        }
        
        return false;
    }
    
    public static String getUserGmail(Context context, String username) {
        List<RentalRecord> allRecords = readRentalRecords(context);
        
        for (RentalRecord record : allRecords) {
            if (record.getUsername().equals(username)) {
                return record.getGmail();
            }
        }
        
        return "gmail@gmail.com";
    }
    
    public static String getUserGender(Context context, String username) {
        List<RentalRecord> allRecords = readRentalRecords(context);
        
        for (RentalRecord record : allRecords) {
            if (record.getUsername().equals(username)) {
                return record.getGender();
            }
        }
        
        return "Unknown";
    }
}
