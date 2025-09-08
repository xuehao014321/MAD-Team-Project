package com.example.assignment;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
    
    /**
     * Get all records that were added to SharedPreferences
     */
    private static List<RentalRecord> getAddedRecords(Context context) {
        List<RentalRecord> records = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        int count = prefs.getInt("record_count", 0);
        
        for (int i = 0; i < count; i++) {
            String username = prefs.getString("record_" + i + "_username", "");
            String item = prefs.getString("record_" + i + "_item", "");
            String type = prefs.getString("record_" + i + "_type", "");
            String status = prefs.getString("record_" + i + "_status", "");
            String description = prefs.getString("record_" + i + "_description", "");
            String distance = prefs.getString("record_" + i + "_distance", "");
            int credit = prefs.getInt("record_" + i + "_credit", 0);
            
            if (!username.isEmpty() && !item.isEmpty()) {
                RentalRecord record = new RentalRecord(username, item, type, status, description, distance, credit);
                records.add(record);
            }
        }
        
        return records;
    }
    
    public static List<RentalRecord> getAllRentalRecords(Context context) {
        // Only return added records from SharedPreferences
        return getAddedRecords(context);
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
    
    /**
     * Get total number of items for a user (both lent and borrowed)
     * @param context Application context
     * @param username Username to query
     * @return Total count of user's items (lent + borrowed)
     */
    public static int getTotalItemsCount(Context context, String username) {
        List<RentalRecord> userRecords = getRentalHistoryForUser(context, username);
        return userRecords.size(); // Return total count of all records for this user
    }
    
    /**
     * Get total number of items for a user from API (both Available and Borrowed status)
     * @param context Application context
     * @param userId User ID to query
     * @param callback Callback to handle the result
     */
    public static void getTotalItemsCountFromAPI(Context context, int userId, final TotalItemsCallback callback) {
        Log.d("RentalDataManager", "=== GETTING TOTAL ITEMS COUNT FROM API FOR USER ID: " + userId + " ===");
        
        ApiClient.getItemsByUserId(userId, new ApiClient.ItemsListCallback() {
            @Override
            public void onSuccess(List<Item> items) {
                int totalCount = items.size(); // Count all items regardless of status
                Log.d("RentalDataManager", "Total items found for user " + userId + ": " + totalCount);
                
                // Log details for debugging
                int availableCount = 0;
                int borrowedCount = 0;
                for (Item item : items) {
                    if ("Available".equals(item.getStatus())) {
                        availableCount++;
                    } else if ("Borrowed".equals(item.getStatus())) {
                        borrowedCount++;
                    }
                    Log.d("RentalDataManager", "Item: " + item.getTitle() + " | Status: " + item.getStatus());
                }
                
                Log.d("RentalDataManager", "Available items: " + availableCount + ", Borrowed items: " + borrowedCount);
                Log.d("RentalDataManager", "=== API ITEMS COUNT COMPLETE ===");
                
                callback.onSuccess(totalCount);
            }
            
            @Override
            public void onError(String error) {
                Log.e("RentalDataManager", "Failed to get items from API: " + error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * Callback interface for total items count from API
     */
    public interface TotalItemsCallback {
        void onSuccess(int totalCount);
        void onError(String error);
    }
    
    public static int getUserCredit(Context context, String username) {
        // Calculate credit score based on actual activities
        return calculateUserCredit(context, username);
    }
    
    /**
     * Dynamically calculate credit score based on user rental activities
     * Base score: 50 points
     * Each rental record (Lend or Borrow): +5 points
     * Formula: 50 + (total_records × 5)
     */
    private static int calculateUserCredit(Context context, String username) {
        // Add initial debug info
        Log.d("RentalDataManager", "=== CALCULATING CREDIT FOR USER: " + username + " ===");
        
        // Check SharedPreferences first
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int totalStoredRecords = prefs.getInt("record_count", 0);
        Log.d("RentalDataManager", "Total records in SharedPreferences: " + totalStoredRecords);
        
        // Get all records first
        List<RentalRecord> allRecords = getAllRentalRecords(context);
        Log.d("RentalDataManager", "Total records retrieved: " + allRecords.size());
        
        // Get user-specific records
        List<RentalRecord> userRecords = getRentalHistoryForUser(context, username);
        
        int baseCredit = 50; // Base 60 points (updated from 50)
        int totalRecords = userRecords.size(); // Count all records (Lend and Borrow)
       
        // Add debug logging
        Log.d("RentalDataManager", "Target username: '" + username + "'");
        Log.d("RentalDataManager", "User-specific records found: " + totalRecords);
        
        if (userRecords.isEmpty()) {
            Log.d("RentalDataManager", "No records found for user: " + username);
            Log.d("RentalDataManager", "Checking all usernames in database:");
            for (RentalRecord record : allRecords) {
                Log.d("RentalDataManager", "  Found username: '" + record.getUsername() + "'");
            }
        } else {
            for (int i = 0; i < userRecords.size(); i++) {
                RentalRecord record = userRecords.get(i);
                Log.d("RentalDataManager", "Record " + (i+1) + ": Type=" + record.getType() + 
                      ", Item=" + record.getItemName() + ", Status=" + record.getStatus());
            }
        }
        
        // Calculate score: base + 5 points per record
        int calculatedCredit = baseCredit + (totalRecords * 5);
        
        Log.d("RentalDataManager", "Base credit: " + baseCredit);
        Log.d("RentalDataManager", "Total records count: " + totalRecords);
        Log.d("RentalDataManager", "Points from records: " + (totalRecords * 5));
        Log.d("RentalDataManager", "Final calculated credit: " + calculatedCredit);
        Log.d("RentalDataManager", "=== CREDIT CALCULATION COMPLETE ===");
        
        // Return calculated credit without upper limit
        return calculatedCredit;
    }
    
    /**
     * Test method to query and print item counts for each user
     */
    public static void testUserItemCounts(Context context) {
        Log.d("RentalDataManager", "=== Testing User Item Counts ===");
        
        List<RentalRecord> allRecords = getAllRentalRecords(context);
        Log.d("RentalDataManager", "Total records in database: " + allRecords.size());
        
        // Group records by username
        Map<String, Integer> userItemCounts = new HashMap<>();
        
        for (RentalRecord record : allRecords) {
            String username = record.getUsername();
            userItemCounts.put(username, userItemCounts.getOrDefault(username, 0) + 1);
            
            Log.d("RentalDataManager", "Record: User=" + username + 
                  ", Item=" + record.getItemName() + 
                  ", Type=" + record.getType() + 
                  ", Status=" + record.getStatus());
        }
        
        // Print results for each user
        Log.d("RentalDataManager", "=== User Item Count Summary ===");
        for (Map.Entry<String, Integer> entry : userItemCounts.entrySet()) {
            String username = entry.getKey();
            int itemCount = entry.getValue();
            int calculatedCredit = 50 + (itemCount * 5);
            
            Log.d("RentalDataManager", "User: " + username + 
                  " | Items: " + itemCount + 
                  " | Credit: " + calculatedCredit);
        }
        
        Log.d("RentalDataManager", "=== Test Complete ===");
    }
    
    
    
    /**
     * Test method to get total items count from API for a specific user ID
     * @param context Application context
     * @param userId User ID to test
     */
    public static void testUserTotalItemsFromAPI(Context context, int userId) {
        Log.d("RentalDataManager", "=== TESTING API TOTAL ITEMS COUNT FOR USER ID: " + userId + " ===");
        
        getTotalItemsCountFromAPI(context, userId, new TotalItemsCallback() {
            @Override
            public void onSuccess(int totalCount) {
                Log.d("RentalDataManager", "✅ SUCCESS: User " + userId + " has " + totalCount + " total items from API");
            }
            
            @Override
            public void onError(String error) {
                Log.e("RentalDataManager", "❌ ERROR: Failed to get items count for user " + userId + ": " + error);
            }
        });
        
        Log.d("RentalDataManager", "=== API TEST INITIATED ===");
    }

    /**
     * Calculate user credit dynamically based on API item data (like alice_api_query.js logic)
     * Base score: 50 points
     * Each item owned: +5 points
     * Formula: 50 + (total_items_from_api × 5)
     */
    public static void getUserCreditFromAPI(Context context, int userId, String username, final CreditCallback callback) {
        Log.d("RentalDataManager", "=== CALCULATING CREDIT FROM API FOR USER: " + username + " (ID: " + userId + ") ===");
        
        // 获取用户在API中的所有物品（类似alice_api_query.js的逻辑）
        ApiClient.getItemsByUserId(userId, new ApiClient.ItemsListCallback() {
            @Override
            public void onSuccess(List<Item> items) {
                int baseCredit = 50; // 基础credit分数
                int totalItems = items.size(); // API中的物品总数
                int calculatedCredit = baseCredit + (totalItems * 5); // 每个物品+5分
                
                Log.d("RentalDataManager", "🎯 === API CREDIT CALCULATION RESULT === 🎯");
                Log.d("RentalDataManager", "👤 User: " + username + " (ID: " + userId + ")");
                Log.d("RentalDataManager", "📦 Total items from API: " + totalItems);
                Log.d("RentalDataManager", "🔢 Base credit: " + baseCredit);
                Log.d("RentalDataManager", "➕ Points from items: " + (totalItems * 5));
                Log.d("RentalDataManager", "🎉 Final calculated credit: " + calculatedCredit);
                
                // 显示物品详情（类似alice_api_query.js）
                if (totalItems > 0) {
                    Log.d("RentalDataManager", "📋 Item details:");
                    for (int i = 0; i < items.size(); i++) {
                        Item item = items.get(i);
                        Log.d("RentalDataManager", "   " + (i + 1) + ". " + item.getTitle() + " (" + item.getStatus() + ")");
                        Log.d("RentalDataManager", "      - Description: " + item.getDescription());
                        Log.d("RentalDataManager", "      - Price: $" + item.getPrice());
                    }
                }
                
                Log.d("RentalDataManager", "=== API CREDIT CALCULATION COMPLETE ===");
                
                callback.onSuccess(calculatedCredit);
            }
            
            @Override
            public void onError(String error) {
                Log.e("RentalDataManager", "❌ Failed to get items from API for credit calculation: " + error);
                
                // 如果API调用失败，fallback到本地计算
                Log.d("RentalDataManager", "💡 Fallback to local credit calculation");
                int localCredit = calculateUserCredit(context, username);
                callback.onSuccess(localCredit);
            }
        });
    }
    
    /**
     * Callback interface for credit calculation from API
     */
    public interface CreditCallback {
        void onSuccess(int credit);
        void onError(String error);
    }

    /**
     * Test Alice user credit calculation from API (like alice_api_query.js)
     * Alice user ID is 1 in the API
     */
    public static void testAliceApiCredit(Context context, final CreditCallback callback) {
        final int ALICE_USER_ID = 1; // Alice's user ID in API
        Log.d("RentalDataManager", "🎯 === ALICE API CREDIT TEST (like alice_api_query.js) === 🎯");
        
        // First get Alice user info from API
        ApiClient.getUserById(ALICE_USER_ID, new ApiClient.UserCallback() {
            @Override
            public void onSuccess(User aliceUser) {
                Log.d("RentalDataManager", "👤 Alice用户信息:");
                Log.d("RentalDataManager", "   - 用户ID: " + aliceUser.getUserId());
                Log.d("RentalDataManager", "   - 用户名: " + aliceUser.getUsername());
                Log.d("RentalDataManager", "   - 邮箱: " + aliceUser.getEmail());
                Log.d("RentalDataManager", "   - 电话: " + aliceUser.getPhone());
                
                // Then calculate credit using API items
                getUserCreditFromAPI(context, ALICE_USER_ID, aliceUser.getUsername(), new CreditCallback() {
                    @Override
                    public void onSuccess(int credit) {
                        Log.d("RentalDataManager", "🎉 Alice用户积分测试完成: " + credit + "分");
                        callback.onSuccess(credit);
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.e("RentalDataManager", "❌ Alice积分计算失败: " + error);
                        callback.onError(error);
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                Log.e("RentalDataManager", "❌ 无法获取Alice用户信息: " + error);
                callback.onError("无法获取Alice用户信息: " + error);
            }
        });
    }
    
    /**
     * Add test rental data for demonstration purposes
     */
    public static void addTestData(Context context) {
        Log.d("RentalDataManager", "Adding test rental data...");
        
        // Add some sample rental records for different users
        addRentalRecord(context, new RentalRecord("alice", "Laptop", "Lend", "Available", "MacBook Pro 2021", "0.5 km", 5));
        addRentalRecord(context, new RentalRecord("alice", "Camera", "Lend", "Available", "Canon DSLR", "1.2 km", 5));
        addRentalRecord(context, new RentalRecord("alice", "Book", "Borrow", "Borrowed", "Java Programming", "2.1 km", 0));
        
        addRentalRecord(context, new RentalRecord("bob", "Bicycle", "Lend", "Available", "Mountain bike", "0.8 km", 5));
        addRentalRecord(context, new RentalRecord("bob", "Textbook", "Borrow", "Borrowed", "Physics textbook", "1.5 km", 0));
        
        addRentalRecord(context, new RentalRecord("charlie", "Power Bank", "Lend", "Available", "20000mAh power bank", "0.3 km", 5));
        addRentalRecord(context, new RentalRecord("charlie", "Umbrella", "Borrow", "Borrowed", "Black umbrella", "1.0 km", 0));
        
        Log.d("RentalDataManager", "Test data added successfully");
    }
} 