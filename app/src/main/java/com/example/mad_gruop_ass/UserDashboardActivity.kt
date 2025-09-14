package com.example.mad_gruop_ass

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.mad_gruop_ass.utils.UserSessionManager

class UserDashboardActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "UserDashboardActivity"
    }

    private var appLogo: ImageView? = null
    private var mainUserName: TextView? = null
    private var mainUserEmail: TextView? = null
    private var profileButton: Button? = null
    private var quickRentals: TextView? = null
    private var quickCredit: TextView? = null
    private var creditInfoIconMain: ImageView? = null
    private var creditLevelDashboard: TextView? = null
    private var creditFormulaDashboard: TextView? = null
    private lateinit var userSessionManager: UserSessionManager
    private var currentUser: String = ""
    private var userId: Int = 0
    private var creditPoints: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request no title feature MUST be called before setContentView
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)

        // Hide ActionBar to remove the title bar
        supportActionBar?.hide()

        setContentView(R.layout.activity_user_dashboard)

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
        setupUserData()
        setupClickListeners()
        setupLogoAnimation()
        updateUserAvatar()
    }

    private fun initializeViews() {
        try {
            appLogo = findViewById(R.id.app_logo)
            mainUserName = findViewById(R.id.main_user_name)
            mainUserEmail = findViewById(R.id.main_user_email)
            profileButton = findViewById(R.id.profile_button)
            quickRentals = findViewById(R.id.quick_rentals)
            quickCredit = findViewById(R.id.quick_credit)
            creditInfoIconMain = findViewById(R.id.credit_info_icon_main)
            creditLevelDashboard = findViewById(R.id.credit_level_dashboard)
            creditFormulaDashboard = findViewById(R.id.credit_formula_dashboard)

            // Add null value check with safe nullable access
            Log.d(TAG, "appLogo found: ${appLogo != null}")
            Log.d(TAG, "mainUserName TextView found: ${mainUserName != null}")
            Log.d(TAG, "mainUserEmail TextView found: ${mainUserEmail != null}")
            Log.d(TAG, "profileButton found: ${profileButton != null}")
            Log.d(TAG, "quickRentals found: ${quickRentals != null}")
            Log.d(TAG, "quickCredit found: ${quickCredit != null}")
            Log.d(TAG, "creditInfoIconMain found: ${creditInfoIconMain != null}")
            Log.d(TAG, "creditLevelDashboard found: ${creditLevelDashboard != null}")
            Log.d(TAG, "creditFormulaDashboard found: ${creditFormulaDashboard != null}")
            Log.d(TAG, "All views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun setupUserData() {
        try {
            if (currentUser.isNotEmpty()) {
                Log.d(TAG, "Setting up data for user: '$currentUser'")

                mainUserName?.text = currentUser
                Log.d(TAG, "Set username text to: $currentUser")

                // Set user email from session or default
                val userEmail = userSessionManager.getEmail() ?: "user@example.com"
                mainUserEmail?.text = userEmail

                // Always calculate credit from API to get accurate item count
                updateCreditPoints()
                
                // Note: updateRentalCount() is not called here as we show item count from credit calculation
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up user data: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun updateUserAvatar() {
        try {
            val userAvatar: ImageView? = findViewById(R.id.user_avatar)
            
            if (userAvatar != null && currentUser.isNotEmpty()) {
                Log.d(TAG, "Loading avatar from API for user: $currentUser")
                
                // Get user data from API to load avatar
                ApiClient.getUserByUsername(currentUser, object : ApiClient.UserCallback {
                    override fun onSuccess(user: User) {
                        runOnUiThread {
                            // Load avatar using Glide if avatarUrl exists
                            if (!user.avatarUrl.isNullOrEmpty()) {
                                Glide.with(this@UserDashboardActivity)
                                    .load(user.avatarUrl)
                                    .transform(CircleCrop())
                                    .placeholder(R.drawable.ic_person)
                                    .error(R.drawable.ic_person)
                                    .into(userAvatar)
                                Log.d(TAG, "✅ Avatar loaded from API: ${user.avatarUrl}")
                            } else {
                                // Use default avatar if no URL provided
                                userAvatar.setImageResource(R.drawable.ic_person)
                                Log.d(TAG, "Using default avatar - no URL from API")
                            }
                            
                            // Also update email if available
                            if (user.email.isNotEmpty()) {
                                mainUserEmail?.text = user.email
                            }
                        }
                    }
                    
                    override fun onError(error: String) {
                        Log.e(TAG, "Failed to load user data from API: $error")
                        runOnUiThread {
                            // Use default avatar on error
                            userAvatar.setImageResource(R.drawable.ic_person)
                        }
                    }
                })
            } else {
                Log.d(TAG, "User avatar view not found or no username available")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user avatar: ${e.message}")
        }
    }

    private fun updateRentalCount() {
        try {
            Log.d(TAG, "Updating rental count from API for user ID: $userId")
            
            if (userId > 0) {
                // Get available items count from API
                ApiClient.getUserRentalCount(userId, object : ApiClient.RentalCountCallback {
                    override fun onSuccess(rentalCount: Int) {
                        runOnUiThread {
                            quickRentals?.text = rentalCount.toString()
                            Log.d(TAG, "Rental count updated from API: $rentalCount")
                        }
                    }
                    
                    override fun onError(error: String) {
                        Log.e(TAG, "Failed to get rental count from API: $error")
                        // Fallback to default value
                        runOnUiThread {
                            quickRentals?.text = "0"
                        }
                    }
                })
            } else {
                // Set default value if no userId
                quickRentals?.text = "0"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating rental count: ${e.message}")
        }
    }

    private fun updateCreditPoints() {
        try {
            Log.d(TAG, "Updating credit points from API based on user items for user: $currentUser (ID: $userId)")
            
            if (userId > 0) {
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
                // Set default value if no userId
                creditPoints = 50
                updateCreditDisplay(50, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating credit points: ${e.message}")
        }
    }
    
    /**
     * Update all credit-related UI elements
     */
    private fun updateCreditDisplay(credit: Int, itemCount: Int) {
        try {
            // Update credit score
            quickCredit?.text = credit.toString()
            
            // Update credit level badge (English)
            val levelDescription = getCreditLevelDescriptionEnglish(credit)
            creditLevelDashboard?.text = levelDescription
            
            // Update item count (shown as "Rental Records" but actually API items)
            quickRentals?.text = itemCount.toString()
            
            // Update formula display with actual item count
            creditFormulaDashboard?.text = "50 + ($itemCount × 10)"
            
            Log.d(TAG, "✅ Dashboard credit display updated: ${credit} points, ${itemCount} items from API, level: $levelDescription")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating credit display: ${e.message}")
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

    private fun setupLogoAnimation() {
        try {
            appLogo?.let { logo ->
                val pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_pulse_animation)
                logo.startAnimation(pulseAnimation)
                Log.d(TAG, "Logo animation started successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up logo animation: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun setupClickListeners() {
        try {
            // Setup click listener for app logo - 跳转到物品列表
            appLogo?.setOnClickListener {
                Log.d(TAG, "App logo clicked, navigating to MainActivity")
                val intent = Intent(this@UserDashboardActivity, MainActivity::class.java)
                startActivity(intent)
            }

            // Setup click listener for profile button
            profileButton?.setOnClickListener {
                Log.d(TAG, "Profile button clicked, navigating to MyProfileActivity")
                val intent = Intent(this@UserDashboardActivity, MyProfileActivity::class.java)
                intent.putExtra("username", currentUser)
                intent.putExtra("user_id", userId)
                intent.putExtra("credit", creditPoints)
                startActivity(intent)
            }

            // Setup credit info icon click event - show credit information
            creditInfoIconMain?.setOnClickListener {
                showCreditInfoDialog()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh user data when returning to this activity
        updateUserAvatar()
        updateRentalCount()
        updateCreditPoints()
    }

    /**
     * Show detailed credit information dialog
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
                .setNeutralButton("Refresh") { _, _ -> 
                    // Refresh credit data
                    updateCreditPoints()
                }
                .create()
                
            dialog.show()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing credit info dialog: ${e.message}")
            showToast("Credit Score: Based on rental records (Formula: 50 + records×10)", Toast.LENGTH_LONG)
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

    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }

    /**
     * Test API credit calculation
     */
    private fun testApiCreditCalculation() {
        Log.d(TAG, "🧪 === TESTING API CREDIT CALCULATION === 🧪")
        showToast("Testing API credit calculation...")
        
        if (userId > 0) {
            ApiClient.getUserCredit(userId, object : ApiClient.CreditCallback {
                override fun onSuccess(credit: Int) {
                    runOnUiThread {
                        creditPoints = credit
                        quickCredit?.text = creditPoints.toString()
                        
                        // Show detailed info
                        showToast(
                            "🎯 API credit calculation completed!\n" +
                            "👤 User: $currentUser\n" +
                            "🎉 Latest credit: $credit points\n" +
                            "📦 Based on API data calculation", 
                            Toast.LENGTH_LONG)
                        
                        Log.d(TAG, "✅ API credit test completed: $credit")
                    }
                }
                
                override fun onError(error: String) {
                    runOnUiThread {
                        showToast(
                            "❌ API credit calculation failed: $error", 
                            Toast.LENGTH_LONG)
                        
                        Log.e(TAG, "❌ API credit test failed: $error")
                    }
                }
            })
        } else {
            showToast("❌ No user ID available for API test", Toast.LENGTH_SHORT)
        }
    }
} 