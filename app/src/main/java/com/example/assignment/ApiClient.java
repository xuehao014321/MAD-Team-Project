package com.example.assignment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {
    private static final String TAG = "ApiClient";

    // ✅ API Base URL - Use your NeighborLink API
    private static final String BASE_URL = "http://192.168.0.104:5000/api";

    // ✅ HTTP Client
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ✅ User related callback interfaces
    public interface UserCallback {
        void onSuccess(User user);
        void onError(String error);
    }

    public interface UsersListCallback {
        void onSuccess(List<User> users);
        void onError(String error);
    }

    public interface UpdateUserCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    // ✅ Item related callback interfaces
    public interface ItemCallback {
        void onSuccess(Item item);
        void onError(String error);
    }

    public interface ItemsListCallback {
        void onSuccess(List<Item> items);
        void onError(String error);
    }

    // ✅ Get all users
    public static void getAllUsers(UsersListCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/users")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to get user list", e);
                mainHandler.post(() -> callback.onError("Network request failed: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Get user list response: " + responseBody);

                    if (response.isSuccessful()) {
                        // Parse JSON array to User list
                        Type listType = new TypeToken<List<User>>(){}.getType();
                        List<User> users = gson.fromJson(responseBody, listType);

                        mainHandler.post(() -> callback.onSuccess(users));
                    } else {
                        mainHandler.post(() -> callback.onError("Server error: " + response.code()));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse user list data", e);
                    mainHandler.post(() -> callback.onError("Data parsing failed: " + e.getMessage()));
                }
            }
        });
    }

    // ✅ Get user by username (search from user list)
    public static void getUserByUsername(String username, UserCallback callback) {
        getAllUsers(new UsersListCallback() {
            @Override
            public void onSuccess(List<User> users) {
                // Search for user with specified username in user list
                User foundUser = null;
                for (User user : users) {
                    if (user.getUsername().equals(username)) {
                        foundUser = user;
                        break;
                    }
                }

                if (foundUser != null) {
                    callback.onSuccess(foundUser);
                } else {
                    callback.onError("User '" + username + "' does not exist");
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // ✅ Get user by ID
    public static void getUserById(int userId, UserCallback callback) {
        getAllUsers(new UsersListCallback() {
            @Override
            public void onSuccess(List<User> users) {
                // Search for user with specified ID in user list
                User foundUser = null;
                for (User user : users) {
                    if (user.getUserId() == userId) {
                        foundUser = user;
                        break;
                    }
                }

                if (foundUser != null) {
                    callback.onSuccess(foundUser);
                } else {
                    callback.onError("User ID " + userId + " does not exist");
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // ✅ Update user information
    public static void updateUser(int userId, String email, String phone, UpdateUserCallback callback) {
        try {
            // Create JSON for update data
            JsonObject updateData = new JsonObject();
            if (email != null && !email.trim().isEmpty()) {
                updateData.addProperty("email", email);
            }
            if (phone != null && !phone.trim().isEmpty()) {
                updateData.addProperty("phone", phone);
            }

            if (updateData.size() == 0) {
                mainHandler.post(() -> callback.onError("No data provided for update"));
                return;
            }

            String jsonString = gson.toJson(updateData);
            Log.d(TAG, "Update user data: " + jsonString);

            RequestBody requestBody = RequestBody.create(
                    jsonString,
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(BASE_URL + "/users/" + userId)
                    .patch(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Failed to update user information", e);
                    mainHandler.post(() -> callback.onError("Network request failed: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String responseBody = response.body().string();
                        Log.d(TAG, "Update user response: " + responseBody);

                        if (response.isSuccessful()) {
                            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                            String message = jsonResponse.get("message").getAsString();
                            mainHandler.post(() -> callback.onSuccess(message));
                        } else {
                            JsonObject errorResponse = gson.fromJson(responseBody, JsonObject.class);
                            String errorMessage = errorResponse.get("error").getAsString();
                            mainHandler.post(() -> callback.onError("Update failed: " + errorMessage));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse update response", e);
                        mainHandler.post(() -> callback.onError("Response parsing failed: " + e.getMessage()));
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Failed to create update request", e);
            mainHandler.post(() -> callback.onError("Request creation failed: " + e.getMessage()));
        }
    }

    // ✅ Test API connection
    public static void testConnection(UpdateUserCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL.replace("/api", "") + "/api/test")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "API connection test failed", e);
                mainHandler.post(() -> callback.onError("Cannot connect to API server: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    Log.d(TAG, "API connection test response: " + responseBody);

                    if (response.isSuccessful()) {
                        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                        String message = jsonResponse.get("message").getAsString();
                        mainHandler.post(() -> callback.onSuccess("API connection successful: " + message));
                    } else {
                        mainHandler.post(() -> callback.onError("API server error: " + response.code()));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse connection test response", e);
                    mainHandler.post(() -> callback.onError("Response parsing failed: " + e.getMessage()));
                }
            }
        });
    }

    // ✅ Get all items
    public static void getAllItems(ItemsListCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/items")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to get items list", e);
                mainHandler.post(() -> callback.onError("Network request failed: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Get items list response: " + responseBody);

                    if (response.isSuccessful()) {
                        // Parse JSON array to Item list
                        Type listType = new TypeToken<List<Item>>(){}.getType();
                        List<Item> items = gson.fromJson(responseBody, listType);

                        mainHandler.post(() -> callback.onSuccess(items));
                    } else {
                        mainHandler.post(() -> callback.onError("Server error: " + response.code()));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse items list data", e);
                    mainHandler.post(() -> callback.onError("Data parsing failed: " + e.getMessage()));
                }
            }
        });
    }

    // ✅ Get items by user ID
    public static void getItemsByUserId(int userId, ItemsListCallback callback) {
        Log.d(TAG, "Filtering items for user ID: " + userId);
        getAllItems(new ItemsListCallback() {
            @Override
            public void onSuccess(List<Item> items) {
                Log.d(TAG, "Total items received: " + items.size());
                // Filter items for specified user ID
                List<Item> userItems = new ArrayList<>();
                for (Item item : items) {
                    Log.d(TAG, "Checking item: ID=" + item.getItemId() + ", UserID=" + item.getUserId() + ", belongs to target user: " + (item.getUserId() == userId));
                    if (item.getUserId() == userId) {
                        userItems.add(item);
                    }
                }
                Log.d(TAG, "Filtered items count: " + userItems.size() + " for user ID: " + userId);
                callback.onSuccess(userItems);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // ✅ Get item by ID
    public static void getItemById(int itemId, ItemCallback callback) {
        getAllItems(new ItemsListCallback() {
            @Override
            public void onSuccess(List<Item> items) {
                // Search for item with specified ID
                Item foundItem = null;
                for (Item item : items) {
                    if (item.getItemId() == itemId) {
                        foundItem = item;
                        break;
                    }
                }

                if (foundItem != null) {
                    callback.onSuccess(foundItem);
                } else {
                    callback.onError("Item ID " + itemId + " does not exist");
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
}