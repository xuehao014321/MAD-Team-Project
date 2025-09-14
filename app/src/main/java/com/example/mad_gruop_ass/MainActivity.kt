package com.example.mad_gruop_ass

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.mad_gruop_ass.utils.UserSessionManager
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private val itemList = mutableListOf<Item>()
    private lateinit var fabAdd: ImageView
    private lateinit var profileIcon: ImageView
    private lateinit var appLogo: ImageView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var userSessionManager: UserSessionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        userSessionManager = UserSessionManager(this)
        setupRecyclerView()
        setupFAB()
        setupProfileIcon()
        setupSwipeRefresh()
        setupLogoClick()
        loadData()
    }

    override fun onResume() {
        super.onResume()
        // 刷新用户头像（可能刚登录）
        updateProfileIcon()
        // 返回页面时刷新，确保数据同步
        refreshData()
    }
    
    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        recyclerView.layoutManager = layoutManager
        
        itemAdapter = ItemAdapter(itemList) { item ->
            // Handle item click - navigate to detail page
            val intent = Intent(this@MainActivity, ItemDetailActivity::class.java)
            intent.putExtra("itemId", item.itemId)  //itemId
            intent.putExtra("itemTitle", item.title)
            intent.putExtra("itemDescription", item.description)
            intent.putExtra("itemImageUrl", item.imageUrl)
            intent.putExtra("itemStatus", item.status)
            intent.putExtra("itemLikes", item.likes)
            // 修复距离处理 - 将 Double 转换为格式化的字符串
            val distanceText = when {
                item.distance <= 0 -> "0 km"
                item.distance < 1 -> "${(item.distance * 1000).toInt()}m"
                else -> "${String.format("%.1f", item.distance)} km"
            }
            intent.putExtra("itemDistance", distanceText)
            intent.putExtra("itemCreatedAt", item.createdAt)
            intent.putExtra("itemUsername", item.username)
            intent.putExtra("itemUserId", item.userId) // ûID
            startActivity(intent)
        }
        recyclerView.adapter = itemAdapter
        
        val spacing = resources.getDimensionPixelSize(R.dimen.item_spacing_small)
        recyclerView.addItemDecoration(StaggeredSpacingItemDecoration(spacing))
    }
    
    private fun setupFAB() {
        fabAdd = findViewById(R.id.fabAdd)
        fabAdd.setOnClickListener {
            val intent = Intent(this, PublishActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupProfileIcon() {
        profileIcon = findViewById(R.id.profileIcon)
        profileIcon.setOnClickListener {
            if (userSessionManager.isLoggedIn()) {
                val intent = Intent(this, MyProfileActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }
    
    private fun setupSwipeRefresh() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
        
        swipeRefreshLayout.setOnRefreshListener {
            refreshDataWithShuffle()
        }
    }
    
    private fun setupLogoClick() {
        appLogo = findViewById(R.id.appLogo)
        appLogo.setOnClickListener {
            refreshDataWithShuffle()
            Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadData() {
        // 使用 ApiClient 的回调方式加载数据
        ApiClient.getAllItems(object : ApiClient.ItemsListCallback {
            override fun onSuccess(items: List<Item>) {
                lifecycleScope.launch {
                    itemList.clear()
                    itemList.addAll(items)
                    itemAdapter.notifyDataSetChanged()
                    swipeRefreshLayout.isRefreshing = false
                }
            }

            override fun onError(error: String) {
                lifecycleScope.launch {
                    Toast.makeText(this@MainActivity, "Failed to load data: $error", Toast.LENGTH_SHORT).show()
                    // 如果API失败，加载备用数据
                    loadFallbackData()
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        })
    }

    private fun refreshData() {
        // 刷新数据，直接调用loadData
        loadData()
    }
    
    private fun refreshDataWithShuffle() {
        // 刷新数据并打乱排序
        ApiClient.getAllItems(object : ApiClient.ItemsListCallback {
            override fun onSuccess(items: List<Item>) {
                lifecycleScope.launch {
                    itemList.clear()
                    // 打乱排序
                    val shuffledItems = items.shuffled()
                    itemList.addAll(shuffledItems)
                    itemAdapter.notifyDataSetChanged()
                    swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(this@MainActivity, "Refresh completed!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(error: String) {
                lifecycleScope.launch {
                    Toast.makeText(this@MainActivity, "Refresh failed: $error", Toast.LENGTH_SHORT).show()
                    // 如果API失败，加载备用数据并打乱
                    loadFallbackDataWithShuffle()
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        })
    }
    
    private fun loadFallbackData() {
        val fallbackItems = listOf(
            Item().apply {
                itemId = 1
                userId = 1
                title = "Basketball"
                description = "Professional basketball, suitable for indoor and outdoor use"
                setPriceString("50")
                imageUrl = "https://picsum.photos/200?1"
                status = "Available"
                views = 120
                likes = 15
                createdAt = "2025-09-02 09:00:00"
                username = "Test User 1"
            },
            Item().apply {
                itemId = 2
                userId = 2
                title = "Guitar"
                description = "Classical guitar with beautiful sound quality"
                setPriceString("200")
                imageUrl = "https://picsum.photos/200?2"
                status = "Available"
                views = 85
                likes = 23
                createdAt = "2025-09-02 10:30:00"
                username = "Test User 2"
            },
            Item().apply {
                itemId = 3
                userId = 3
                title = "Treadmill"
                description = "Home treadmill with complete functions"
                setPriceString("800")
                imageUrl = "https://picsum.photos/200?3"
                status = "Available"
                views = 200
                likes = 45
                createdAt = "2025-09-02 11:15:00"
                username = "Test User 3"
            }
        )
        itemList.addAll(fallbackItems)
        itemAdapter.notifyDataSetChanged()
    }
    
    private fun loadFallbackDataWithShuffle() {
        val fallbackItems = listOf(
            Item().apply {
                itemId = 1
                userId = 1
                title = "Basketball"
                description = "Professional basketball, suitable for indoor and outdoor use"
                setPriceString("50")
                imageUrl = "https://picsum.photos/200?1"
                status = "Available"
                views = 120
                likes = 15
                createdAt = "2025-09-02 09:00:00"
                username = "Test User 1"
            },
            Item().apply {
                itemId = 2
                userId = 2
                title = "Guitar"
                description = "Classical guitar with beautiful sound quality"
                setPriceString("200")
                imageUrl = "https://picsum.photos/200?2"
                status = "Available"
                views = 85
                likes = 23
                createdAt = "2025-09-02 10:30:00"
                username = "Test User 2"
            },
            Item().apply {
                itemId = 3
                userId = 3
                title = "Treadmill"
                description = "Home treadmill with complete functions"
                setPriceString("800")
                imageUrl = "https://picsum.photos/200?3"
                status = "Available"
                views = 200
                likes = 45
                createdAt = "2025-09-02 11:15:00"
                username = "Test User 3"
            }
        )
        itemList.clear()
        // 打乱备用数据的排序
        val shuffledItems = fallbackItems.shuffled()
        itemList.addAll(shuffledItems)
        itemAdapter.notifyDataSetChanged()
    }
    
    private fun updateProfileIcon() {
        if (userSessionManager.isLoggedIn()) {
            val userId = userSessionManager.getUserId()
            ApiClient.getUserById(userId, object : ApiClient.UserCallback {
                override fun onSuccess(user: User) {
                    runOnUiThread {
                        if (!user.avatarUrl.isNullOrEmpty()) {
                            Glide.with(this@MainActivity)
                                .load(user.avatarUrl)
                                .transform(CircleCrop())
                                .placeholder(R.drawable.ic_user_circle)
                                .error(R.drawable.ic_user_circle)
                                .into(profileIcon)
                        } else {
                            profileIcon.setImageResource(R.drawable.ic_user_circle)
                        }
                    }
                }

                override fun onError(error: String) {
                    runOnUiThread {
                        profileIcon.setImageResource(R.drawable.ic_user_circle)
                    }
                }
            })
        } else {
            profileIcon.setImageResource(R.drawable.ic_user_circle)
        }
    }
}