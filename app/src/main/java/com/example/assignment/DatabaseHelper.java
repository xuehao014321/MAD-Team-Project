package com.example.assignment;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * Database helper class - handles SQLite database operations
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "rental_system.db";
    private static final int DATABASE_VERSION = 1;
    
    // Table names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_ITEMS = "items";
    private static final String TABLE_RENTAL_RECORDS = "rental_records";
    
    // User table fields
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PHONE = "phone";
    
    // Item table fields
    private static final String COLUMN_ITEM_ID = "id";
    private static final String COLUMN_ITEM_USER_ID = "user_id";
    private static final String COLUMN_ITEM_TITLE = "title";
    private static final String COLUMN_ITEM_DESCRIPTION = "description";
    private static final String COLUMN_ITEM_STATUS = "status";
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables...");
        
        // Create user table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT UNIQUE, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_PHONE + " TEXT)";
        
        // Create item table
        String createItemsTable = "CREATE TABLE " + TABLE_ITEMS + " (" +
                COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ITEM_USER_ID + " INTEGER, " +
                COLUMN_ITEM_TITLE + " TEXT, " +
                COLUMN_ITEM_DESCRIPTION + " TEXT, " +
                COLUMN_ITEM_STATUS + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_ITEM_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "))";
        
        // Create rental records table
        String createRentalRecordsTable = "CREATE TABLE " + TABLE_RENTAL_RECORDS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "item_id INTEGER, " +
                "type TEXT, " +
                "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id), " +
                "FOREIGN KEY(item_id) REFERENCES " + TABLE_ITEMS + "(id))";
        
        db.execSQL(createUsersTable);
        db.execSQL(createItemsTable);
        db.execSQL(createRentalRecordsTable);
        
        // Insert test data
        insertTestData(db);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RENTAL_RECORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
    
    /**
     * Insert test data
     */
    private void insertTestData(SQLiteDatabase db) {
        Log.d(TAG, "Inserting test data...");
        
        // Insert user data
        db.execSQL("INSERT INTO " + TABLE_USERS + " (username, email, phone) VALUES ('bob', 'bob@example.com', '123-456-7891')");
        db.execSQL("INSERT INTO " + TABLE_USERS + " (username, email, phone) VALUES ('charlie', 'charlie@example.com', '123-456-7892')");
        
        // Insert users' item data
        db.execSQL("INSERT INTO " + TABLE_ITEMS + " (user_id, title, description, status) VALUES (1, 'Bob''s Guitar', 'Electric guitar', 'Available')");
        db.execSQL("INSERT INTO " + TABLE_ITEMS + " (user_id, title, description, status) VALUES (2, 'Charlie''s Skateboard', 'Professional skateboard', 'Available')");
        
        Log.d(TAG, "Test data insertion completed");
    }
    
    /**
     * Find user by username
     */
    public User getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;
        
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});
        
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE));
            
            // Create User object using default constructor and setter methods
            user = new User();
            user.setUserId(id);
            user.setUsername(username);
            user.setEmail(email);
            user.setPhone(phone);
            // Set default values
            user.setGender("");    // No gender field in database
            user.setDistanceString("0");  // Default distance
            user.setCreatedAt("");        // No creation time field in database
            user.setAvatarUrl(null);      // No avatar field in database
            user.setCredit(0);            // Default credit
        }
        
        cursor.close();
        return user;
    }
    
    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;
        
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
            String username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE));
            
            // Create User object using default constructor and setter methods
            user = new User();
            user.setUserId(id);
            user.setUsername(username);
            user.setEmail(email);
            user.setPhone(phone);
            // Set default values
            user.setGender("");    // No gender field in database
            user.setDistanceString("0");  // Default distance
            user.setCreatedAt("");        // No creation time field in database
            user.setAvatarUrl(null);      // No avatar field in database
            user.setCredit(0);            // Default credit
        }
        
        cursor.close();
        return user;
    }
    
    /**
     * Find user by user ID
     */
    public User getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;
        
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        if (cursor.moveToFirst()) {
            String username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE));
            
            // Create User object using default constructor and setter methods
            user = new User();
            user.setUserId(userId);
            user.setUsername(username);
            user.setEmail(email);
            user.setPhone(phone);
            // Set default values
            user.setPassword("");  // No password field in database
            user.setGender("");    // No gender field in database
            user.setDistanceString("0");  // Default distance
            user.setCreatedAt("");        // No creation time field in database
            user.setAvatarUrl(null);      // No avatar field in database
            user.setCredit(0);            // Default credit
        }
        
        cursor.close();
        return user;
    }
    
    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<User> users = new ArrayList<>();
        
        String query = "SELECT * FROM " + TABLE_USERS + " ORDER BY " + COLUMN_USERNAME;
        Cursor cursor = db.rawQuery(query, null);
        
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
            String username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE));
            
            // Create User object using default constructor and setter methods
            User user = new User();
            user.setUserId(id);
            user.setUsername(username);
            user.setEmail(email);
            user.setPhone(phone);
            // Set default values
            user.setPassword("");  // No password field in database
            user.setGender("");    // No gender field in database
            user.setDistanceString("0");  // Default distance
            user.setCreatedAt("");        // No creation time field in database
            user.setAvatarUrl(null);      // No avatar field in database
            user.setCredit(0);            // Default credit
            users.add(user);
        }
        
        cursor.close();
        return users;
    }
    
    /**
     * Get all items for specified user
     */
    public List<Item> getItemsByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Item> items = new ArrayList<>();
        
        String query = "SELECT * FROM " + TABLE_ITEMS + " WHERE " + COLUMN_ITEM_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        while (cursor.moveToNext()) {
            int itemId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_TITLE));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_DESCRIPTION));
            String status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_STATUS));
            
            // Create Item object using default constructor and setter methods
            Item item = new Item();
            item.setItemId(itemId);
            item.setUserId(userId);
            item.setTitle(title);
            item.setDescription(description);
            item.setStatus(status);
            // Set default values
            item.setPrice(0.0);        // No price field in database
            item.setImageUrl("");      // No image field in database
            item.setViews(0);          // No views field in database
            item.setLikes(0);          // No likes field in database
            item.setDistance(0.0);     // No distance field in database
            item.setCreatedAt("");     // No creation time field in database
            items.add(item);
        }
        
        cursor.close();
        return items;
    }
    
    /**
     * Get item count for specified user
     */
    public int getItemsCountByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        
        String query = "SELECT COUNT(*) FROM " + TABLE_ITEMS + " WHERE " + COLUMN_ITEM_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        
        cursor.close();
        return count;
    }
    
    /**
     * Get item count for specified username
     */
    public int getItemsCountByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        
        String query = "SELECT COUNT(*) FROM " + TABLE_ITEMS + " i " +
                      "JOIN " + TABLE_USERS + " u ON i." + COLUMN_ITEM_USER_ID + " = u." + COLUMN_USER_ID + " " +
                      "WHERE u." + COLUMN_USERNAME + " = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{username});
        
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        
        cursor.close();
        return count;
    }
} 