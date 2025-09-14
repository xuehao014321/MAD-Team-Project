package com.example.mad_gruop_ass

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
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
import com.example.mad_gruop_ass.utils.UserSessionManager

class MyProfileActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MyProfileActivity"
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
    private var logoutButton: Button? = null
    private lateinit var userSessionManager: UserSessionManager
    private var currentUser: String = ""
    private var userId: Int = 0
    private var creditPoints: Int = 0
    
    // RecyclerView相关变量
    private lateinit var itemAdapter: ItemAdapter
    private val itemList = mutableListOf<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request no title feature MUST be called before setContentView
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)

        // Hide ActionBar to remove the title bar
        supportActionBar?.hide()

        setContentView(R.layout.activity_my_profile)

        userSessionManager = UserSessionManager(this)

        // Get user data from intent or session
        intent?.let {
            if (it.hasExtra("username")) {
                currentUser = it.getStringExtra("username") ?: ""
                userId = it.getIntExtra("user_id", 0)
                creditPoints = it.getIntExtra("credit", 0)
                Log.d(TAG, "Received username: '$currentUser', userId: $userId, credit: $creditPoints")
            }
        }

        // If no username from intent, try to get from session
        if (currentUser.isEmpty()) {
            currentUser = userSessionManager.getUsername() ?: "Username"
            Log.d(TAG, "Using session username: '$currentUser'")
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
        loadUserItems()
    }

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
            logoutButton = findViewById(R.id.logout_button)

            Log.d(TAG, "All views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun setupRecyclerView() {
        try {
            recyclerView?.let { recyclerView ->
                val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
                recyclerView.layoutManager = layoutManager
                
                itemAdapter = ItemAdapter(itemList) { item ->
                    // Handle item click - navigate to detail page
                    val intent = Intent(this@MyProfileActivity, ItemDetailActivity::class.java)
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

    private fun setupUserData() {
        try {
            if (currentUser.isNotEmpty()) {
                Log.d(TAG, "Setting up data for user: '$currentUser'")

                // Set username immediately
                userName?.text = currentUser

                // Load user data from API
                loadUserDataFromAPI()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up user data: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Load user data from API including gender, email, and other profile info
     */
    private fun loadUserDataFromAPI() {
        if (currentUser.isNotEmpty()) {
            ApiClient.getUserByUsername(currentUser, object : ApiClient.UserCallback {
                override fun onSuccess(user: User) {
                    runOnUiThread {
                        try {
                            // Update user email
                            if (user.email.isNotEmpty()) {
                                userEmail?.text = user.email
                                Log.d(TAG, " Loaded user email: ${user.email}")
                            } else {
                                userEmail?.text = userSessionManager.getEmail() ?: "user@example.com"
                            }

                            // Update user gender - this is what was missing!
                            if (user.gender.isNotEmpty()) {
                                userGender?.text = user.gender
                                Log.d(TAG, " Loaded user gender: ${user.gender}")
                            } else {
                                userGender?.text = "Not specified"
                                Log.d(TAG, "No gender data found for user: $currentUser")
                            }

                            // Update credit rating based on credit points
                            val rating = calculateRatingFromCredit(creditPoints)
                            creditRating?.rating = rating
                            creditPointsTextView?.text = "$creditPoints points"
                            
                            // Load actual credit data from API
                            loadCreditDataFromAPI()

                            // Load user avatar with the user's avatar URL
                            updateUserAvatarFromAPI(user)

                        } catch (e: Exception) {
                            Log.e(TAG, "Error updating UI with user data: ${e.message}")
                        }
                    }
                }

                override fun onError(error: String) {
                    Log.e(TAG, " Failed to load user data from API: $error")
                    runOnUiThread {
                        // Set fallback data
                        userEmail?.text = userSessionManager.getEmail() ?: "user@example.com"
                        userGender?.text = "Not specified"
                        creditRating?.rating = 4.0f
                        creditPointsTextView?.text = "$creditPoints points"
                        
                        // Still try to load credit data from API
                        loadCreditDataFromAPI()
                        
                        // Still try to load avatar from session
                        updateUserAvatar()
                    }
                }
            })
        } else {
            Log.w(TAG, "No username available to load user data")
        }
    }

    /**
     * Update user avatar from API user data
     */
    private fun updateUserAvatarFromAPI(user: User) {
        try {
            userAvatar?.let { avatar ->
                if (!user.avatarUrl.isNullOrEmpty()) {
                    // Load user avatar from API data
                    Glide.with(this)
                        .load(user.avatarUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .transform(CircleCrop())
                        .into(avatar)
                    Log.d(TAG, " Loaded user avatar from API: ${user.avatarUrl}")
                } else {
                    // Fallback to session or default
                    updateUserAvatar()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user avatar from API: ${e.message}")
            // Fallback to original method
            updateUserAvatar()
        }
    }

    /**
     * Fallback method to update user avatar from session
     */
    private fun updateUserAvatar() {
        try {
            userAvatar?.let { avatar ->
                if (userSessionManager.isLoggedIn()) {
                    val avatarUrl = userSessionManager.getAvatarUrl()
                    if (!avatarUrl.isNullOrEmpty()) {
                        // Load user avatar
                        Glide.with(this)
                            .load(avatarUrl)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .transform(CircleCrop())
                            .into(avatar)
                    } else {
                        // Use default avatar
                        avatar.setImageResource(R.drawable.ic_person)
                    }
                } else {
                    // Use default avatar
                    avatar.setImageResource(R.drawable.ic_person)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user avatar: ${e.message}")
        }
    }

    private fun setupClickListeners() {
        try {
            // Back button click listener
            backButton?.setOnClickListener {
                Log.d(TAG, "Back button clicked")
                finish()
            }

            // Credit info icon click listener
            creditInfoIcon?.setOnClickListener {
                showCreditInfoDialog()
            }

            // Logout button click listener
            logoutButton?.setOnClickListener {
                Log.d(TAG, "Logout button clicked")
                performLogout()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun loadUserItems() {
        try {
            // 从API加载当前用户的商品数据
            if (userSessionManager.isLoggedIn()) {
                val currentUserId = userSessionManager.getUserId()
                if (currentUserId != -1) {
                    // 调用API获取用户的所有商品
                    ApiClient.getItemsByUserId(currentUserId, object : ApiClient.ItemsListCallback {
                        override fun onSuccess(items: List<Item>) {
                            runOnUiThread {
                                itemList.clear()
                                if (items.isNotEmpty()) {
                                    itemList.addAll(items)
                                    recyclerView?.visibility = android.view.View.VISIBLE
                                    hideEmptyState()
                                } else {
                                    recyclerView?.visibility = android.view.View.GONE
                                    showEmptyState("You haven't posted any items yet")
                                }
                                itemAdapter.notifyDataSetChanged()
                                Log.d(TAG, "Loaded ${items.size} items for user $currentUserId")
                            }
                        }

                        override fun onError(error: String) {
                            runOnUiThread {
                                Log.e(TAG, "Failed to load user items: $error")
                                // 显示空状态
                                itemList.clear()
                                recyclerView?.visibility = android.view.View.GONE
                                showEmptyState("Failed to load items")
                                itemAdapter.notifyDataSetChanged()
                            }
                        }
                    })
                } else {
                    Log.e(TAG, "User ID not found in session")
                    itemList.clear()
                    recyclerView?.visibility = android.view.View.GONE
                    showEmptyState("Please login to view your items")
                    itemAdapter.notifyDataSetChanged()
                }
            } else {
                Log.e(TAG, "User not logged in")
                itemList.clear()
                recyclerView?.visibility = android.view.View.GONE
                showEmptyState("Please login to view your items")
                itemAdapter.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading user items: ${e.message}")
            itemList.clear()
            recyclerView?.visibility = android.view.View.GONE
            showEmptyState("Error loading items")
            itemAdapter.notifyDataSetChanged()
        }
    }

    private var emptyStateTextView: TextView? = null

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
            
            // 添加到主容器中，在logout按钮之前
            val mainContainer = findViewById<android.view.ViewGroup>(R.id.main)
            val logoutButton = findViewById<Button>(R.id.logout_button)
            
            if (mainContainer != null && logoutButton != null) {
                // 找到logout按钮的索引位置
                val logoutIndex = mainContainer.indexOfChild(logoutButton)
                if (logoutIndex != -1) {
                    // 在logout按钮之前插入空状态文字
                    mainContainer.addView(emptyStateTextView, logoutIndex)
                } else {
                    // 如果找不到logout按钮，就添加到最后
                    mainContainer.addView(emptyStateTextView)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing empty state: ${e.message}")
        }
    }

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

    private fun performLogout() {
        // Clear user session
        userSessionManager.clearSession()
        
        // Show logout message
        showToast("Logged out successfully")
        
        // Navigate to login activity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

        /**
     * Load credit data from API based on user's published items
     */
    private fun loadCreditDataFromAPI() {
        if (userId > 0) {
            Log.d(TAG, "Loading credit data from API for user: $currentUser (ID: $userId)")
            
            // Get user's items from API to calculate credit
            ApiClient.getItemsByUserId(userId, object : ApiClient.ItemsListCallback {
                override fun onSuccess(items: List<Item>) {
                    runOnUiThread {
                        // Calculate credit based on API items: 50 + (items count × 10)
                        val itemCount = items.size
                        val calculatedCredit = 50 + (itemCount * 10)
                        
                        creditPoints = calculatedCredit
                        updateCreditDisplay(calculatedCredit, itemCount)
                        
                        Log.d(TAG, "✅ Credit updated from API: $calculatedCredit points (based on $itemCount items)")
                        Log.d(TAG, "📦 User items from API:")
                        items.forEachIndexed { index, item ->
                            Log.d(TAG, "   ${index + 1}. ${item.title} (${item.status})")
                        }
                    }
                }
                
                override fun onError(error: String) {
                    Log.e(TAG, "❌ Failed to get items from API: $error")
                    runOnUiThread {
                        // Use base credit score if API fails
                        creditPoints = 50
                        updateCreditDisplay(50, 0)
                    }
                }
            })
        } else {
            Log.w(TAG, "No valid user ID for API call, using default credit")
            // Use base credit score if no userId
            creditPoints = 50
            updateCreditDisplay(50, 0)
        }
    }
    
    /**
     * 更新Credit显示的所有UI元素
     */
    private fun updateCreditDisplay(credit: Int, itemCount: Int) {
        try {
            // 更新积分显示
            creditPointsTextView?.text = "$credit points"
            
            // 更新星级评分
            val rating = CreditManager.calculateRatingFromCredit(credit)
            creditRating?.rating = rating
            
            // 更新等级徽章 (英文)
            val levelDescription = getCreditLevelDescriptionEnglish(credit)
            creditLevelBadge?.text = levelDescription
            
            // 更新物品数显示 (API中的实际物品数量)
            rentalRecordsCount?.text = itemCount.toString()
            
            // 计算下次升级所需积分
            val nextLevelCredit = getNextLevelCredit(credit)
            val pointsNeeded = maxOf(0, nextLevelCredit - credit)
            nextLevelPoints?.text = if (pointsNeeded > 0) "$pointsNeeded pts" else "Max Level"
            
            // 更新徽章背景颜色（根据等级）
            updateBadgeBackground(credit)
            
            Log.d(TAG, "✅ Credit显示已更新: ${credit}分, ${itemCount}个物品, 等级: $levelDescription")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ 更新Credit显示时出错: ${e.message}")
        }
    }
    
    /**
     * 获取下一等级所需的Credit分数
     */
    private fun getNextLevelCredit(currentCredit: Int): Int {
        return when {
            currentCredit < 60 -> 60   // 体验用户
            currentCredit < 70 -> 70   // 参与用户
            currentCredit < 80 -> 80   // 积极用户
            currentCredit < 100 -> 100 // 活跃用户
            else -> currentCredit      // 已达最高等级
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
     * 根据Credit等级更新徽章背景颜色
     */
    private fun updateBadgeBackground(credit: Int) {
        try {
            val colorResId = when {
                credit >= 100 -> android.R.color.holo_green_dark  // 活跃用户 - 绿色
                credit >= 80 -> android.R.color.holo_blue_dark    // 积极用户 - 蓝色  
                credit >= 70 -> android.R.color.holo_purple       // 参与用户 - 紫色
                credit >= 60 -> android.R.color.holo_orange_dark  // 体验用户 - 橙色
                else -> android.R.color.darker_gray               // 新手用户 - 灰色
            }
            
            // 这里可以动态创建不同颜色的背景，暂时保持原有样式
            // creditLevelBadge?.setBackgroundColor(ContextCompat.getColor(this, colorResId))
            
        } catch (e: Exception) {
            Log.e(TAG, "更新徽章背景时出错: ${e.message}")
        }
    }

    /**
     * Calculate rating from credit points - now using CreditManager
     * This method is deprecated, use CreditManager.calculateRatingFromCredit() instead
     */
    @Deprecated("Use CreditManager.calculateRatingFromCredit() instead")
    private fun calculateRatingFromCredit(credit: Int): Float {
        return CreditManager.calculateRatingFromCredit(credit)
    }

    /**
     * 显示Credit信息详细对话框
     */
    private fun showCreditInfoDialog() {
        try {
            val currentCredit = creditPoints
            val itemCount = maxOf(0, (currentCredit - 50) / 10)
            val levelDescription = getCreditLevelDescriptionEnglish(currentCredit)
            val nextLevelCredit = getNextLevelCredit(currentCredit)
            val pointsNeeded = maxOf(0, nextLevelCredit - currentCredit)
            
            val message = buildString {
                appendLine("🎯 Credit Score System")
                appendLine("")
                appendLine("📊 Current Status:")
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
                    appendLine("🏆 Congratulations! You've reached the highest level!")
                }
            }
            
            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("💳 Credit Score Details")
                .setMessage(message)
                .setPositiveButton("Got it") { dialog, _ -> dialog.dismiss() }
                .setNeutralButton("View Items") { _, _ -> 
                    // 可以跳转到物品列表页面
                    showToast("Item list feature coming soon...")
                }
                .create()
                
            dialog.show()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing credit info dialog: ${e.message}")
            showToast("Credit Score: Based on rental records (Formula: 50 + records×10)", Toast.LENGTH_LONG)
        }
    }

    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }
}
