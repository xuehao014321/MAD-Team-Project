package com.example.mad_gruop_ass

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ItemDetailActivity : AppCompatActivity() {
    
    private lateinit var itemImageView: ImageView
    private lateinit var itemNameTextView: TextView
    private lateinit var itemDescriptionTextView: TextView
    private lateinit var itemStatusTextView: TextView
    private lateinit var postedDateTextView: TextView
    private lateinit var distanceTextView: TextView
    private lateinit var likeCountTextView: TextView
    private lateinit var backButton: ImageButton
    private lateinit var lendButton: Button
    private lateinit var ownerProfileButton: LinearLayout
    private lateinit var ownerNameHeader: TextView
    
    private var currentItem: Item? = null
    private var itemId: Int = 0
    private var itemUsername: String = ""
    private var itemUserId: Int = 0 // 添加用户ID变量
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)
        
        initializeViews()
        setupClickListeners()
        loadItemDetails()
    }
    
    private fun initializeViews() {
        itemImageView = findViewById(R.id.item_image)
        itemNameTextView = findViewById(R.id.item_name)
        itemDescriptionTextView = findViewById(R.id.item_description)
        itemStatusTextView = findViewById(R.id.item_status)
        postedDateTextView = findViewById(R.id.posted_date)
        distanceTextView = findViewById(R.id.distance)
        likeCountTextView = findViewById(R.id.like_count)
        backButton = findViewById(R.id.back_button)
        lendButton = findViewById(R.id.lend_button)
        ownerProfileButton = findViewById(R.id.owner_profile_button)
        ownerNameHeader = findViewById(R.id.owner_name_header)
    }
    
    private fun setupClickListeners() {
        backButton.setOnClickListener { finish() }
        
        lendButton.setOnClickListener { borrowItem() }
        ownerProfileButton.setOnClickListener { 
            Log.d("ItemDetailActivity", "Owner profile button clicked")
            Log.d("ItemDetailActivity", "Username: '$itemUsername', UserId: $itemUserId")
            
            // 优先使用userId，如果没有则使用username
            if (itemUserId > 0) {
                // 使用用户ID跳转
                val intent = Intent(this@ItemDetailActivity, UserProfileActivity::class.java)
                intent.putExtra("user_id", itemUserId)
                intent.putExtra("username", itemUsername) // 也传递用户名作为备用
                startActivity(intent)
                Log.d("ItemDetailActivity", "Navigating to UserProfileActivity with userId: $itemUserId")
            } else if (itemUsername.isNotEmpty()) {
                // 如果没有userId，使用用户名
                val intent = Intent(this@ItemDetailActivity, UserProfileActivity::class.java)
                intent.putExtra("username", itemUsername)
                intent.putExtra("user_id", 0)
                startActivity(intent)
                Log.d("ItemDetailActivity", "Navigating to UserProfileActivity with username: $itemUsername")
            } else {
                Toast.makeText(this, "User information unavailable", Toast.LENGTH_SHORT).show()
                Log.w("ItemDetailActivity", "Both userId and username are empty, cannot navigate to user profile")
            }
        }
    }
    
    private fun loadItemDetails() {
        val itemTitle = intent.getStringExtra("itemTitle") ?: ""
        val itemDescription = intent.getStringExtra("itemDescription") ?: ""
        val itemImageUrl = intent.getStringExtra("itemImageUrl") ?: ""
        val itemStatus = intent.getStringExtra("itemStatus") ?: ""
        val itemLikes = intent.getIntExtra("itemLikes", 0)
        val itemDistance = intent.getStringExtra("itemDistance") ?: ""
        val itemCreatedAt = intent.getStringExtra("itemCreatedAt") ?: ""
        itemUsername = intent.getStringExtra("itemUsername") ?: ""
        itemUserId = intent.getIntExtra("itemUserId", 0) // 获取用户ID
        itemId = intent.getIntExtra("itemId", 0)
        
        // 显示数据
        itemNameTextView.text = itemTitle
        itemDescriptionTextView.text = itemDescription
        itemStatusTextView.text = itemStatus
        postedDateTextView.text = formatDate(itemCreatedAt)
        distanceTextView.text = itemDistance
        likeCountTextView.text = itemLikes.toString()
        ownerNameHeader.text = itemUsername
        
        // 记录日志以便调试
        Log.d("ItemDetailActivity", "Loaded item details - Username: '$itemUsername', UserId: $itemUserId, Title: '$itemTitle'")
        
        // 加载图片 - 如果没有图片URL就不显示
        if (itemImageUrl.isNotEmpty()) {
            loadImageWithFallback(itemImageView, listOf(itemImageUrl), 0)
        } else {
            // 没有图片时隐藏ImageView或显示空白
            itemImageView.visibility = android.view.View.GONE
        }
            
        // 设置借用按钮状态
        lendButton.isEnabled = itemStatus == "Available"
        if (itemStatus == "Borrowed") {
            lendButton.text = "Borrowed"
        }
    }
    
    private fun borrowItem() {
        // Prevent multiple clicks during operation
        lendButton.isEnabled = false
        lendButton.text = "Processing..."
        
        lifecycleScope.launch {
            try {
                // 使用PATCH方法只更新物品状态为已借出
                val updatedItem = Item().apply {
                    this.itemId = this@ItemDetailActivity.itemId
                    this.status = "Borrowed" // 只更新状态为已借出
                }
                
                ApiClient.updateItem(updatedItem, object : ApiClient.ItemCallback {
                    override fun onSuccess(item: Item) {
                        runOnUiThread {
                            // 更新UI状态
                            itemStatusTextView.text = "Borrowed"
                            lendButton.text = "Borrowed"
                            lendButton.isEnabled = false
                            
                            Toast.makeText(this@ItemDetailActivity, 
                                "Borrowed successfully!", 
                                Toast.LENGTH_SHORT).show()
                            
                            // 设置结果，通知MainActivity需要刷新数据
                            setResult(RESULT_OK)
                        }
                    }

                    override fun onError(error: String) {
                        runOnUiThread {
                            // Restore button state on error
                            lendButton.text = "Borrow"
                            lendButton.isEnabled = true
                            
                            Toast.makeText(this@ItemDetailActivity, 
                                "Borrow failed: $error", 
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                })
                    
            } catch (e: Exception) {
                Log.e("ItemDetailActivity", "Borrow failed", e)
                runOnUiThread {
                    // Restore button state on exception
                    lendButton.text = "Borrow"
                    lendButton.isEnabled = true
                    
                    Toast.makeText(this@ItemDetailActivity, 
                        "Borrow failed: ${e.message}", 
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadImageWithFallback(imageView: ImageView, urls: List<String>, currentIndex: Int = 0) {
        if (currentIndex >= urls.size) {
            imageView.visibility = android.view.View.GONE
            return
        }
        
        val currentUrl = urls[currentIndex]
        
        // 简单的图片加载
        try {
            Glide.with(this)
                .load(currentUrl)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(imageView)
        } catch (e: Exception) {
            Log.e("ItemDetailActivity", "Error loading image: ${e.message}")
            imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    private fun formatDate(inputDateString: String): String {
        if (inputDateString.isEmpty()) return ""
        
        return try {
            // 尝试多种可能的日期格式
            val possibleFormats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",  // ISO format with milliseconds
                "yyyy-MM-dd'T'HH:mm:ss'Z'",      // ISO format without milliseconds
                "yyyy-MM-dd HH:mm:ss",           // Standard SQL datetime
                "yyyy-MM-dd"                     // Date only
            )
            
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            
            for (format in possibleFormats) {
                try {
                    val inputFormat = SimpleDateFormat(format, Locale.US)
                    val date = inputFormat.parse(inputDateString)
                    return outputFormat.format(date)
                } catch (e: Exception) {
                    continue // Try next format
                }
            }
            
            // If all formats fail, return original string
            inputDateString
        } catch (e: Exception) {
            Log.e("ItemDetailActivity", "Date formatting failed: ${e.message}")
            inputDateString
        }
    }
}
