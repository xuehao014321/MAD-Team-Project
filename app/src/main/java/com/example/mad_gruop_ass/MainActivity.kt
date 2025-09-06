package com.example.mad_gruop_ass

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private val itemList = mutableListOf<ItemModel>()
    
    private lateinit var fabAdd: ImageView
    private val apiClient = ApiClient()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Enable edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setupRecyclerView()
        setupFAB()
        loadSampleData()
        setupAnimations()
    }
    
    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        
        // Setup StaggeredGridLayoutManager for Pinterest-like layout
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        recyclerView.layoutManager = layoutManager
        
        // Create and set adapter
        itemAdapter = ItemAdapter(itemList) { item ->
            // Handle item click - navigate to detail page
            // TODO: Implement navigation to detail page
        }
        recyclerView.adapter = itemAdapter
        
        // Add item decoration for spacing - Xiaohongshu style
        val spacing = resources.getDimensionPixelSize(R.dimen.item_spacing_small)
        recyclerView.addItemDecoration(StaggeredSpacingItemDecoration(spacing))
    }
    
    private fun setupFAB() {
        fabAdd = findViewById(R.id.fabAdd)
        
        // 设置加号按钮点击事件
        fabAdd.setOnClickListener {
            // 打开发布作品页面
            val intent = Intent(this, PublishActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun loadSampleData() {
        // 首先尝试从API加载数据
        loadDataFromApi()
    }
    
    private fun loadDataFromApi() {
        lifecycleScope.launch {
            try {
                // 测试连接
                val isConnected = apiClient.testConnection()
                if (isConnected) {
                    Log.d("MainActivity", "API连接成功，正在加载数据...")
                    Toast.makeText(this@MainActivity, "API连接成功", Toast.LENGTH_SHORT).show()
                    
                    // 同时加载商品数据和用户数据
                    val apiItems = apiClient.getItems()
                    val apiUsers = apiClient.getUsers()
                    
                    if (apiItems.isNotEmpty()) {
                        // 创建用户ID到用户名的映射
                        val userMap = apiUsers.associate { it.userId to it.username }
                        
                        // 为每个商品添加用户名
                        val itemsWithUsernames = apiItems.map { item ->
                            item.copy(username = userMap[item.userId] ?: "未知用户")
                        }
                        
                        itemList.clear()
                        itemList.addAll(itemsWithUsernames)
                        itemAdapter.notifyDataSetChanged()
                        Log.d("MainActivity", "从API加载了 ${itemsWithUsernames.size} 个商品")
                        Toast.makeText(this@MainActivity, "加载了 ${itemsWithUsernames.size} 个商品", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("MainActivity", "API返回空数据，使用备用数据")
                        loadFallbackData()
                    }
                } else {
                    Log.w("MainActivity", "API连接失败，使用备用数据")
                    Toast.makeText(this@MainActivity, "API连接失败，使用本地数据", Toast.LENGTH_SHORT).show()
                    loadFallbackData()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "加载数据时出错", e)
                Toast.makeText(this@MainActivity, "加载数据失败: ${e.message}", Toast.LENGTH_SHORT).show()
                loadFallbackData()
            }
        }
    }
    
    private fun loadFallbackData() {
        // 备用测试数据 - 展示多种图片类型
        itemList.addAll(listOf(
            ItemModel(
                itemId = 1,
                userId = 1,
                title = "篮球",
                description = "专业篮球，适合室内外使用",
                price = "50",
                imageUrl = "https://picsum.photos/200?1",
                status = "Available",
                views = 120,
                likes = 15,
                distance = "2.5 km",
                createdAt = "2025-09-02 09:00:00",
                username = "测试用户1"
            ),
            ItemModel(
                itemId = 2,
                userId = 2,
                title = "吉他",
                description = "古典吉他，音质优美",
                price = "200",
                imageUrl = "https://picsum.photos/200?2",
                status = "Available",
                views = 85,
                likes = 23,
                distance = "1.8 km",
                createdAt = "2025-09-02 10:30:00",
                username = "测试用户2"
            ),
            ItemModel(
                itemId = 3,
                userId = 3,
                title = "跑步机",
                description = "家用跑步机，功能齐全",
                price = "800",
                imageUrl = "https://picsum.photos/200?3",
                status = "Available",
                views = 200,
                likes = 45,
                distance = "3.2 km",
                createdAt = "2025-09-02 11:15:00",
                username = "测试用户3"
            ),
            ItemModel(
                itemId = 4,
                userId = 4,
                title = "耳机",
                description = "无线蓝牙耳机，降噪功能",
                price = "150",
                imageUrl = "https://picsum.photos/200?4",
                status = "Available",
                views = 95,
                likes = 18,
                distance = "1.5 km",
                createdAt = "2025-09-02 12:00:00",
                username = "测试用户4"
            ),
            ItemModel(
                itemId = 5,
                userId = 5,
                title = "书籍套装",
                description = "编程学习书籍，适合初学者",
                price = "80",
                imageUrl = "https://picsum.photos/200?5",
                status = "Available",
                views = 60,
                likes = 12,
                distance = "2.0 km",
                createdAt = "2025-09-02 13:45:00",
                username = "测试用户5"
            )
        ))
        itemAdapter.notifyDataSetChanged()
    }
    
    private fun setupAnimations() {
        // Add entrance animation to RecyclerView
        recyclerView.alpha = 0f
        recyclerView.translationY = 100f
        
        recyclerView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }
} 