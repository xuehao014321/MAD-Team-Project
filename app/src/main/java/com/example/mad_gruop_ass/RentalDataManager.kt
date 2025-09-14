package com.example.mad_gruop_ass

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object RentalDataManager {
    
    private const val PREF_NAME = "rental_data"
    private const val KEY_ADDED_RECORDS = "added_records"
    
    /**
     * Get all records that were added to SharedPreferences
     */
    private fun getAddedRecords(context: Context): List<RentalRecord> {
        val records = mutableListOf<RentalRecord>()
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        
        val count = prefs.getInt("record_count", 0)
        
        for (i in 0 until count) {
            val username = prefs.getString("record_${i}_username", "") ?: ""
            val item = prefs.getString("record_${i}_item", "") ?: ""
            val type = prefs.getString("record_${i}_type", "") ?: ""
            val status = prefs.getString("record_${i}_status", "") ?: ""
            val description = prefs.getString("record_${i}_description", "") ?: ""
            val distance = prefs.getString("record_${i}_distance", "") ?: ""
            val credit = prefs.getInt("record_${i}_credit", 0)
            
            if (username.isNotEmpty() && item.isNotEmpty()) {
                val record = RentalRecord(username, item, type, status, description, distance, credit)
                records.add(record)
            }
        }
        
        return records
    }
    
    fun getAllRentalRecords(context: Context): List<RentalRecord> {
        // Only return added records from SharedPreferences
        return getAddedRecords(context)
    }
    
    fun getRentalHistoryForUser(context: Context, username: String): List<RentalRecord> {
        val allRecords = getAllRentalRecords(context)
        return allRecords.filter { it.username == username }
    }
    
    fun getItemsLentCount(context: Context, username: String): Int {
        val userRecords = getRentalHistoryForUser(context, username)
        return userRecords.count { it.type == "Lend" }
    }
    
    fun getItemsBorrowedCount(context: Context, username: String): Int {
        val userRecords = getRentalHistoryForUser(context, username)
        return userRecords.count { it.type == "Borrow" }
    }
    
    /**
     * Get total number of items for a user (both lent and borrowed)
     * @param context Application context
     * @param username Username to query
     * @return Total count of user's items (lent + borrowed)
     */
    fun getTotalItemsCount(context: Context, username: String): Int {
        val userRecords = getRentalHistoryForUser(context, username)
        return userRecords.size // Return total count of all records for this user
    }
    
    /**
     * Get total number of items for a user from API (both Available and Borrowed status)
     * @param context Application context
     * @param userId User ID to query
     * @param callback Callback to handle the result
     */
    fun getTotalItemsCountFromAPI(context: Context, userId: Int, callback: TotalItemsCallback) {
        Log.d("RentalDataManager", "=== GETTING TOTAL ITEMS COUNT FROM API FOR USER ID: $userId ===")
        
        ApiClient.getItemsByUserId(userId, object : ApiClient.ItemsListCallback {
            override fun onSuccess(items: List<Item>) {
                val totalCount = items.size // Count all items regardless of status
                Log.d("RentalDataManager", "Total items found for user $userId: $totalCount")
                
                // Log details for debugging
                var availableCount = 0
                var borrowedCount = 0
                for (item in items) {
                    when (item.status) {
                        "Available" -> availableCount++
                        "Borrowed" -> borrowedCount++
                    }
                    Log.d("RentalDataManager", "Item: ${item.title} | Status: ${item.status}")
                }
                
                Log.d("RentalDataManager", "Available items: $availableCount, Borrowed items: $borrowedCount")
                Log.d("RentalDataManager", "=== API ITEMS COUNT COMPLETE ===")
                
                callback.onSuccess(totalCount)
            }
            
            override fun onError(error: String) {
                Log.e("RentalDataManager", "Failed to get items from API: $error")
                callback.onError(error)
            }
        })
    }
    
    /**
     * Callback interface for total items count from API
     */
    interface TotalItemsCallback {
        fun onSuccess(totalCount: Int)
        fun onError(error: String)
    }
    
    /**
     * Get available items count for a user from API (only Available status)
     * @param context Application context
     * @param userId User ID to query
     * @param callback Callback to handle the result
     */
    fun getAvailableItemsCountFromAPI(context: Context, userId: Int, callback: AvailableItemsCallback) {
        Log.d("RentalDataManager", "=== GETTING AVAILABLE ITEMS COUNT FROM API FOR USER ID: $userId ===")
        
        ApiClient.getItemsByUserId(userId, object : ApiClient.ItemsListCallback {
            override fun onSuccess(items: List<Item>) {
                val availableCount = items.count { it.status == "Available" }
                
                // Log item details
                for (item in items) {
                    Log.d("RentalDataManager", "Item: ${item.title} | Status: ${item.status}")
                }
                
                Log.d("RentalDataManager", "Available items count for user $userId: $availableCount")
                Log.d("RentalDataManager", "=== AVAILABLE ITEMS COUNT COMPLETE ===")
                
                callback.onSuccess(availableCount)
            }
            
            override fun onError(error: String) {
                Log.e("RentalDataManager", "Failed to get available items from API: $error")
                callback.onError(error)
            }
        })
    }
    
    /**
     * Callback interface for available items count from API
     */
    interface AvailableItemsCallback {
        fun onSuccess(availableCount: Int)
        fun onError(error: String)
    }
    
    fun getUserCredit(context: Context, username: String): Int {
        // Calculate credit score based on actual activities
        return calculateUserCredit(context, username)
    }
    
    /**
     * Dynamically calculate credit score based on user rental activities
     * Base score: 50 points
     * Each rental record (Lend or Borrow): +5 points
     * Formula: 50 + (total_records × 5)
     */
    private fun calculateUserCredit(context: Context, username: String): Int {
        // Add initial debug info
        Log.d("RentalDataManager", "=== CALCULATING CREDIT FOR USER: $username ===")
        
        // Check SharedPreferences first
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val totalStoredRecords = prefs.getInt("record_count", 0)
        Log.d("RentalDataManager", "Total records in SharedPreferences: $totalStoredRecords")
        
        // Get all records first
        val allRecords = getAllRentalRecords(context)
        Log.d("RentalDataManager", "Total records retrieved: ${allRecords.size}")
        
        // Get user-specific records
        val userRecords = getRentalHistoryForUser(context, username)
        
        val baseCredit = 50 // Base 50 points
        val totalRecords = userRecords.size // Count all records (Lend and Borrow)
       
        // Add debug logging
        Log.d("RentalDataManager", "Target username: '$username'")
        Log.d("RentalDataManager", "User-specific records found: $totalRecords")
        
        if (userRecords.isEmpty()) {
            Log.d("RentalDataManager", "No records found for user: $username")
            Log.d("RentalDataManager", "Checking all usernames in database:")
            for (record in allRecords) {
                Log.d("RentalDataManager", "  Found username: '${record.username}'")
            }
        } else {
            userRecords.forEachIndexed { i, record ->
                Log.d("RentalDataManager", "Record ${i + 1}: Type=${record.type}, Item=${record.itemName}, Status=${record.status}")
            }
        }
        
        // Calculate score: base + 5 points per record
        val calculatedCredit = baseCredit + (totalRecords * 5)
        
        Log.d("RentalDataManager", "Base credit: $baseCredit")
        Log.d("RentalDataManager", "Total records count: $totalRecords")
        Log.d("RentalDataManager", "Points from records: ${totalRecords * 5}")
        Log.d("RentalDataManager", "Final calculated credit: $calculatedCredit")
        Log.d("RentalDataManager", "=== CREDIT CALCULATION COMPLETE ===")
        
        // Return calculated credit without upper limit
        return calculatedCredit
    }
    
    /**
     * Test method to query and print item counts for each user
     */
    fun testUserItemCounts(context: Context) {
        Log.d("RentalDataManager", "=== Testing User Item Counts ===")
        
        val allRecords = getAllRentalRecords(context)
        Log.d("RentalDataManager", "Total records in database: ${allRecords.size}")
        
        // Group records by username
        val userItemCounts = mutableMapOf<String, Int>()
        
        for (record in allRecords) {
            val username = record.username
            userItemCounts[username] = userItemCounts.getOrDefault(username, 0) + 1
            
            Log.d("RentalDataManager", "Record: User=$username, Item=${record.itemName}, Type=${record.type}, Status=${record.status}")
        }
        
        // Print results for each user
        Log.d("RentalDataManager", "=== User Item Count Summary ===")
        for ((username, itemCount) in userItemCounts) {
            val calculatedCredit = 50 + (itemCount * 5)
            
            Log.d("RentalDataManager", "User: $username | Items: $itemCount | Credit: $calculatedCredit")
        }
        
        Log.d("RentalDataManager", "=== Test Complete ===")
    }

    /**
     * Test method to get total items count from API for a specific user ID
     * @param context Application context
     * @param userId User ID to test
     */
    fun testUserTotalItemsFromAPI(context: Context, userId: Int) {
        Log.d("RentalDataManager", "=== TESTING API TOTAL ITEMS COUNT FOR USER ID: $userId ===")
        
        getTotalItemsCountFromAPI(context, userId, object : TotalItemsCallback {
            override fun onSuccess(totalCount: Int) {
                Log.d("RentalDataManager", "✅ SUCCESS: User $userId has $totalCount total items from API")
            }
            
            override fun onError(error: String) {
                Log.e("RentalDataManager", "❌ ERROR: Failed to get items count for user $userId: $error")
            }
        })
        
        Log.d("RentalDataManager", "=== API TEST INITIATED ===")
    }

    /**
     * Calculate user credit dynamically based on API item data
     * Base score: 50 points
     * Each item owned: +5 points
     * Formula: 50 + (total_items_from_api × 5)
     */
    fun getUserCreditFromAPI(context: Context, userId: Int, username: String, callback: CreditCallback) {
        Log.d("RentalDataManager", "=== CALCULATING CREDIT FROM API FOR USER: $username (ID: $userId) ===")
        
        // Get all items for user from API
        ApiClient.getItemsByUserId(userId, object : ApiClient.ItemsListCallback {
            override fun onSuccess(items: List<Item>) {
                val baseCredit = 50 // Base credit score
                val totalItems = items.size // Total items count from API
                val calculatedCredit = baseCredit + (totalItems * 5) // +5 points per item
                
                Log.d("RentalDataManager", "🎯 === API CREDIT CALCULATION RESULT === 🎯")
                Log.d("RentalDataManager", "👤 User: $username (ID: $userId)")
                Log.d("RentalDataManager", "📦 Total items from API: $totalItems")
                Log.d("RentalDataManager", "🔢 Base credit: $baseCredit")
                Log.d("RentalDataManager", "➕ Points from items: ${totalItems * 5}")
                Log.d("RentalDataManager", "🎉 Final calculated credit: $calculatedCredit")
                
                // Display item details
                if (totalItems > 0) {
                    Log.d("RentalDataManager", "📋 Item details:")
                    items.forEachIndexed { i, item ->
                        Log.d("RentalDataManager", "   ${i + 1}. ${item.title} (${item.status})")
                        Log.d("RentalDataManager", "      - Description: ${item.description}")
                        Log.d("RentalDataManager", "      - Price: $${item.price}")
                    }
                }
                
                Log.d("RentalDataManager", "=== API CREDIT CALCULATION COMPLETE ===")
                
                callback.onSuccess(calculatedCredit)
            }
            
            override fun onError(error: String) {
                Log.e("RentalDataManager", "❌ Failed to get items from API for credit calculation: $error")
                
                // If API call fails, fallback to local calculation
                Log.d("RentalDataManager", "💡 Fallback to local credit calculation")
                val localCredit = calculateUserCredit(context, username)
                callback.onSuccess(localCredit)
            }
        })
    }
    
    /**
     * Callback interface for credit calculation from API
     */
    interface CreditCallback {
        fun onSuccess(credit: Int)
        fun onError(error: String)
    }
}


