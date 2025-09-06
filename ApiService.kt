package com.example.mad_gruop_ass

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ApiService {
    companion object {
        private const val BASE_URL = "http://192.168.56.1:5000"
        private const val TAG = "ApiService"
    }

    // 获取所有用户
    suspend fun getUsers(): List<User> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/api/users")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Response code: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "Response: $response")
                
                val jsonArray = JSONArray(response)
                val users = mutableListOf<User>()
                
                for (i in 0 until jsonArray.length()) {
                    val userJson = jsonArray.getJSONObject(i)
                    val user = User(
                        id = userJson.optInt("user_id", 0),
                        name = userJson.optString("username", ""),
                        email = userJson.optString("email", ""),
                        phone = userJson.optString("phone", "")
                    )
                    users.add(user)
                }
                
                users
            } else {
                Log.e(TAG, "HTTP Error: $responseCode")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching users", e)
            emptyList()
        }
    }

    // 创建新用户
    suspend fun createUser(user: User): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/api/users")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val userJson = JSONObject().apply {
                put("name", user.name)
                put("email", user.email)
                put("phone", user.phone)
            }
            
            connection.outputStream.use { output ->
                output.write(userJson.toString().toByteArray())
            }
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Create user response code: $responseCode")
            
            responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user", e)
            false
        }
    }

    // 更新用户
    suspend fun updateUser(user: User): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/api/users/${user.id}")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "PUT"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val userJson = JSONObject().apply {
                put("name", user.name)
                put("email", user.email)
                put("phone", user.phone)
            }
            
            connection.outputStream.use { output ->
                output.write(userJson.toString().toByteArray())
            }
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Update user response code: $responseCode")
            
            responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user", e)
            false
        }
    }

    // 删除用户
    suspend fun deleteUser(userId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/api/users/$userId")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "DELETE"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Delete user response code: $responseCode")
            
            responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user", e)
            false
        }
    }

    // 获取所有物品
    suspend fun getItems(): List<Item> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/api/items")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Items response code: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "Items response: $response")
                
                val jsonArray = JSONArray(response)
                val items = mutableListOf<Item>()
                
                for (i in 0 until jsonArray.length()) {
                    val itemJson = jsonArray.getJSONObject(i)
                    val item = Item(
                        id = itemJson.optInt("item_id", 0),
                        userId = itemJson.optInt("user_id", 0),
                        title = itemJson.optString("title", ""),
                        description = itemJson.optString("description", ""),
                        price = itemJson.optString("price", "0.00"),
                        imageUrl = itemJson.optString("image_url", ""),
                        status = itemJson.optString("status", "Available"),
                        createdAt = itemJson.optString("created_at", "")
                    )
                    items.add(item)
                }
                
                items
            } else {
                Log.e(TAG, "HTTP Error: $responseCode")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching items", e)
            emptyList()
        }
    }

    // 根据用户ID获取物品
    suspend fun getItemsByUserId(userId: Int): List<Item> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/api/users/$userId/items")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val responseCode = connection.responseCode
            Log.d(TAG, "User items response code: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "User items response: $response")
                
                val jsonArray = JSONArray(response)
                val items = mutableListOf<Item>()
                
                for (i in 0 until jsonArray.length()) {
                    val itemJson = jsonArray.getJSONObject(i)
                    val item = Item(
                        id = itemJson.optInt("item_id", 0),
                        userId = itemJson.optInt("user_id", 0),
                        title = itemJson.optString("title", ""),
                        description = itemJson.optString("description", ""),
                        price = itemJson.optString("price", "0.00"),
                        imageUrl = itemJson.optString("image_url", ""),
                        status = itemJson.optString("status", "Available"),
                        createdAt = itemJson.optString("created_at", "")
                    )
                    items.add(item)
                }
                
                items
            } else {
                Log.e(TAG, "HTTP Error: $responseCode")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user items", e)
            emptyList()
        }
    }
}

// 用户数据类
data class User(
    val id: Int = 0,
    val name: String,
    val email: String,
    val phone: String
)

// 物品数据类
data class Item(
    val id: Int = 0,
    val userId: Int = 0,
    val title: String,
    val description: String,
    val price: String,
    val imageUrl: String,
    val status: String,
    val createdAt: String
)
