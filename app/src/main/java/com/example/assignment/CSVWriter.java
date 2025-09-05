package com.example.assignment;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVWriter {
    
    public static boolean addRentalRecord(Context context, RentalRecord record) {
        try {
            // Read existing data
            List<RentalRecord> allRecords = CSVReader.readRentalRecords(context);
            
            // Add new record
            allRecords.add(record);
            
            // Write back to file
            return writeRecordsToFile(context, allRecords);
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean writeRecordsToFile(Context context, List<RentalRecord> records) {
        try {
            // Get the assets directory
            AssetManager assetManager = context.getAssets();
            
            // Create a temporary file in internal storage
            File tempFile = new File(context.getFilesDir(), "neighborlink_database_temp.csv");
            FileWriter fileWriter = new FileWriter(tempFile);
            BufferedWriter writer = new BufferedWriter(fileWriter);
            
            // Write header
            writer.write("Username,Password,Credit,Item,Type,Distance,Status,Description,Like,Favor");
            writer.newLine();
            
            // Write records
            for (RentalRecord record : records) {
                writer.write(record.getUsername() + ",");
                writer.write("password,"); // Default password
                writer.write(record.getCredit() + ",");
                writer.write(record.getItemName() + ",");
                writer.write(record.getType() + ",");
                writer.write(record.getDistance() + ",");
                writer.write(record.getStatus() + ",");
                writer.write(record.getDescription() + ",");
                writer.write(record.getLike() + ",");
                writer.write(record.getFavor() + "");
                writer.newLine();
            }
            
            writer.close();
            fileWriter.close();
            
            // Copy temp file back to assets (this won't work in production, but for demo purposes)
            // In a real app, you'd use a database or external storage
            
            return true;
            
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
