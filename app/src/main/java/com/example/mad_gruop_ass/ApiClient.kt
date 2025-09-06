package com.example.mad_gruop_ass

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ApiClient {
    companion object {
        // 根据你的网络配置，选择正确的IP地址
        // 以太网: 192.168.0.119 (主要网络)
        // VMware: 192.168.56.1 (虚拟机网络)
        private const val BASE_URL = "http://192.168.56.1:5000"
        private const val TAG = "ApiClient"
        
        // 备用IP地址列表，如果主IP失败可以尝试
        private val FALLBACK_IPS = listOf(
            "192.168.56.1",    // VMware网络
            "192.168.0.119",   // 以太网
            "192.168.159.1",   // VMnet1
            "192.168.184.1"    // VMnet8
        )
    }

    // 获取所有用户
    suspend fun getUsers(): List<UserModel> = withContext(Dispatchers.IO) {
        try {
            val response = makeRequest("$BASE_URL/api/users", "GET")
            if (response != null) {
                parseUsersFromJson(response)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching users", e)
            emptyList()
        }
    }

    // 获取所有商品
    suspend fun getItems(): List<ItemModel> = withContext(Dispatchers.IO) {
        try {
            val response = makeRequest("$BASE_URL/api/items", "GET")
            if (response != null) {
                parseItemsFromJson(response)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching items", e)
            emptyList()
        }
    }

    // 创建新商品
    suspend fun createItem(item: ItemModel): Boolean = withContext(Dispatchers.IO) {
        try {
            val itemJson = JSONObject().apply {
                put("title", item.title)
                put("description", item.description)
                put("price", item.price)
                put("imageUrl", item.imageUrl)
                put("status", item.status)
                put("userId", item.userId)
            }
            
            val response = makeRequest("$BASE_URL/api/items", "POST", itemJson.toString())
            response != null
        } catch (e: Exception) {
            Log.e(TAG, "Error creating item", e)
            false
        }
    }

    // 更新商品
    suspend fun updateItem(item: ItemModel): Boolean = withContext(Dispatchers.IO) {
        try {
            val itemJson = JSONObject().apply {
                put("title", item.title)
                put("description", item.description)
                put("price", item.price)
                put("imageUrl", item.imageUrl)
                put("status", item.status)
            }
            
            val response = makeRequest("$BASE_URL/api/items/${item.itemId}", "PUT", itemJson.toString())
            response != null
        } catch (e: Exception) {
            Log.e(TAG, "Error updating item", e)
            false
        }
    }

    // 删除商品
    suspend fun deleteItem(itemId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = makeRequest("$BASE_URL/api/items/$itemId", "DELETE")
            response != null
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting item", e)
            false
        }
    }

    // 测试连接
    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = makeRequest("$BASE_URL/api/test", "GET")
            response != null
        } catch (e: Exception) {
            Log.e(TAG, "Connection test failed", e)
            false
        }
    }

    // 通用HTTP请求方法
    private suspend fun makeRequest(urlString: String, method: String, body: String? = null): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = method
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            if (body != null) {
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.outputStream.use { output ->
                    output.write(body.toByteArray())
                }
            }
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Request to $urlString - Response code: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                Log.e(TAG, "HTTP Error: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Request failed: $urlString", e)
            null
        }
    }

    // 解析用户数据
    private fun parseUsersFromJson(jsonString: String): List<UserModel> {
        try {
            val jsonArray = JSONArray(jsonString)
            val users = mutableListOf<UserModel>()
            
            for (i in 0 until jsonArray.length()) {
                val userJson = jsonArray.getJSONObject(i)
                val user = UserModel(
                    userId = userJson.optInt("user_id", 0),
                    username = userJson.optString("username", ""),
                    email = userJson.optString("email", ""),
                    phone = userJson.optString("phone", ""),
                    gender = userJson.optString("gender", ""),
                    distance = userJson.optInt("distance", 0),
                    createdAt = userJson.optString("created_at", ""),
                    avatarUrl = userJson.optString("avatar_url", "")
                )
                users.add(user)
            }
            
            return users
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing users JSON", e)
            return emptyList()
        }
    }

    // 解析商品数据
    private fun parseItemsFromJson(jsonString: String): List<ItemModel> {
        try {
            val jsonArray = JSONArray(jsonString)
            val items = mutableListOf<ItemModel>()
            
            for (i in 0 until jsonArray.length()) {
                val itemJson = jsonArray.getJSONObject(i)
                val item = ItemModel(
                    itemId = itemJson.optInt("item_id", 0),
                    userId = itemJson.optInt("user_id", 0),
                    title = itemJson.optString("title", ""),
                    description = itemJson.optString("description", ""),
                    price = itemJson.optString("price", "0"),
                    imageUrl = itemJson.optString("image_url", ""),
                    status = itemJson.optString("status", "Available"),
                    views = itemJson.optInt("views", 0),
                    likes = itemJson.optInt("likes", 0),
                    distance = itemJson.optString("distance", "0 km"),
                    createdAt = itemJson.optString("created_at", ""),
                    username = itemJson.optString("username", "未知用户")
                )
                items.add(item)
            }
            
            return items
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing items JSON", e)
            return emptyList()
        }
    }
}

// 用户数据模型
data class UserModel(
    val userId: Int,
    val username: String,
    val email: String,
    val phone: String,
    val gender: String,
    val distance: Int,
    val createdAt: String,
    val avatarUrl: String
)
