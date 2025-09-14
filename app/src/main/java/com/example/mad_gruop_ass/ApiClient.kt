package com.example.mad_gruop_ass

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException
import java.lang.reflect.Type
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

object ApiClient {
    private const val TAG = "ApiClient"

    // ✅ API Base URL - Use your NeighborLink API
    private const val BASE_URL = "http://192.168.0.104:5000/api"

    // ✅ HTTP Client
    private val client = OkHttpClient()
    private val gson = Gson()
    private val mainHandler = Handler(Looper.getMainLooper())

    // ✅ User related callback interfaces
    interface UserCallback {
        fun onSuccess(user: User)
        fun onError(error: String)
    }

    interface UsersListCallback {
        fun onSuccess(users: List<User>)
        fun onError(error: String)
    }

    interface UpdateUserCallback {
        fun onSuccess(message: String)
        fun onError(error: String)
    }

    // ✅ Item related callback interfaces
    interface ItemCallback {
        fun onSuccess(item: Item)
        fun onError(error: String)
    }

    interface ItemsListCallback {
        fun onSuccess(items: List<Item>)
        fun onError(error: String)
    }

    // ✅ Credit related callback interfaces
    interface CreditCallback {
        fun onSuccess(creditPoints: Int)
        fun onError(error: String)
    }

    interface RentalCountCallback {
        fun onSuccess(count: Int)
        fun onError(error: String)
    }

    interface ImageUploadCallback {
        fun onSuccess(imageUrl: String)
        fun onError(error: String)
    }

    // ✅ Reset likes related callback interface
    interface ResetLikesCallback {
        fun onSuccess(message: String)
        fun onError(error: String)
    }

    // ✅ Get all users
    fun getAllUsers(callback: UsersListCallback) {
        val request = Request.Builder()
            .url("$BASE_URL/users")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to get user list", e)
                mainHandler.post { callback.onError("Network request failed: ${e.message}") }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string() ?: ""
                    Log.d(TAG, "Get user list response: $responseBody")

                    if (response.isSuccessful) {
                        // Parse JSON array to User list
                        val listType: Type = object : TypeToken<List<User>>() {}.type
                        val users: List<User> = gson.fromJson(responseBody, listType)

                        mainHandler.post { callback.onSuccess(users) }
                    } else {
                        mainHandler.post { callback.onError("Server error: ${response.code}") }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse user list data", e)
                    mainHandler.post { callback.onError("Data parsing failed: ${e.message}") }
                }
            }
        })
    }

    // ✅ Get user by username (search from user list)
    fun getUserByUsername(username: String, callback: UserCallback) {
        getAllUsers(object : UsersListCallback {
            override fun onSuccess(users: List<User>) {
                // Search for user with specified username in user list
                val foundUser = users.find { it.username == username }

                if (foundUser != null) {
                    callback.onSuccess(foundUser)
                } else {
                    callback.onError("User '$username' does not exist")
                }
            }

            override fun onError(error: String) {
                callback.onError(error)
            }
        })
    }

    // ✅ Get user by email (search from user list)
    fun getUserByEmail(email: String, callback: UserCallback) {
        getAllUsers(object : UsersListCallback {
            override fun onSuccess(users: List<User>) {
                // Search for user with specified email in user list
                val foundUser = users.find { it.email == email }

                if (foundUser != null) {
                    callback.onSuccess(foundUser)
                } else {
                    callback.onError("User with email '$email' does not exist")
                }
            }

            override fun onError(error: String) {
                callback.onError(error)
            }
        })
    }

    // ✅ Get user by email or phone number (search from user list)
    fun getUserByEmailOrPhone(emailOrPhone: String, callback: UserCallback) {
        getAllUsers(object : UsersListCallback {
            override fun onSuccess(users: List<User>) {
                // Search for user with specified email or phone number in user list
                val foundUser = users.find { it.email == emailOrPhone || it.phone == emailOrPhone }

                if (foundUser != null) {
                    callback.onSuccess(foundUser)
                } else {
                    callback.onError("User with email/phone '$emailOrPhone' does not exist")
                }
            }

            override fun onError(error: String) {
                callback.onError(error)
            }
        })
    }

    // ✅ Get user by ID
    fun getUserById(userId: Int, callback: UserCallback) {
        getAllUsers(object : UsersListCallback {
            override fun onSuccess(users: List<User>) {
                // Search for user with specified ID in user list
                val foundUser = users.find { it.userId == userId }

                if (foundUser != null) {
                    callback.onSuccess(foundUser)
                } else {
                    callback.onError("User ID $userId does not exist")
                }
            }

            override fun onError(error: String) {
                callback.onError(error)
            }
        })
    }

    // ✅ Update user information
    fun updateUser(userId: Int, email: String?, phone: String?, callback: UpdateUserCallback) {
        try {
            // Create JSON for update data
            val updateData = JsonObject()
            if (!email.isNullOrBlank()) {
                updateData.addProperty("email", email)
            }
            if (!phone.isNullOrBlank()) {
                updateData.addProperty("phone", phone)
            }

            if (updateData.size() == 0) {
                mainHandler.post { callback.onError("No data provided for update") }
                return
            }

            val jsonString = gson.toJson(updateData)
            Log.d(TAG, "Update user data: $jsonString")

            val requestBody = RequestBody.create(
                "application/json; charset=utf-8".toMediaTypeOrNull(),
                jsonString
            )

            val request = Request.Builder()
                .url("$BASE_URL/users/$userId")
                .patch(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Failed to update user information", e)
                    mainHandler.post { callback.onError("Network request failed: ${e.message}") }
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseBody = response.body?.string() ?: ""
                        Log.d(TAG, "Update user response: $responseBody")

                        if (response.isSuccessful) {
                            val jsonResponse = gson.fromJson(responseBody, JsonObject::class.java)
                            val message = jsonResponse.get("message").asString
                            mainHandler.post { callback.onSuccess(message) }
                        } else {
                            val errorResponse = gson.fromJson(responseBody, JsonObject::class.java)
                            val errorMessage = errorResponse.get("error").asString
                            mainHandler.post { callback.onError("Update failed: $errorMessage") }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse update response", e)
                        mainHandler.post { callback.onError("Response parsing failed: ${e.message}") }
                    }
                }
            })

        } catch (e: Exception) {
            Log.e(TAG, "Failed to create update request", e)
            mainHandler.post { callback.onError("Request creation failed: ${e.message}") }
        }
    }

    // ✅ Test API connection
    fun testConnection(callback: UpdateUserCallback) {
        val request = Request.Builder()
            .url(BASE_URL.replace("/api", "") + "/api/test")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "API connection test failed", e)
                mainHandler.post { callback.onError("Cannot connect to API server: ${e.message}") }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string() ?: ""
                    Log.d(TAG, "API connection test response: $responseBody")

                    if (response.isSuccessful) {
                        val jsonResponse = gson.fromJson(responseBody, JsonObject::class.java)
                        val message = jsonResponse.get("message").asString
                        mainHandler.post { callback.onSuccess("API connection successful: $message") }
                    } else {
                        mainHandler.post { callback.onError("API server error: ${response.code}") }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse connection test response", e)
                    mainHandler.post { callback.onError("Response parsing failed: ${e.message}") }
                }
            }
        })
    }

    // ✅ Get all items
    fun getAllItems(callback: ItemsListCallback) {
        val request = Request.Builder()
            .url("$BASE_URL/items")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to get items list", e)
                mainHandler.post { callback.onError("Network request failed: ${e.message}") }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string() ?: ""
                    Log.d(TAG, "Get items list response: $responseBody")

                    if (response.isSuccessful) {
                        // Parse JSON array to Item list
                        val listType: Type = object : TypeToken<List<Item>>() {}.type
                        val items: List<Item> = gson.fromJson(responseBody, listType)

                        // Debug: Log the parsed items to check all user name fields
                        Log.d(TAG, "=== PARSED ITEMS DEBUG ===")
                        items.forEachIndexed { index, item ->
                            Log.d(TAG, "Item $index: title='${item.title}', userId=${item.userId}")
                            Log.d(TAG, "  - username='${item.username}'")
                            Log.d(TAG, "  - ownerName='${item.ownerName}'")
                            Log.d(TAG, "  - userName='${item.userName}'")
                            Log.d(TAG, "  - owner='${item.owner}'")
                        }
                        Log.d(TAG, "=== END ITEMS DEBUG ===")

                        mainHandler.post { callback.onSuccess(items) }
                    } else {
                        mainHandler.post { callback.onError("Server error: ${response.code}") }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse items list data", e)
                    mainHandler.post { callback.onError("Data parsing failed: ${e.message}") }
                }
            }
        })
    }

    // ✅ Get items by user ID
    fun getItemsByUserId(userId: Int, callback: ItemsListCallback) {
        Log.d(TAG, "Filtering items for user ID: $userId")
        getAllItems(object : ItemsListCallback {
            override fun onSuccess(items: List<Item>) {
                Log.d(TAG, "Total items received: ${items.size}")
                // Filter items for specified user ID
                val userItems = items.filter { item ->
                    Log.d(TAG, "Checking item: ID=${item.itemId}, UserID=${item.userId}, belongs to target user: ${item.userId == userId}")
                    item.userId == userId
                }
                Log.d(TAG, "Filtered items count: ${userItems.size} for user ID: $userId")
                callback.onSuccess(userItems)
            }

            override fun onError(error: String) {
                callback.onError(error)
            }
        })
    }

    // ✅ Get item by ID
    fun getItemById(itemId: Int, callback: ItemCallback) {
        getAllItems(object : ItemsListCallback {
            override fun onSuccess(items: List<Item>) {
                // Search for item with specified ID
                val foundItem = items.find { it.itemId == itemId }

                if (foundItem != null) {
                    callback.onSuccess(foundItem)
                } else {
                    callback.onError("Item ID $itemId does not exist")
                }
            }

            override fun onError(error: String) {
                callback.onError(error)
            }
        })
    }

    // ✅ Upload image to server
    suspend fun uploadImage(imageUri: android.net.Uri, context: android.content.Context): String? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("$BASE_URL/upload")
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.doInput = true
            connection.useCaches = false
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            val boundary = "----AndroidFormBoundary${System.currentTimeMillis()}"
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            connection.setRequestProperty("Accept", "application/json")

            val mimeType = context.contentResolver.getType(imageUri) ?: "application/octet-stream"
            val fileName = "image_${System.currentTimeMillis()}" + when (mimeType) {
                "image/jpeg", "image/jpg" -> ".jpg"
                "image/png" -> ".png"
                "image/webp" -> ".webp"
                else -> ".bin"
            }

            val twoHyphens = "--"
            val lineEnd = "\r\n"

            val output = java.io.DataOutputStream(connection.outputStream)
            context.contentResolver.openInputStream(imageUri)?.use { input ->
                // Header
                output.writeBytes(twoHyphens + boundary + lineEnd)
                output.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"$fileName\"" + lineEnd)
                output.writeBytes("Content-Type: $mimeType" + lineEnd)
                output.writeBytes(lineEnd)

                // File bytes
                val buffer = ByteArray(1024 * 8)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }
                output.writeBytes(lineEnd)

                // Footer
                output.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
                output.flush()
            } ?: run {
                return@withContext null
            }

            val responseCode = connection.responseCode
            val responseBody = try {
                if (responseCode in 200..299) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    connection.errorStream?.bufferedReader()?.use { it.readText() }
                }
            } catch (e: Exception) {
                null
            }

            if (responseCode in 200..299 && responseBody != null) {
                val jsonResponse = JSONObject(responseBody)
                if (jsonResponse.optBoolean("success", false)) {
                    jsonResponse.optString("image_url", null)
                } else null
            } else {
                Log.e(TAG, "Upload failed, code=$responseCode, body=$responseBody")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image", e)
            null
        } finally {
            try { connection?.disconnect() } catch (_: Exception) {}
        }
    }

    // ✅ Get user credit points by user ID
    fun getUserCredit(userId: Int, callback: CreditCallback) {
        Log.d(TAG, "Getting credit for user ID: $userId")
        
        // For now, we'll simulate credit data based on user items
        // In a real implementation, this would call a dedicated credit API endpoint
        getItemsByUserId(userId, object : ItemsListCallback {
            override fun onSuccess(items: List<Item>) {
                // Calculate credit points based on user's items
                // Each item contributes 10 points, available items get bonus
                var creditPoints = 0
                items.forEach { item ->
                    creditPoints += 10 // Base points per item
                    if (item.status.equals("available", ignoreCase = true)) {
                        creditPoints += 5 // Bonus for available items
                    }
                }
                
                // Ensure minimum credit points
                if (creditPoints == 0 && items.isNotEmpty()) {
                    creditPoints = 50 // Default credit for users with items
                } else if (creditPoints == 0) {
                    creditPoints = 25 // Default credit for new users
                }
                
                Log.d(TAG, "Calculated credit points: $creditPoints for user $userId")
                callback.onSuccess(creditPoints)
            }

            override fun onError(error: String) {
                Log.e(TAG, "Failed to calculate credit for user $userId: $error")
                // Return default credit on error
                callback.onSuccess(25)
            }
        })
    }

    // ✅ Get user rental count by user ID
    fun getUserRentalCount(userId: Int, callback: RentalCountCallback) {
        Log.d(TAG, "Getting rental count for user ID: $userId")
        
        getItemsByUserId(userId, object : ItemsListCallback {
            override fun onSuccess(items: List<Item>) {
                // Count available items for rental
                val availableCount = items.count { 
                    it.status.equals("available", ignoreCase = true) 
                }
                
                Log.d(TAG, "User $userId has $availableCount available items for rental")
                callback.onSuccess(availableCount)
            }

            override fun onError(error: String) {
                Log.e(TAG, "Failed to get rental count for user $userId: $error")
                callback.onError(error)
            }
        })
    }

    // ✅ Create new item
    fun createItem(item: Item, callback: ItemCallback) {
        try {
            // Create JSON for the new item
            val itemData = JsonObject()
            itemData.addProperty("user_id", item.userId)
            itemData.addProperty("title", item.title)
            itemData.addProperty("description", item.description)
            itemData.addProperty("price", item.price)
            itemData.addProperty("status", item.status)
            itemData.addProperty("views", item.views)
            itemData.addProperty("likes", item.likes)
            itemData.addProperty("distance", item.distance)
            if (item.imageUrl.isNotEmpty()) {
                itemData.addProperty("image_url", item.imageUrl)
            }
            if (item.createdAt.isNotEmpty()) {
                itemData.addProperty("created_at", item.createdAt)
            }

            val jsonString = gson.toJson(itemData)
            Log.d(TAG, "Create item data: $jsonString")

            val requestBody = RequestBody.create(
                "application/json; charset=utf-8".toMediaTypeOrNull(),
                jsonString
            )

            val request = Request.Builder()
                .url("$BASE_URL/items")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Failed to create item", e)
                    mainHandler.post { callback.onError("Network request failed: ${e.message}") }
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseBody = response.body?.string() ?: ""
                        Log.d(TAG, "Create item response: $responseBody")

                        if (response.isSuccessful) {
                            // Parse the created item from response
                            val createdItem = gson.fromJson(responseBody, Item::class.java)
                            mainHandler.post { callback.onSuccess(createdItem) }
                        } else {
                            val errorResponse = gson.fromJson(responseBody, JsonObject::class.java)
                            val errorMessage = errorResponse.get("error")?.asString ?: "Creation failed"
                            mainHandler.post { callback.onError("Creation failed: $errorMessage") }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse create response", e)
                        mainHandler.post { callback.onError("Response parsing failed: ${e.message}") }
                    }
                }
            })

        } catch (e: Exception) {
            Log.e(TAG, "Failed to create item request", e)
            mainHandler.post { callback.onError("Request creation failed: ${e.message}") }
        }
    }

    // ✅ Update item information
    fun updateItem(item: Item, callback: ItemCallback) {
        try {
            // Create JSON for update data - only include fields that exist in database
            val updateData = JsonObject()
            
            // Only add non-empty/non-default values to avoid database errors
            if (item.title.isNotEmpty()) {
                updateData.addProperty("title", item.title)
            }
            if (item.description.isNotEmpty()) {
                updateData.addProperty("description", item.description)
            }
            if (item.status.isNotEmpty()) {
                updateData.addProperty("status", item.status)
            }
            if (item.views > 0) {
                updateData.addProperty("views", item.views)
            }
            if (item.likes > 0) {
                updateData.addProperty("likes", item.likes)
            }
            if (item.price > 0) {
                updateData.addProperty("price", item.price)
            }
            if (item.distance > 0) {
                updateData.addProperty("distance", item.distance)
            }
            if (item.isLiked > 0) {
                updateData.addProperty("is_liked", item.isLiked)
            }

            val jsonString = gson.toJson(updateData)
            Log.d(TAG, "Update item data: $jsonString")

            val requestBody = RequestBody.create(
                "application/json; charset=utf-8".toMediaTypeOrNull(),
                jsonString
            )

            val request = Request.Builder()
                .url("$BASE_URL/items/${item.itemId}")
                .patch(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Failed to update item information", e)
                    mainHandler.post { callback.onError("Network request failed: ${e.message}") }
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseBody = response.body?.string() ?: ""
                        Log.d(TAG, "Update item response: $responseBody")

                        if (response.isSuccessful) {
                            // Parse the updated item from response
                            val updatedItem = gson.fromJson(responseBody, Item::class.java)
                            mainHandler.post { callback.onSuccess(updatedItem) }
                        } else {
                            val errorResponse = gson.fromJson(responseBody, JsonObject::class.java)
                            val errorMessage = errorResponse.get("error")?.asString ?: "Update failed"
                            mainHandler.post { callback.onError("Update failed: $errorMessage") }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse update response", e)
                        // If parsing fails, return the original item as success
                        mainHandler.post { callback.onSuccess(item) }
                    }
                }
            })

        } catch (e: Exception) {
            Log.e(TAG, "Failed to create update request", e)
            mainHandler.post { callback.onError("Request creation failed: ${e.message}") }
        }
    }

    // ✅ Reset all items is_liked field to 0
    fun resetAllItemsIsLiked(callback: ResetLikesCallback) {
        Log.d(TAG, "=== 开始重置所有物品的is_liked字段 ===")
        
        getAllItems(object : ItemsListCallback {
            override fun onSuccess(items: List<Item>) {
                Log.d(TAG, "获取到${items.size}个物品，开始重置is_liked字段")
                
                if (items.isEmpty()) {
                    Log.d(TAG, "没有物品需要重置")
                    mainHandler.post { callback.onSuccess("没有物品需要重置") }
                    return
                }
                
                // 打印所有物品的当前is_liked状态
                items.forEachIndexed { index, item ->
                    Log.d(TAG, "物品${index + 1}: ID=${item.itemId}, isLiked=${item.isLiked}")
                }
                
                // 异步重置每个物品的is_liked字段
                resetItemsIsLikedSequentially(items, 0, callback)
            }

            override fun onError(error: String) {
                Log.e(TAG, "获取物品列表失败: $error")
                mainHandler.post { callback.onError("获取物品列表失败: $error") }
            }
        })
    }

    // ✅ 递归重置物品的is_liked字段，使用PATCH方法
    private fun resetItemsIsLikedSequentially(items: List<Item>, index: Int, callback: ResetLikesCallback) {
        if (index >= items.size) {
            Log.d(TAG, "所有物品的is_liked字段重置完成")
            mainHandler.post { callback.onSuccess("所有物品的is_liked字段重置完成") }
            return
        }

        val item = items[index]
        Log.d(TAG, "正在重置物品 ${index + 1}/${items.size}: ID=${item.itemId}")

        // 创建更新请求，将is_liked设置为0，使用PATCH方法
        val updateData = JsonObject().apply {
            addProperty("is_liked", 0)
        }
        val json = gson.toJson(updateData)
        val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json)

        val request = Request.Builder()
            .url("$BASE_URL/items/${item.itemId}")
            .patch(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "重置物品 ${item.itemId} 失败", e)
                // 即使某个物品更新失败，也继续处理其他物品
                resetItemsIsLikedSequentially(items, index + 1, callback)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string() ?: ""
                    Log.d(TAG, "重置物品 ${item.itemId} 响应: $responseBody")

                    if (response.isSuccessful) {
                        Log.d(TAG, "物品 ${item.itemId} 的is_liked字段重置成功")
                    } else {
                        Log.e(TAG, "重置物品 ${item.itemId} 失败，状态码: ${response.code}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "解析重置物品 ${item.itemId} 响应失败", e)
                } finally {
                    // 即使某个物品更新失败，也继续处理其他物品
                    resetItemsIsLikedSequentially(items, index + 1, callback)
                }
            }
        })
    }
}