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
        private const val BASE_URL = "http://192.168.56.1:5000"
        private const val TAG = "ApiClient"
    }

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

    suspend fun createItem(item: ItemModel): Boolean = withContext(Dispatchers.IO) {
        try {
            val itemJson = JSONObject().apply {
                put("title", item.title)
                put("description", item.description)
                put("price", item.price)
                put("image_url", item.imageUrl)
                put("status", item.status)
                put("user_id", item.userId)
                put("views", item.views)
                put("likes", item.likes)
                put("distance", item.distance)
            }
            
            val response = makeRequest("$BASE_URL/api/items", "POST", itemJson.toString())
            response != null
        } catch (e: Exception) {
            Log.e(TAG, "Error creating item", e)
            false
        }
    }

    suspend fun uploadImage(imageUri: android.net.Uri, context: android.content.Context): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/api/upload")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.doInput = true
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            
            val boundary = "----WebKitFormBoundary${System.currentTimeMillis()}"
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            
            val inputStream = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                return@withContext null
            }
            
            val outputStream = connection.outputStream
            val writer = outputStream.bufferedWriter()
            
            try {
                writer.write("--$boundary\r\n")
                writer.write("Content-Disposition: form-data; name=\"image\"; filename=\"image.jpg\"\r\n")
                writer.write("Content-Type: image/jpeg\r\n\r\n")
                writer.flush()
                
                val buffer = ByteArray(8192)
                var bytesRead: Int
                
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                
                writer.write("\r\n--$boundary--\r\n")
                writer.flush()
                
            } finally {
                writer.close()
                inputStream.close()
            }
            
            val responseCode = connection.responseCode
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = org.json.JSONObject(responseBody)
                if (jsonResponse.getBoolean("success")) {
                    jsonResponse.getString("image_url")
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image", e)
            null
        }
    }

    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = makeRequest("$BASE_URL/api/test", "GET")
            response != null
        } catch (e: Exception) {
            Log.e(TAG, "Connection test failed", e)
            false
        }
    }

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
            
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Request failed: $urlString", e)
            null
        }
    }

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