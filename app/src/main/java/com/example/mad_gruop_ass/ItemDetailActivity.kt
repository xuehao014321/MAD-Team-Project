package com.example.mad_gruop_ass

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class ItemDetailActivity : AppCompatActivity() {
    
    private lateinit var itemImageView: ImageView
    private lateinit var itemNameTextView: TextView
    private lateinit var itemDescriptionTextView: TextView
    private lateinit var itemStatusTextView: TextView
    private lateinit var postedDateTextView: TextView
    private lateinit var distanceTextView: TextView
    private lateinit var likeCountTextView: TextView
    private lateinit var backButton: ImageButton
    private lateinit var likeButton: ImageButton
    private lateinit var lendButton: Button
    private lateinit var ownerProfileButton: LinearLayout
    private lateinit var ownerNameHeader: TextView
    
    private val apiClient = ApiClient()
    private var currentItem: ItemModel? = null
    private var isLiked = false
    private var likeCount = 0
    
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
        likeButton = findViewById(R.id.like_button)
        lendButton = findViewById(R.id.lend_button)
        ownerProfileButton = findViewById(R.id.owner_profile_button)
        ownerNameHeader = findViewById(R.id.owner_name_header)
    }
    
    private fun setupClickListeners() {
        backButton.setOnClickListener { finish() }
        likeButton.setOnClickListener { toggleLike() }
        lendButton.setOnClickListener { borrowItem() }
        ownerProfileButton.setOnClickListener { 
            Toast.makeText(this, "View User Profile", Toast.LENGTH_SHORT).show()
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
        val itemUsername = intent.getStringExtra("itemUsername") ?: ""
        
        // 显示数据
        itemNameTextView.text = itemTitle
        itemDescriptionTextView.text = itemDescription
        itemStatusTextView.text = itemStatus
        postedDateTextView.text = itemCreatedAt
        distanceTextView.text = itemDistance
        likeCount = itemLikes
        likeCountTextView.text = likeCount.toString()
        ownerNameHeader.text = itemUsername
        
        // 加载图片
        Glide.with(this)
            .load(itemImageUrl)
            .placeholder(R.drawable.default_image)
            .error(R.drawable.default_image)
            .into(itemImageView)
            
        // 设置借用按钮状态
        lendButton.isEnabled = itemStatus == "Available"
        if (itemStatus == "Borrowed") {
            lendButton.text = "Borrowed"
        }
    }
    
    private fun toggleLike() {
        lifecycleScope.launch {
            try {
                isLiked = !isLiked
                likeCount = if (isLiked) likeCount + 1 else likeCount - 1
                likeCountTextView.text = likeCount.toString()
                
                // 更新按钮图标
                if (isLiked) {
                    likeButton.setImageResource(R.drawable.ic_thumb_up)
                } else {
                    likeButton.setImageResource(R.drawable.ic_thumb_up_outline)
                }
                
                Toast.makeText(this@ItemDetailActivity, 
                    if (isLiked) "Liked successfully" else "Unliked", 
                    Toast.LENGTH_SHORT).show()
                    
            } catch (e: Exception) {
                Toast.makeText(this@ItemDetailActivity, 
                    "Operation failed: ", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun borrowItem() {
        lifecycleScope.launch {
            try {
                // 更新UI状态
                itemStatusTextView.text = "Borrowed"
                lendButton.text = "Borrowed"
                lendButton.isEnabled = false
                
                Toast.makeText(this@ItemDetailActivity, 
                    "Borrowed successfully！", 
                    Toast.LENGTH_SHORT).show()
                    
            } catch (e: Exception) {
                Toast.makeText(this@ItemDetailActivity, 
                    "Borrow failed: ", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}
