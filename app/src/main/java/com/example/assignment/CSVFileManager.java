package com.example.assignment;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVFileManager {

    private static final String CSV_FILENAME = "neighborlink_database.csv";
    private File csvFile;
    private Context context;

    public CSVFileManager(Context context) {
        this.context = context;
        this.csvFile = new File(context.getFilesDir(), CSV_FILENAME);
        if (!csvFile.exists()) {
            copyCsvFromAssets();
        }
    }

    private void copyCsvFromAssets() {
        Log.d("CSVFileManager", "Copying CSV from assets to: " + csvFile.getAbsolutePath());
        AssetManager assetManager = context.getAssets();
        try (InputStream inputStream = assetManager.open(CSV_FILENAME);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {

            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
                lineCount++;
            }
            Log.d("CSVFileManager", "Successfully copied " + lineCount + " lines from assets");
        } catch (IOException e) {
            Log.e("CSVFileManager", "Error copying CSV from assets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<RentalRecord> readAllRecords() {
        List<RentalRecord> records = new ArrayList<>();
        Log.d("CSVFileManager", "Starting to read CSV file: " + csvFile.getAbsolutePath());
        Log.d("CSVFileManager", "File exists: " + csvFile.exists());

        try {
            BufferedReader reader = new BufferedReader(new FileReader(csvFile));
            String line;
            boolean isFirstLine = true;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (isFirstLine) {
                    isFirstLine = false;
                    Log.d("CSVFileManager", "Skipping header: " + line);
                    continue; // Skip header
                }

                Log.d("CSVFileManager", "Reading line " + lineNumber + ": " + line);
                
                // 使用更健壮的CSV解析方法
                String[] data = parseCSVLine(line);
                Log.d("CSVFileManager", "Parsed " + data.length + " fields from line " + lineNumber);
                
                // 添加详细的字段调试信息
                for (int i = 0; i < data.length; i++) {
                    Log.d("CSVFileManager", "Field " + i + ": '" + data[i] + "'");
                }
                
                // 检查字段数量 - CSV有13个字段，但ProfilePic是最后一个，我们只需要前12个
                if (data.length >= 12) {
                    try {
                        RentalRecord record = new RentalRecord();
                        record.setUsername(data[0]);
                        record.setPassword(data[1]);
                        
                        Log.d("CSVFileManager", "Setting username: " + data[0] + ", password: " + data[1]);
                        
                        // 更安全的数字解析
                        try {
                            record.setCredit(Integer.parseInt(data[2]));
                            Log.d("CSVFileManager", "Setting credit: " + data[2]);
                        } catch (NumberFormatException e) {
                            Log.w("CSVFileManager", "Invalid credit value on line " + lineNumber + ": " + data[2] + ", using default 100");
                            record.setCredit(100);
                        }
                        
                        record.setItemName(data[3]);
                        record.setType(data[4]);
                        record.setDistance(data[5]);
                        record.setStatus(data[6]);
                        record.setDescription(data[7]);
                        
                        Log.d("CSVFileManager", "Setting item: " + data[3] + ", type: " + data[4] + ", distance: " + data[5]);
                        Log.d("CSVFileManager", "Setting status: " + data[6] + ", description: " + data[7]);
                        
                        try {
                            record.setLike(Integer.parseInt(data[8]));
                            Log.d("CSVFileManager", "Setting like: " + data[8]);
                        } catch (NumberFormatException e) {
                            Log.w("CSVFileManager", "Invalid like value on line " + lineNumber + ": " + data[8] + ", using default 0");
                            record.setLike(0);
                        }
                        
                        try {
                            record.setFavor(Integer.parseInt(data[9]));
                            Log.d("CSVFileManager", "Setting favor: " + data[9]);
                        } catch (NumberFormatException e) {
                            Log.w("CSVFileManager", "Invalid favor value on line " + lineNumber + ": " + data[9] + ", using default 0");
                            record.setFavor(0);
                        }
                        
                        record.setGmail(data[10]);
                        record.setGender(data[11]);
                        
                        Log.d("CSVFileManager", "Setting gmail: " + data[10] + ", gender: " + data[11]);
                        
                        records.add(record);
                        Log.d("CSVFileManager", "Successfully added record for user: " + data[0]);
                    } catch (Exception e) {
                        Log.e("CSVFileManager", "Error processing line " + lineNumber + ": " + e.getMessage());
                        Log.e("CSVFileManager", "Problematic line: " + line);
                        e.printStackTrace();
                        // 继续处理下一行，而不是停止
                    }
                } else {
                    Log.w("CSVFileManager", "Line " + lineNumber + " has insufficient fields (" + data.length + "), skipping");
                    Log.w("CSVFileManager", "Problematic line: " + line);
                }
            }

            reader.close();
            Log.d("CSVFileManager", "Finished reading CSV. Total records loaded: " + records.size());
        } catch (IOException e) {
            Log.e("CSVFileManager", "Error reading CSV file: " + e.getMessage());
            e.printStackTrace();
        }

        return records;
    }

    // 添加CSV行解析方法
    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(currentField.toString().trim());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        result.add(currentField.toString().trim());
        return result.toArray(new String[0]);
    }

    public boolean addRecord(RentalRecord record) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile, true)); // Append mode

            // Write the new record
            writer.write(record.getUsername() + ",");
            writer.write(record.getPassword() + ","); // 使用实际的密码，而不是硬编码的 "password"
            writer.write(record.getCredit() + ",");
            writer.write(record.getItemName() + ",");
            writer.write(record.getType() + ",");
            writer.write(record.getDistance() + ",");
            writer.write(record.getStatus() + ",");
            writer.write(record.getDescription() + ",");
            writer.write(record.getLike() + ",");
            writer.write(record.getFavor() + ",");
            writer.write(record.getGmail() != null ? record.getGmail() : ""); // Write Gmail
            writer.write(",");
            writer.write(record.getGender() != null ? record.getGender() : ""); // Write Gender
            writer.write(",");
            writer.write(""); // ProfilePic placeholder
            writer.newLine();

            writer.close();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Note: updateRecord and deleteRecord are not currently used but provided for completeness
    public boolean updateRecord(int index, RentalRecord record) {
        List<RentalRecord> allRecords = readAllRecords();

        if (index >= 0 && index < allRecords.size()) {
            allRecords.set(index, record);
            return writeAllRecords(allRecords);
        }

        return false;
    }

    public boolean deleteRecord(int index) {
        List<RentalRecord> allRecords = readAllRecords();

        if (index >= 0 && index < allRecords.size()) {
            allRecords.remove(index);
            return writeAllRecords(allRecords);
        }

        return false;
    }

    private boolean writeAllRecords(List<RentalRecord> records) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));

            // Write header
            writer.write("Username,Password,Credit,Item,Type,Distance,Status,Description,Like,Favor,Gmail,Gender,ProfilePic");
            writer.newLine();

            // Write all records
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
                writer.write(record.getFavor() + ",");
                writer.write(record.getGmail() != null ? record.getGmail() : ""); // Write Gmail
                writer.write(",");
                writer.write(record.getGender() != null ? record.getGender() : ""); // Write Gender
                writer.write(",");
                writer.write(""); // ProfilePic placeholder
                writer.newLine();
            }

            writer.close();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<RentalRecord> getRecordsForUser(String username) {
        List<RentalRecord> allRecords = readAllRecords();
        List<RentalRecord> userRecords = new ArrayList<>();

        for (RentalRecord record : allRecords) {
            if (record.getUsername().equals(username)) {
                userRecords.add(record);
            }
        }

        return userRecords;
    }

    public int getItemsLentCount(String username) {
        List<RentalRecord> userRecords = getRecordsForUser(username);
        int count = 0;

        for (RentalRecord record : userRecords) {
            if ("Lend".equals(record.getType())) {
                count++;
            }
        }

        return count;
    }

    public int getItemsBorrowedCount(String username) {
        List<RentalRecord> userRecords = getRecordsForUser(username);
        int count = 0;

        for (RentalRecord record : userRecords) {
            if ("Borrow".equals(record.getType())) {
                count++;
            }
        }

        return count;
    }

    public int getUserCredit(String username) {
        List<RentalRecord> allRecords = readAllRecords();

        for (RentalRecord record : allRecords) {
            if (record.getUsername().equals(username)) {
                return record.getCredit();
            }
        }

        return 100; // Default credit if user not found
    }

    public String getUserGmail(String username) {
        List<RentalRecord> allRecords = readAllRecords();
        Log.d("CSVFileManager", "Looking for user: " + username);
        Log.d("CSVFileManager", "Total records: " + allRecords.size());
        
        // 添加更详细的调试信息
        Log.d("CSVFileManager", "Searching for username: '" + username + "' (length: " + username.length() + ")");

        for (RentalRecord record : allRecords) {
            Log.d("CSVFileManager", "Checking record for user: '" + record.getUsername() + 
                  "' (length: " + record.getUsername().length() + "), gmail: " + record.getGmail());
            Log.d("CSVFileManager", "Username match: " + record.getUsername().equals(username));
            if (record.getUsername().equals(username)) {
                Log.d("CSVFileManager", "Found user " + username + " with gmail: " + record.getGmail());
                return record.getGmail();
            }
        }

        Log.d("CSVFileManager", "User " + username + " not found, returning default gmail");
        return "gmail@gmail.com"; // Default gmail if user not found
    }

    public String getUserGender(String username) {
        List<RentalRecord> allRecords = readAllRecords();
        Log.d("CSVFileManager", "Looking for user: " + username);

        for (RentalRecord record : allRecords) {
            Log.d("CSVFileManager", "Checking record for user: " + record.getUsername() + 
                  ", gender: " + record.getGender());
            if (record.getUsername().equals(username)) {
                Log.d("CSVFileManager", "Found user " + username + " with gender: " + record.getGender());
                return record.getGender();
            }
        }

        Log.d("CSVFileManager", "User " + username + " not found, returning default gender");
        return "Unknown"; // Default gender if user not found
    }

    public boolean validateUser(String username, String password) {
        List<RentalRecord> allRecords = readAllRecords();
        Log.d("CSVFileManager", "Validating user: " + username);
        Log.d("CSVFileManager", "Total records for validation: " + allRecords.size());

        for (RentalRecord record : allRecords) {
            Log.d("CSVFileManager", "Checking validation for user: " + record.getUsername() + 
                  ", password: " + record.getPassword());
            if (record.getUsername().equals(username) && record.getPassword().equals(password)) {
                Log.d("CSVFileManager", "User validation successful for: " + username);
                return true;
            }
        }

        Log.d("CSVFileManager", "User validation failed for: " + username);
        return false;
    }
}
