package com.example.mad_gruop_ass

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop

/**
 * 用户详情页面Activity
 * 显示其他用户的个人资料信息，包括头像、姓名、性别、邮箱、信用评分和发布的物品
 */
class UserProfileActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "UserProfileActivity"
    }

    private var backButton: TextView? = null
    private var userAvatar: ImageView? = null
    private var userName: TextView? = null
    private var userGender: TextView? = null
    private var userEmail: TextView? = null
    private var creditRating: RatingBar? = null
    private var creditPointsTextView: TextView? = null
    private var creditInfoIcon: ImageView? = null
    private var creditLevelBadge: TextView? = null
    private var creditFormula: TextView? = null
    private var rentalRecordsCount: TextView? = null
    private var nextLevelPoints: TextView? = null
    private var recyclerView: RecyclerView? = null
    
    // 目标用户信息变量
    private var targetUsername: String = ""
    private var targetUserId: Int = 0
    private var targetUserCredit: Int = 0
    
    // RecyclerView相关变量
    private lateinit var itemAdapter: ItemAdapter
    private val itemList = mutableListOf<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request no title feature MUST be called before setContentView
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)

        // Hide ActionBar to remove the title bar
        supportActionBar?.hide()

        setContentView(R.layout.activity_user_profile)

        // 获取传递的用户信息
        intent?.let {
            targetUsername = it.getStringExtra("username") ?: ""
            targetUserId = it.getIntExtra("user_id", 0)
            targetUserCredit = it.getIntExtra("credit", 0)
            Log.d(TAG, "Received target user: '$targetUsername', userId: $targetUserId, credit: $targetUserCredit")
        }

        // 如果既没有用户名也没有用户ID，显示错误并返回
        if (targetUsername.isEmpty() && targetUserId <= 0) {
            Toast.makeText(this, "用户信息无效", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Set system bars padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupRecyclerView()
        setupUserData()
        setupClickListeners()
    }

    /**
     * 初始化所有视图组件
     */
    private fun initializeViews() {
        try {
            backButton = findViewById(R.id.back_button)
            userAvatar = findViewById(R.id.user_avatar)
            userName = findViewById(R.id.user_name)
            userGender = findViewById(R.id.user_gender)
            userEmail = findViewById(R.id.user_email)
            creditRating = findViewById(R.id.credit_rating)
            creditPointsTextView = findViewById(R.id.credit_points)
            creditInfoIcon = findViewById(R.id.credit_info_icon)
            creditLevelBadge = findViewById(R.id.credit_level_badge)
            creditFormula = findViewById(R.id.credit_formula)
            rentalRecordsCount = findViewById(R.id.rental_records_count)
            nextLevelPoints = findViewById(R.id.next_level_points)
            recyclerView = findViewById(R.id.recyclerView)

            Log.d(TAG, "All views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 设置RecyclerView用于显示用户的物品
     */
    private fun setupRecyclerView() {
        try {
            recyclerView?.let { recyclerView ->
                val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
                recyclerView.layoutManager = layoutManager
                
                itemAdapter = ItemAdapter(itemList) { item ->
                    // Handle item click - navigate to detail page
                    val intent = Intent(this@UserProfileActivity, ItemDetailActivity::class.java)
                    intent.putExtra("itemId", item.itemId)
                    intent.putExtra("itemTitle", item.title)
                    intent.putExtra("itemDescription", item.description)
                    intent.putExtra("itemImageUrl", item.imageUrl)
                    intent.putExtra("itemStatus", item.status)
                    intent.putExtra("itemLikes", item.likes)
                    val distanceText = when {
                        item.distance <= 0 -> "0 km"
                        item.distance < 1 -> "${(item.distance * 1000).toInt()}m"
                        else -> "${String.format("%.1f", item.distance)} km"
                    }
                    intent.putExtra("itemDistance", distanceText)
                    intent.putExtra("itemCreatedAt", item.createdAt)
                    val username = when {
                        item.username.isNotEmpty() -> item.username
                        item.ownerName.isNotEmpty() -> item.ownerName
                        item.userName.isNotEmpty() -> item.userName
                        item.owner.isNotEmpty() -> item.owner
                        else -> "User ${item.userId}"
                    }
                    intent.putExtra("itemUsername", username)
                    intent.putExtra("itemUserId", item.userId) // 添加用户ID
                    startActivity(intent)
                }
                recyclerView.adapter = itemAdapter
                
                val spacing = resources.getDimensionPixelSize(R.dimen.item_spacing_small)
                recyclerView.addItemDecoration(StaggeredSpacingItemDecoration(spacing))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView: ${e.message}")
        }
    }

    /**
     * 设置用户数据
     */
    private fun setupUserData() {
        try {
            // 优先使用userId加载数据，如果没有则使用用户名
            if (targetUserId > 0) {
                Log.d(TAG, "Loading user data by userId: $targetUserId")
                loadUserDataByUserId()
            } else if (targetUsername.isNotEmpty()) {
                Log.d(TAG, "Loading user data by username: '$targetUsername'")
                loadUserDataByUsername()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up user data: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 通过用户ID从API加载用户详细信息
     */
    private fun loadUserDataByUserId() {
        ApiClient.getUserById(targetUserId, object : ApiClient.UserCallback {
            override fun onSuccess(user: User) {
                runOnUiThread {
                    try {
                        // 更新用户信息
                        targetUsername = user.username
                        userName?.text = user.username
                        
                        updateUserInfo(user)
                        
                        // 加载用户物品和信用数据
                        loadCreditDataFromAPI()
                        loadUserItems()
                        
                        Log.d(TAG, " Loaded user data by userId: $targetUserId -> ${user.username}")
                        
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating UI with user data: ${e.message}")
                    }
                }
            }

            override fun onError(error: String) {
                Log.e(TAG, " Failed to load user data by userId: $error")
                runOnUiThread {
                    showEmptyState("无法加载用户信息")
                }
            }
        })
    }

    /**
     * 通过用户名从API加载用户详细信息
     */
    private fun loadUserDataByUsername() {
        userName?.text = targetUsername
        
        ApiClient.getUserByUsername(targetUsername, object : ApiClient.UserCallback {
            override fun onSuccess(user: User) {
                runOnUiThread {
                    try {
                        // 重要：从API响应中获取真实的用户ID
                        targetUserId = user.userId
                        Log.d(TAG, " Got real user ID from API: $targetUserId for username: $targetUsername")
                        
                        updateUserInfo(user)
                        
                        // 现在有了真实的用户ID，加载信用数据和用户物品
                        loadCreditDataFromAPI()
                        loadUserItems()

                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating UI with target user data: ${e.message}")
                    }
                }
            }

            override fun onError(error: String) {
                Log.e(TAG, " Failed to load target user data from API: $error")
                runOnUiThread {
                    showEmptyState("无法加载用户信息")
                }
            }
        })
    }

    /**
     * 更新用户信息UI
     */
    private fun updateUserInfo(user: User) {
        // 更新用户邮箱
        if (user.email.isNotEmpty()) {
            userEmail?.text = user.email
            Log.d(TAG, " Loaded user email: ${user.email}")
        } else {
            userEmail?.text = "未提供邮箱"
        }

        // 更新用户性别
        if (user.gender.isNotEmpty()) {
            userGender?.text = user.gender
            Log.d(TAG, " Loaded user gender: ${user.gender}")
        } else {
            userGender?.text = "未指定"
        }

        // 加载用户头像
        updateUserAvatarFromAPI(user)
    }

    /**
     * 从API数据更新用户头像
     */
    private fun updateUserAvatarFromAPI(user: User) {
        try {
            userAvatar?.let { avatar ->
                if (!user.avatarUrl.isNullOrEmpty()) {
                    // 从API数据加载用户头像
                    Glide.with(this)
                        .load(user.avatarUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .transform(CircleCrop())
                        .into(avatar)
                    Log.d(TAG, " Loaded user avatar from API: ${user.avatarUrl}")
                } else {
                    // 使用默认头像
                    avatar.setImageResource(R.drawable.ic_person)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user avatar from API: ${e.message}")
            // 使用默认头像
            userAvatar?.setImageResource(R.drawable.ic_person)
        }
    }

    /**
     * 设置点击监听器
     */
    private fun setupClickListeners() {
        try {
            // 返回按钮点击监听器
            backButton?.setOnClickListener {
                Log.d(TAG, "Back button clicked")
                finish()
            }

            // 信用信息图标点击监听器
            creditInfoIcon?.setOnClickListener {
                showCreditInfoDialog()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 加载用户的物品列表
     */
    private fun loadUserItems() {
        try {
            // 从API加载目标用户的商品数据
            if (targetUserId > 0) {
                Log.d(TAG, "Loading items for user ID: $targetUserId")
                // 调用API获取用户的所有商品
                ApiClient.getItemsByUserId(targetUserId, object : ApiClient.ItemsListCallback {
                    override fun onSuccess(items: List<Item>) {
                        runOnUiThread {
                            itemList.clear()
                            if (items.isNotEmpty()) {
                                itemList.addAll(items)
                                recyclerView?.visibility = android.view.View.VISIBLE
                                hideEmptyState()
                                Log.d(TAG, " Loaded ${items.size} items for target user $targetUserId")
                            } else {
                                recyclerView?.visibility = android.view.View.GONE
                                showEmptyState("该用户还没有发布任何物品")
                                Log.d(TAG, "No items found for target user $targetUserId")
                            }
                            itemAdapter.notifyDataSetChanged()
                        }
                    }

                    override fun onError(error: String) {
                        runOnUiThread {
                            Log.e(TAG, "Failed to load target user items: $error")
                            // 显示空状态
                            itemList.clear()
                            recyclerView?.visibility = android.view.View.GONE
                            showEmptyState("加载物品失败")
                            itemAdapter.notifyDataSetChanged()
                        }
                    }
                })
            } else {
                Log.w(TAG, "Target user ID is 0, cannot load items")
                showEmptyState("用户信息无效")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading target user items: ${e.message}")
            itemList.clear()
            recyclerView?.visibility = android.view.View.GONE
            showEmptyState("加载物品时出错")
            itemAdapter.notifyDataSetChanged()
        }
    }

    private var emptyStateTextView: TextView? = null

    /**
     * 显示空状态
     */
    private fun showEmptyState(message: String) {
        try {
            // 如果已经有空状态视图，先移除
            hideEmptyState()
            
            // 创建新的空状态视图
            emptyStateTextView = TextView(this).apply {
                text = message
                textSize = 16f
                setTextColor(resources.getColor(android.R.color.darker_gray, null))
                setPadding(32, 64, 32, 64)
                gravity = android.view.Gravity.CENTER
                textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
            }
            
            // 添加到主容器中
            val mainContainer = findViewById<android.view.ViewGroup>(R.id.main)
            
            if (mainContainer != null) {
                // 添加空状态文字
                mainContainer.addView(emptyStateTextView)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing empty state: ${e.message}")
        }
    }

    /**
     * 隐藏空状态
     */
    private fun hideEmptyState() {
        try {
            emptyStateTextView?.let { textView ->
                val parent = textView.parent as? android.view.ViewGroup
                parent?.removeView(textView)
            }
            emptyStateTextView = null
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding empty state: ${e.message}")
        }
    }

    /**
     * Load credit data from API based on target user's published items
     */
    private fun loadCreditDataFromAPI() {
        if (targetUserId > 0) {
            Log.d(TAG, "Loading credit data from API for target user: $targetUsername (ID: $targetUserId)")
            
            // Get target user's items from API to calculate credit
            ApiClient.getItemsByUserId(targetUserId, object : ApiClient.ItemsListCallback {
                override fun onSuccess(items: List<Item>) {
                    runOnUiThread {
                        // Calculate credit based on API items: 50 + (items count × 10)
                        val itemCount = items.size
                        val calculatedCredit = 50 + (itemCount * 10)
                        
                        targetUserCredit = calculatedCredit
                        updateCreditDisplay(calculatedCredit, itemCount)
                        
                        Log.d(TAG, "✅ Target user credit updated from API: $calculatedCredit points (based on $itemCount items)")
                        Log.d(TAG, "📦 Target user items from API:")
                        items.forEachIndexed { index, item ->
                            Log.d(TAG, "   ${index + 1}. ${item.title} (${item.status})")
                        }
                    }
                }
                
                override fun onError(error: String) {
                    Log.e(TAG, "❌ Failed to get items from API for target user: $error")
                    runOnUiThread {
                        // Use base credit score if API fails
                        targetUserCredit = 50
                        updateCreditDisplay(50, 0)
                    }
                }
            })
        } else {
            Log.w(TAG, "No valid target user ID for API call, using default credit")
            // Use base credit score if no targetUserId
            targetUserCredit = 50
            updateCreditDisplay(50, 0)
        }
    }
    
    /**
     * Update all credit-related UI elements
     */
    private fun updateCreditDisplay(credit: Int, itemCount: Int) {
        try {
            // Update credit score
            creditPointsTextView?.text = "$credit points"
            
            // Update rating stars
            val rating = CreditManager.calculateRatingFromCredit(credit)
            creditRating?.rating = rating
            
            // Update credit level badge (English)
            val levelDescription = getCreditLevelDescriptionEnglish(credit)
            creditLevelBadge?.text = levelDescription
            
            // Update item count (API中的实际物品数量)
            rentalRecordsCount?.text = itemCount.toString()
            
            // Calculate next level credit requirement
            val nextLevelCredit = getNextLevelCredit(credit)
            val pointsNeeded = maxOf(0, nextLevelCredit - credit)
            nextLevelPoints?.text = if (pointsNeeded > 0) "$pointsNeeded pts" else "Max Level"
            
            Log.d(TAG, "✅ Credit display updated: ${credit} points, ${itemCount} items, level: $levelDescription")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating credit display: ${e.message}")
        }
    }
    
    /**
     * Get next level credit requirement
     */
    private fun getNextLevelCredit(currentCredit: Int): Int {
        return when {
            currentCredit < 60 -> 60   // Explorer
            currentCredit < 70 -> 70   // Participant
            currentCredit < 80 -> 80   // Engaged
            currentCredit < 100 -> 100 // Active
            else -> currentCredit      // Max level reached
        }
    }

    /**
     * Get credit level description in English
     */
    private fun getCreditLevelDescriptionEnglish(credit: Int): String {
        return when {
            credit >= 100 -> "Active"
            credit >= 80 -> "Engaged" 
            credit >= 70 -> "Participant"
            credit >= 60 -> "Explorer"
            credit >= 50 -> "Beginner"
            else -> "Inactive"
        }
    }

    /**
     * 根据信用积分计算评分 - 现在使用CreditManager
     * 此方法已弃用，请使用CreditManager.calculateRatingFromCredit()
     */
    @Deprecated("Use CreditManager.calculateRatingFromCredit() instead")
    private fun calculateRatingFromCredit(credit: Int): Float {
        return CreditManager.calculateRatingFromCredit(credit)
    }

    /**
     * Show detailed credit information dialog
     */
    private fun showCreditInfoDialog() {
        try {
            val currentCredit = targetUserCredit
            val itemCount = maxOf(0, (currentCredit - 50) / 10)
            val levelDescription = getCreditLevelDescriptionEnglish(currentCredit)
            val nextLevelCredit = getNextLevelCredit(currentCredit)
            val pointsNeeded = maxOf(0, nextLevelCredit - currentCredit)
            
            val message = buildString {
                appendLine("🎯 Credit Score System")
                appendLine("")
                appendLine("📊 User Status:")
                appendLine("• Score: $currentCredit points")
                appendLine("• Level: $levelDescription")
                appendLine("• Published Items: $itemCount")
                appendLine("")
                appendLine("📈 Calculation Formula:")
                appendLine("Score = 50 + (Published Items × 10)")
                appendLine("")
                appendLine("⭐ Level Guide:")
                appendLine("• Beginner: 50 pts (0 items)")
                appendLine("• Explorer: 60 pts (1 item)")
                appendLine("• Participant: 70 pts (2 items)")
                appendLine("• Engaged: 80 pts (3 items)")
                appendLine("• Active: 100+ pts (5+ items)")
                appendLine("")
                if (pointsNeeded > 0) {
                    appendLine("🚀 Next Level:")
                    appendLine("Need $pointsNeeded more points!")
                    appendLine("Publish ${(pointsNeeded + 9) / 10} more items")
                } else {
                    appendLine("🏆 This user has reached the highest level!")
                }
            }
            
            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("💳 Credit Score Details")
                .setMessage(message)
                .setPositiveButton("Got it") { dialog, _ -> dialog.dismiss() }
                .create()
                
            dialog.show()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing credit info dialog: ${e.message}")
            showToast("Credit Score: Based on rental records (Formula: 50 + records×10)", Toast.LENGTH_LONG)
        }
    }

    /**
     * 显示Toast消息
     */
    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }
}
